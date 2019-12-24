package me.mohistzh.metrics.sdk.core.context;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Look up beans
 * @Author Jonathan
 * @Date 2019/12/24
 **/
public class RequestMappingPath implements ApplicationContextAware {

    private static final String REGEX_BRANCH_WITH_NAME = "\\{[a-zA-Z0-9_\\-]+}";
    private static final String REPLACE_WITH_VAR = "VAR";
    private static final String PATTERN_IGNORE_START = "\\Q";
    private static final String PATTERN_IGNORE_END = "\\E";
    private static final String REGEX_BRANCH_WITH_NAME_QUOTED = "\\\\E[a-zA-Z0-9_\\-]+\\\\Q";
    private static final String REGEX_REPLACE_NAME_QUOTED = "\\[a-zA-Z0-9_-]}";
    private static final String REGEX_REPLACE_NAME = "[a-zA-Z0-9]+";

    private ApplicationContext applicationContext;

    private volatile Map<String, Pattern> generifyUrlPatternMap;


    public Set<String> mapping() {
        Set<String> result = Sets.newHashSet();
        extractHandlerMappings(applicationContext, result);
        extractMethodMappings(applicationContext, result);
        String contextPath = ((GenericWebApplicationContext) applicationContext).getServletContext().getContextPath();

        return result.stream().map(x -> contextPath + x).collect(Collectors.toSet());
    }

    private void extractHandlerMappings(ApplicationContext applicationContext, Set<String> result) {
        if (applicationContext != null) {
            Map<String, AbstractUrlHandlerMapping> mappings = applicationContext.getBeansOfType(AbstractUrlHandlerMapping.class);
            for (Map.Entry<String, AbstractUrlHandlerMapping> mappingEntry : mappings.entrySet()) {
                Map<String, Object> handlers = getHandlerMap(mappingEntry.getValue());

                for (Map.Entry<String, Object> handler : handlers.entrySet()) {
                    result.add(handler.getKey());
                }
            }

        }

    }

    @SuppressWarnings("rawtypes")
    private void extractMethodMappings(ApplicationContext applicationContext, Set<String> result) {
        if (applicationContext != null) {
            for (Map.Entry<String, AbstractHandlerMethodMapping> bean : applicationContext.getBeansOfType(AbstractHandlerMethodMapping.class).entrySet()) {
                @SuppressWarnings("unchecked")
                Map<?, HandlerMethod> methodMap = bean.getValue().getHandlerMethods();
                for (Map.Entry<?, HandlerMethod> methodEntry : methodMap.entrySet()) {
                    result.addAll(((RequestMappingInfo)(methodEntry.getKey())).getPatternsCondition().getPatterns());
                }

            }
        }

    }

    private Map<String, Object> getHandlerMap(AbstractUrlHandlerMapping mapping) {
        if (AopUtils.isCglibProxy(mapping)) {
            return Collections.emptyMap();
        }
        return mapping.getHandlerMap();
    }


    private Map<String, Pattern> generifyUrlPatternMapInstance() {
        if (generifyUrlPatternMap == null) {
            synchronized (this) {
                if (generifyUrlPatternMap == null) {
                    generifyUrlPatternMap = Maps.newHashMap();
                    for (String urlMap : mapping()) {
                        String quotedUrlMap = PATTERN_IGNORE_START + urlMap.replaceAll(REGEX_BRANCH_WITH_NAME, REGEX_BRANCH_WITH_NAME_QUOTED) + PATTERN_IGNORE_END;
                        Pattern pattern = Pattern.compile(quotedUrlMap.replaceAll(REGEX_REPLACE_NAME_QUOTED, REGEX_REPLACE_NAME));
                        generifyUrlPatternMap.put(urlMap, pattern);
                    }
                }
            }
        }
        return generifyUrlPatternMap;
    }

    public Optional<String> generifyUrl(String url) {
        for (String urlMap : mapping()) {
            Matcher matcher;
            matcher = generifyUrlPatternMapInstance().get(urlMap).matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return Optional.of(urlMap.replaceAll(REGEX_BRANCH_WITH_NAME, REPLACE_WITH_VAR));
        }
        return Optional.empty();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
