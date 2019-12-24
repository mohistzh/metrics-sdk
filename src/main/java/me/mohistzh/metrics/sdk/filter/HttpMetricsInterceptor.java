package me.mohistzh.metrics.sdk.filter;

import com.codahale.metrics.MetricRegistry;
import me.mohistzh.metrics.sdk.core.context.LatencyStatsRegistry;
import me.mohistzh.metrics.sdk.core.context.RequestMappingPath;
import org.LatencyUtils.LatencyStats;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * HTTP layer metrics
 * @Author Jonathan
 * @Date 2019/12/24
 **/
public class HttpMetricsInterceptor extends HandlerInterceptorAdapter {


    private static final String TIMER_HTTP_SERVER_PREFIX = "timer.http.server";
    private static final String UNKNOWN = "UNKNOWN";
    private final LatencyStatsRegistry latencyStatsRegistry;
    private final RequestMappingPath requestMappingPath;
    private final ThreadLocal<Long> startTimeMillis = new ThreadLocal<>();
    private final ThreadLocal<LatencyStats> timer = new ThreadLocal<>();

    public HttpMetricsInterceptor(LatencyStatsRegistry latencyStatsRegistry, RequestMappingPath requestMappingPath) {
        this.latencyStatsRegistry = latencyStatsRegistry;
        this.requestMappingPath = requestMappingPath;
    }

    /**
     * Recording time of request
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<String> generifiedPath = requestMappingPath.generifyUrl((request).getRequestURI());
        String path = MetricRegistry.name(TIMER_HTTP_SERVER_PREFIX, (request).getMethod().toLowerCase(), generifiedPath.orElse(UNKNOWN));
        timer.set(latencyStatsRegistry.getLatencyStatsInstance(path));
        startTimeMillis.set(System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        timer.get().recordLatency(System.nanoTime() - startTimeMillis.get());
        timer.remove();
        startTimeMillis.remove();
    }
}
