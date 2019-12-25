package me.mohistzh.metrics.sdk.custom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder for custom metrics data tags.
 * @Author Jonathan
 * @Date 2019/12/25
 **/
@Slf4j
public class CustomMetricsTagsBuilder {

    @Getter
    private Map<String, String> tags;

    CustomMetricsTagsBuilder(Map<String, String> tags) {
        this.tags = tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, String> tags;

        public Builder add(String key, String value) {
            if (tags == null) {
                tags = new HashMap<>();
            }

            if (key == null) {
                return this;
            }

            if (value == null || StringUtils.isEmpty(value) || value.equals(" ")) {
                log.error("DataPoint value can not be empty, and not permit space. Refer key: " + key);
                return this;
            }
            tags.putIfAbsent(key, value);
            return this;
        }

        public Map<String, String> build() {
            return new CustomMetricsTagsBuilder(tags).getTags();
        }
    }
}
