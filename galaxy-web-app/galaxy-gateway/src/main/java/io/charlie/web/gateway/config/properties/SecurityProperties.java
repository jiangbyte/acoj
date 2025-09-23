package io.charlie.web.gateway.config.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025/4/13
 * @description 白名单配置
 * security:
 * ignore:
 * urls:
 * - /public/**
 * - /static/**
 */
@Data
@Slf4j
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private Ignore ignore;

    @Data
    public static class Ignore {
        private List<String> urls;
    }
}
