package github.jiangbyte.io.common.log.format;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * 为每条 JSON 日志补充与 fastapi 对齐的 service 上下文字段。
 *
 * Author: Charlie
 */
public class HeiStructuredLoggingJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {

    private final Environment environment;

    public HeiStructuredLoggingJsonMembersCustomizer(Environment environment) {
        this.environment = environment;
    }

    /** 向 JSON 日志成员追加 service 上下文字段。 */
    @Override
    public void customize(JsonWriter.Members<Object> members) {
        String service = environment.getProperty("hei.logging.service");
        if (!StringUtils.hasText(service)) {
            service = environment.getProperty("spring.application.name", "hei-boot");
        }
        String version = environment.getProperty("hei.logging.service-version", "");
        String[] profiles = environment.getActiveProfiles();
        String environmentName = profiles.length > 0
                ? profiles[0]
                : environment.getProperty("spring.profiles.active", "default");

        members.add("service", service);
        members.add("service_version", version);
        members.add("environment", environmentName);
    }
}
