package me.mohistzh.metrics.sdk.core.reporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import me.mohistzh.metrics.sdk.config.MetricsSdkProperties;
import me.mohistzh.metrics.sdk.core.context.LatencyStatsRegistry;
import me.mohistzh.metrics.sdk.util.InetAddressesUtil;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Get started with Metrics
 * @Author Jonathan
 * @Date 2019/12/25
 **/
@Slf4j
public class MetricsReporterDaemon {

    // second period of time
    private static final int REPORT_PERIOD = 5;
    private static final int JVM_PERIOD = 1;
    private static final int LATENCY_PERIOD = 5;

    private final MetricsSdkProperties metricsSdkProperties;
    private final MetricsReporter metricsReporter;
    private final MetricsReporter jvmMetricsReporter;
    private final MetricRegistry jvmMetricRegistry = new MetricRegistry();
    private final LatencyStatsRegistry latencyStatsRegistry;
    private final LatencyReporter latencyReporter;

    /**
     * build and init service
     * @param metricsSdkProperties
     * @param metricRegistry
     * @param latencyStatsRegistry
     * @param appName
     */
    public MetricsReporterDaemon(MetricsSdkProperties metricsSdkProperties, MetricRegistry metricRegistry,
                                 LatencyStatsRegistry latencyStatsRegistry, String appName) {

        this.metricsSdkProperties = metricsSdkProperties;
        this.latencyStatsRegistry = latencyStatsRegistry;

        String hostName = "unknow";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Unable to get hostname: {}", hostName);
        }

        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        ImmutableMap<String, String> instanceTags = ImmutableMap.of("ip", InetAddressesUtil.getLocalIpAddress(),
                "host", hostName, "app", appName, "pid", pid);

        this.metricsReporter = new MetricsReporter(this.metricsSdkProperties, metricRegistry, instanceTags);
        this.jvmMetricsReporter = new MetricsReporter(this.metricsSdkProperties, jvmMetricRegistry, instanceTags);
        this.latencyReporter = new LatencyReporter(this.metricsSdkProperties, this.latencyStatsRegistry, instanceTags);
        this.start();
    }

    private void start() {
        initJvmMetrics();
        this.jvmMetricsReporter.getReporter().start(JVM_PERIOD, TimeUnit.SECONDS);
        this.metricsReporter.getReporter().start(REPORT_PERIOD, TimeUnit.SECONDS);
        this.metricsReporter.getReporter().start(LATENCY_PERIOD, TimeUnit.SECONDS);
    }

    /**
     * buffers, classes, memory, gc, and threads
     */
    private void initJvmMetrics() {
        jvmMetricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        jvmMetricRegistry.register("jvm.classes", new ClassLoadingGaugeSet());
        jvmMetricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        jvmMetricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        jvmMetricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
    }


}
