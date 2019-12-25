package me.mohistzh.metrics.sdk.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * SDK Configuration properties
 * @Author Jonathan
 * @Date 2019/12/24
 **/
@Data
@ConfigurationProperties("metrics")
public class MetricsSdkProperties {

    private static final String DEFAULT_TOPIC = "metrics";
    private static final boolean DEFAULT_ENABLED = false;

    private String server;
    private String topic = DEFAULT_TOPIC;
    private boolean enabled = DEFAULT_ENABLED;
    private List<String> clientUrls = Lists.newArrayList();
}
