package github.jiangbyte.io.workspace.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 仪表盘模块 Spring Boot 自动配置：扫描 {@code github.jiangbyte.io.workspace} 包下组件，使管理端总览统计 API 随 starter 引入自动注册。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.workspace")
public class WorkspaceAutoConfiguration {
}
