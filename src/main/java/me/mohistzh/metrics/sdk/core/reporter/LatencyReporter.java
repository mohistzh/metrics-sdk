package me.mohistzh.metrics.sdk.core.reporter;

import lombok.Getter;
import me.mohistzh.metrics.sdk.config.MetricsSdkProperties;
import me.mohistzh.metrics.sdk.core.context.LatencyStatsRegistry;

import java.util.Map;

/**
 * @Author Jonathan
 * @Date 2019/12/25
 **/
public class LatencyReporter {

    @Getter
    private LatencyReportBuilder reportBuilder;

    public LatencyReporter(MetricsSdkProperties properties, LatencyStatsRegistry latencyStatsRegistry, Map<String, String> instanceTags) {

        reportBuilder = new LatencyReportBuilder(latencyStatsRegistry, properties.getTopic(), properties.getKafkaServer(), instanceTags);
    }
}
