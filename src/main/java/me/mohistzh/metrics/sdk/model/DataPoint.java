package me.mohistzh.metrics.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
/**
 * Metrics data structure
 * @Author Jonathan
 * @Date 2019/12/24
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPoint {

    //name of SLI. e.g. cpu.usage
    private String key;

    //value of SLI. e.g. 0.5
    private Number value;

    //milliseconds format
    private double timestamp;

    //current context information
    private Map<String, String> tags;

}
