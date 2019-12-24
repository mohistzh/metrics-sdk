package me.mohistzh.metrics.sdk.core.context;


import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Meter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import me.mohistzh.metrics.sdk.model.DataPoint;

import java.util.List;
import java.util.Map;

/**
 * Compose various metrics data points
 * @Author Jonathan
 * @Date 2019/12/24
 **/
public class MetricsDumpper {

    private MetricsDumpper(){}

    /**
     * Dump by gauge which a gauge is an instantaneous measurement of a value.
     * @param key
     * @param gauge
     * @param tags
     * @return
     */
    public static DataPoint dumps(String key, Gauge<?> gauge, Map<String, String> tags) {
        Double value = Doubles.tryParse(gauge.getValue().toString());
        if (value == null) {
            return null;
        }

        DataPoint dataPoint = new DataPoint();
        dataPoint.setKey(key);
        dataPoint.setValue(value);
        dataPoint.setTags(tags);
        //remain second
        dataPoint.setTimestamp(System.currentTimeMillis() / 1000.0);
        return dataPoint;
    }

    /**
     * Dump by counter which a counter is just a gauge for an AtomicLong instance. You can increment or decrement its value.
     * @param key
     * @param counter
     * @param tags
     * @return
     */
    public static DataPoint dumps(String key, Counter counter, Map<String, String> tags) {
        DataPoint dataPoint = new DataPoint();
        dataPoint.setKey(key);
        dataPoint.setValue(counter.getCount());
        dataPoint.setTimestamp(System.currentTimeMillis() / 1000.0);
        dataPoint.setTags(tags);
        return dataPoint;
    }

    /**
     * Dump by histogram which a histogram measures the statistical distribution of values in a stream of data.
     * In addition to minimum, maximum, mean, etc., it also measures median, 75th, 90th, 95th, 98th, 99th, and 99.9th percentiles.
     * @param key
     * @param histogram
     * @param tags
     * @return
     */
    public static List<DataPoint> dumps(String key, Histogram histogram, Map<String, String> tags) {
        List<DataPoint> dataPoints = Lists.newArrayList();
        final Snapshot snapshot = histogram.getSnapshot();
        double timestamp = System.currentTimeMillis() / 1000.0;
        dataPoints.add(new DataPoint(key + ".count", histogram.getCount(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".max", snapshot.getMax(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".mean", snapshot.getMean(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".min", snapshot.getMin(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p50", snapshot.getMedian(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p75", snapshot.get75thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p95", snapshot.get95thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p98", snapshot.get98thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p99", snapshot.get99thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p999", snapshot.get999thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".stddev", snapshot.getStdDev(), timestamp, tags));
        return dataPoints;
    }

    /**
     * Dump by timer which a timer measures both the rate that a particular piece of code is called and the distribution of its duration.
     * @param key
     * @param timer
     * @param tags
     * @return
     */
    public static List<DataPoint> dumps(String key, Timer timer, Map<String, String> tags) {
        List<DataPoint> dataPoints = Lists.newArrayList();
        final Snapshot snapshot = timer.getSnapshot();
        double timestamp = System.currentTimeMillis() / 1000.0;
        dataPoints.add(new DataPoint(key + ".count", timer.getCount(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".max", snapshot.getMax(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".mean", snapshot.getMean(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".min", snapshot.getMin(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p50", snapshot.getMedian(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p75", snapshot.get75thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p95", snapshot.get95thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p98", snapshot.get98thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p99", snapshot.get99thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".p999", snapshot.get999thPercentile(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".stddev", snapshot.getStdDev(), timestamp, tags));

        dataPoints.add(new DataPoint(key + ".stddev", snapshot.getStdDev(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m15_rate", timer.getFifteenMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m1_rate", timer.getOneMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m5_rate", timer.getFiveMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".mean_rate", timer.getMeanRate(), timestamp, tags));
        return dataPoints;
    }

    /**
     * Dump by meter which a meter measures the rate of events over time (e.g., “requests per second”).
     * @param key
     * @param meter
     * @param tags
     * @return
     */
    public static List<DataPoint> dumps(String key, Meter meter, Map<String, String> tags) {
        List<DataPoint> dataPoints = Lists.newArrayList();
        double timestamp = System.currentTimeMillis() / 1000.0;
        dataPoints.add(new DataPoint(key + ".count", meter.getCount(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m15_rate", meter.getFifteenMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m1_rate", meter.getOneMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".m5_rate", meter.getFiveMinuteRate(), timestamp, tags));
        dataPoints.add(new DataPoint(key + ".mean_rate", meter.getMeanRate(), timestamp, tags));
        return dataPoints;
    }




}
