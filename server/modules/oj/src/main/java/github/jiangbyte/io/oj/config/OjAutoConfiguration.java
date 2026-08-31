package github.jiangbyte.io.oj.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OJ 模块 Spring Boot 自动配置：扫描 {@code github.jiangbyte.io.oj} 包下组件。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.oj")
@EnableConfigurationProperties(OjProperties.class)
@EnableScheduling
public class OjAutoConfiguration {
}
