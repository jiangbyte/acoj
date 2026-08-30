package github.jiangbyte.io.profile.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户模块 Spring Boot 自动配置：扫描 {@code github.jiangbyte.io.profile} 包下的组件，
 * 使管理端/门户端个人资料、用户中心 API 与跨模块 Provider 随 starter 引入自动注册。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.profile")
public class ProfileAutoConfiguration {
}
