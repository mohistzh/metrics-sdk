package me.mohistzh.metrics.sdk.custom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.mohistzh.metrics.sdk.model.DataPoint;
import me.mohistzh.metrics.sdk.util.InetAddressesUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Write custom metrics data to kafka server, format like custom.{business_unit}.{module}.{action}
 *
 * e.g.
 * if you want to analyse user module's sign-in / sign-up data, you are able to write metrics data like this way: <p>custom.mybusinessunit.user.signin/signup</p>
 *
 * @Author Jonathan
 * @Date 2019/12/25
 **/
@Slf4j
@Component
@NoArgsConstructor
public class CustomMetricsReporter implements InitializingBean {


    @Setter
    @Value("${custom.metrics.enabled:false}")
    private boolean enabled;

    @Setter
    @Value("${custom.metrics.bu:\"\"}")
    private String bu;

    @Setter
    @Value("custom.metrics.topic:\"\"")
    private String topic;

    @Setter
    @Value("default.application.name:\"\"")
    private String app;

    @Setter
    @Value("custom.metrics.server:\"\"")
    private String server;

    private String ip;

    private KafkaProducer<String, String> producer;

    private SerializeConfig jsonConfig;

    // custom.{bu}.{module}.{action}
    private static final String KEY_FORMAT = "custom.%s.%s.%s";


    /**
     *
     * @param bu business unit
     * @param app application name
     * @param server kafka server addresse
     */
    public CustomMetricsReporter(String bu, String app, String server) {
        this.bu = bu;
        this.app = app;
        this.server = server;
    }

    /**
     * Write custom metrics data
     * @param module module name, e.g. a module name like authentication
     * @param action functionality, like user sign-in or sign-up
     * @param value data value of metrics
     * @param tags alias
     */
    public void report(String module, String action, Number value, Map<String, String> tags) {
        if (!enabled) {
            return;
        }

        String key = String.format(KEY_FORMAT, bu, module, action);
        // second format of timestamp
        double timestamp = System.currentTimeMillis() / 1000.0;
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.putIfAbsent("ip", ip);
        tags.putIfAbsent("app", app);

        DataPoint dataPoint = new DataPoint(key, value, timestamp, tags);
        String json = JSON.toJSONString(dataPoint, jsonConfig);
        producer.send(new ProducerRecord<>(topic, json), ((metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send custom metric data: {}", exception);
            }
        }));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enabled) {
            return;
        }
        try {
            Map<String, String> properties = new HashMap<>();
            properties.put("bootstrap.servers", server);
            properties.put("acks", "1");
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer(properties);
        } catch (Exception e) {
            log.error("Unable connect to custom-metrics-kafka: {}", e);
        }
        try {
            jsonConfig = new SerializeConfig();
            jsonConfig.put(Double.class, new DoubleSerializer("###.###"));
        } catch (Exception e) {
            log.error("Initialize SerializeConfig occurs exception:{}" ,e);
        }
        try {
            ip = InetAddressesUtil.getLocalIpAddress();
        } catch (Exception e) {
            ip = "unknown";
            log.warn("Failed to determine local ip address:{}", e);
        }

    }
}
