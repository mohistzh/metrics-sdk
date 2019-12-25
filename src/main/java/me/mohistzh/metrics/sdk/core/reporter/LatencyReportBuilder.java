package me.mohistzh.metrics.sdk.core.reporter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.mohistzh.metrics.sdk.core.context.LatencyStatsRegistry;
import me.mohistzh.metrics.sdk.model.DataPoint;
import org.HdrHistogram.Histogram;
import org.LatencyUtils.LatencyStats;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Latency reporter data model
 * @Author Jonathan
 * @Date 2019/12/25
 **/
@Slf4j
public class LatencyReportBuilder {

    private final String server;
    private final String topic;
    private final LatencyStatsRegistry registry;
    private KafkaProducer<String,String> producer;
    private final SerializeConfig jsonConfig;
    private final Map<String, String> instanceTags;
    private final ScheduledExecutorService executorService;

    public LatencyReportBuilder(LatencyStatsRegistry registry, String topic, String kafkaServer,
                                Map<String, String> instanceTags) {
        this.registry = registry;
        this.topic = topic;
        this.server = kafkaServer;
        this.instanceTags = instanceTags;

        jsonConfig = new SerializeConfig();
        jsonConfig.put(Double.class, new DoubleSerializer("###.###"));
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().
                setNameFormat("kafka-latency-reporter-d%").build());

    }

    public void start(long period, TimeUnit timeUnit) {
        Map<String, String> properties = new HashMap<>();
        properties.put("bootstrap.servers", String.format("%s", this.server));
        properties.put("acks", "all");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        try {
            producer = new KafkaProducer(properties);
        } catch (Exception e) {
            log.error("Unable connect to kafka server: {}", this.server, e);
            return;
        }

        executorService.scheduleAtFixedRate(this::report, 0, period, timeUnit);

    }

    /**
     * send message to kafka from data points
     */
    private void report() {
        List<DataPoint> dataPoints = dump(registry, instanceTags);

        List<String> jsonList = dataPoints.stream().map(x -> JSON.toJSONString(x, jsonConfig)).collect(Collectors.toList());
        jsonList.forEach(x -> producer.send(new ProducerRecord<>(this.topic, x)));

    }
    private static List<DataPoint> dump(LatencyStatsRegistry registry, Map<String, String> instanceTags) {
        final List<DataPoint> dataPoints = Lists.newArrayList();
        for (String name : registry.getLatencyStatsRegistryNameList()) {
            dataPoints.addAll(sample(name, instanceTags, registry.getLatencyStatsInstance(name)));
        }
        return dataPoints;
    }

    @VisibleForTesting
    static List<DataPoint> sample(String key, Map<String, String> instanceTags, LatencyStats latencyStats) {
        final List<DataPoint> dataPoints = Lists.newArrayList();
        double timestamp = System.currentTimeMillis() / 1000.0;

        Histogram histogram = latencyStats.getIntervalHistogram();

        long count = histogram.getTotalCount();
        long gap = histogram.getEndTimeStamp() - histogram.getStartTimeStamp();
        if (gap == 0) {
            dataPoints.add(new DataPoint(key + ".qps", 0, timestamp, instanceTags));
        } else {
            dataPoints.add(new DataPoint(key + ".qps", count / (gap / 1000d), timestamp, instanceTags));
        }
        dataPoints.add(new DataPoint(key + ".from", histogram.getStartTimeStamp() / 1000d, timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".to", histogram.getEndTimeStamp() / 1000d, timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".count", count, timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".mean", histogram.getMean(), timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".max", count == 0 ? 0 : histogram.getMaxValue(), timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".min", count == 0 ? 0 : histogram.getMinValue(), timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".stddev", histogram.getStdDeviation(), timestamp, instanceTags));
        dataPoints.add(new DataPoint(key + ".p50", histogram.getValueAtPercentile(50d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p75", histogram.getValueAtPercentile(75d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p80", histogram.getValueAtPercentile(80d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p85", histogram.getValueAtPercentile(85d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p90", histogram.getValueAtPercentile(90d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p95", histogram.getValueAtPercentile(95d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p98", histogram.getValueAtPercentile(98d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p99", histogram.getValueAtPercentile(99d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p999", histogram.getValueAtPercentile(99.9d), timestamp,
                instanceTags));
        dataPoints.add(new DataPoint(key + ".p9999", histogram.getValueAtPercentile(99.99d), timestamp,
                instanceTags));
        return dataPoints;
    }
}
