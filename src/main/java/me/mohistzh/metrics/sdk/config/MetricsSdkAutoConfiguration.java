package me.mohistzh.metrics.sdk.config;

import me.mohistzh.metrics.sdk.core.context.LatencyStatsRegistry;
import me.mohistzh.metrics.sdk.core.context.RequestMappingPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * intend to load this sdk automatically
 * @Author Jonathan
 * @Date 2019/12/24
 **/
@Configuration
@EnableConfigurationProperties(MetricsSdkProperties.class)
public class MetricsSdkAutoConfiguration {

    private final Environment env;

    @Autowired
    public MetricsSdkAutoConfiguration(Environment env) {
        this.env = env;
    }


    @Bean
    public LatencyStatsRegistry latencyStatsRegistry() {
        return new LatencyStatsRegistry();
    }

    @Bean
    public RequestMappingPath requestMappingPath() {
        return new RequestMappingPath();
    }




}
