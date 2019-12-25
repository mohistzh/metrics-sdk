package me.mohistzh.metrics.sdk.util;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
/**
 * @Author Jonathan
 * @Date 2019/12/25
 **/
@Slf4j
public class InetAddressesUtil {

    private static final String LOCAL_IP = null;
    private static final String DEFAULT_LOCAL_IP = "127.0.0.1";
    private static volatile String ipGateway;
    private static volatile List<String> NETWORK_INTERFACE_NAMES;


    private InetAddressesUtil(){}

    public static String getLocalIpAddress() {
        return LOCAL_IP;
    }

    private static String getLocalIp() {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            return getLocalHostLanAddress();
        }
        return getInet4Address();
    }



    private static String getInet4Address() {
        try {
            Enumeration<?> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface =(NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                List<InetAddress> addressList = Collections.list(addresses);
                for (InetAddress address : addressList) {
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        if (isGateway(ip)) {
                            return ip;
                        }
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return DEFAULT_LOCAL_IP;
    }

    /**
     * determine if input ip address is gateway
     * @param ip
     * @return
     */
    private static boolean isGateway(String ip) {
        if (ipGateway == null) {
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            Process process = null;
            BufferedReader bufferedReader = null;
            try {
                if (SystemUtils.IS_OS_WINDOWS) {
                    String ipFlag = "0.0.0.0";

                        process = Runtime.getRuntime().exec("route print");
                        inputStream = process.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(inputStreamReader);

                        String result = bufferedReader.lines().filter(x -> x.contains(ipFlag)).findFirst().orElse(null);
                        if (result != null) {
                            // use one space instead of two spaces
                            while (result.contains("  ")) {
                                result = result.replaceAll("  ", " ");
                            }

                            ipGateway = result.split(" ")[3];
                        }

                }
                if (ipGateway != null) {
                    String[] bits = ipGateway.split("\\.");
                    ipGateway = bits[0].concat(".").concat(bits[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                }
            }
        }
        return ip.startsWith(ipGateway);
    }
    private static String getLocalHostLanAddress() {
        if (NETWORK_INTERFACE_NAMES == null) {
            NETWORK_INTERFACE_NAMES = Lists.newArrayList("bond0", "eth0", "en0");
        }

        InetAddress candidateAddress = null;

        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                if (!NETWORK_INTERFACE_NAMES.contains(iface.getDisplayName())) {
                    continue;
                }
                for (Enumeration<InetAddress> inetAddressEnumeration = iface.getInetAddresses(); inetAddressEnumeration.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) inetAddressEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddress;
                        }
                    }

                }
            }

            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }

            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress.getHostAddress();

        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            log.error("Failed to get LAN ip address.", e);
        }
        return DEFAULT_LOCAL_IP;
    }

}
