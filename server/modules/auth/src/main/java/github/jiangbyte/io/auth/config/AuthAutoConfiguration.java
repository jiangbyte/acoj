package github.jiangbyte.io.auth.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证模块 Spring Boot 自动配置：扫描 {@code github.jiangbyte.io.auth} 包下的组件，
 * 使登录、会话、加解密等 Bean 随 starter 引入自动注册。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.auth")
public class AuthAutoConfiguration {
}
