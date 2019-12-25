package me.mohistzh.metrics.sdk.core.reporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import lombok.Getter;
import me.mohistzh.metrics.sdk.config.MetricsSdkProperties;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Metrics Reporter Constructor
 * @Author Jonathan
 * @Date 2019/12/24
 **/
public class MetricsReporter {

    @Getter
    private ReportBuilder reporter;

    public MetricsReporter(MetricsSdkProperties properties, MetricRegistry metricRegistry, Map<String, String> instanceTags) {
        reporter = new ReportBuilder(metricRegistry, MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
                properties.getTopic(), properties.getServer(), instanceTags);
    }

}
