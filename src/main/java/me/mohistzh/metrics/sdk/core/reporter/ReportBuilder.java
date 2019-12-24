package me.mohistzh.metrics.sdk.core.reporter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.codahale.metrics.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.mohistzh.metrics.sdk.core.context.MetricsDumpper;
import me.mohistzh.metrics.sdk.model.DataPoint;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A report builder which involved kafka data stream generify.
 * @Author Jonathan
 * @Date 2019/12/24
 **/
@Slf4j
public class ReportBuilder extends ScheduledReporter {


    private static final String KAFKA_REPORTER = "kafka-reporter";
    private final String kafkaServer;
    private final String topic;
    private KafkaProducer<String, String> producer;
    private final SerializeConfig jsonConfig;
    private final Map<String, String> instanceTags;

    /**
     * initial objects
     * @param registry
     * @param filter
     * @param rateUnit
     * @param durationUnit
     * @param topic
     * @param kafkaServer
     * @param instanceTags
     */
    public ReportBuilder(MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
                         String topic, String kafkaServer, Map<String, String> instanceTags) {
        super(registry, KAFKA_REPORTER, filter, rateUnit, durationUnit);
        this.topic = topic;
        this.kafkaServer = kafkaServer;
        this.instanceTags = instanceTags;

        jsonConfig = new SerializeConfig();
        jsonConfig.put(Double.class, new DoubleSerializer("###.###"));
    }

    /**
     * create a connection with kafka server
     * @param period
     * @param unit
     */
    @Override
    public void start(long period, TimeUnit unit) {
        Map<String, String> properties = new HashMap<>();
        properties.put("bootstrap.servers", String.format("%s", this.kafkaServer));
        properties.put("acks", "all");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        try {
            producer = new KafkaProducer(properties);
        } catch (Exception e) {
            log.error("Unable connect to kafka server: {}", this.kafkaServer, e);
            return;
        }
        super.start(period, unit);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        final List<DataPoint> dataPoints = Lists.newArrayList();
        /**
         * fill data-points case by case
         */
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            DataPoint dataPoint = MetricsDumpper.dumps(entry.getKey(), entry.getValue(), this.instanceTags);
            if (dataPoint == null) {
                break;
            }
            dataPoints.add(dataPoint);
        }
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            dataPoints.add(MetricsDumpper.dumps(entry.getKey(), entry.getValue(), this.instanceTags));
        }
        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            dataPoints.addAll(MetricsDumpper.dumps(entry.getKey(), entry.getValue(), this.instanceTags));
        }
        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            dataPoints.addAll(MetricsDumpper.dumps(entry.getKey(), entry.getValue(), this.instanceTags));
        }
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            dataPoints.addAll(MetricsDumpper.dumps(entry.getKey(), entry.getValue(), this.instanceTags));
        }

        List<String> jsonList =  dataPoints.stream().map(x -> JSON.toJSONString(x, jsonConfig)).collect(Collectors.toList());
        jsonList.forEach(x -> producer.send(new ProducerRecord<>(this.topic, x)));

    }
}
