package me.mohistzh.metrics.sdk.core.context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.LatencyUtils.LatencyStats;

import java.util.Map;
import java.util.List;

/**
 *
 * @Author Jonathan
 * @Date 2019/12/24
 **/
public class LatencyStatsRegistry {

    private Map<String, LatencyStats> latencyStatsMap = Maps.newConcurrentMap();


    public List<String> getLatencyStatsRegistryNameList() {
        return Lists.newArrayList(latencyStatsMap.keySet());
    }

    public LatencyStats getLatencyStatsInstance(String name) {
        if (!latencyStatsMap.containsKey(name)) {
            synchronized (LatencyStatsRegistry.class) {
                if (!latencyStatsMap.containsKey(name)) {
                    latencyStatsMap.put(name, new LatencyStats());
                }
            }
        }
        return latencyStatsMap.get(name);
    }
}
