package me.mohistzh.metrics.sdk.config;

import lombok.extern.slf4j.Slf4j;
import me.mohistzh.metrics.sdk.filter.HttpMetricsInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * register our custom interceptor
 * @Author Jonathan
 * @Date 2019/12/24
 **/
@Slf4j
public class MetricsWebMvcConfigurerAdapter extends WebMvcConfigurationSupport {

    private final HttpMetricsInterceptor httpMetricsInterceptor;

    public MetricsWebMvcConfigurerAdapter(HttpMetricsInterceptor httpMetricsInterceptor) {
        this.httpMetricsInterceptor = httpMetricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.httpMetricsInterceptor);
        super.addInterceptors(registry);
        log.info("Metrics service has registered!");
    }
}
