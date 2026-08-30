package github.jiangbyte.io.sys.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 系统模块 Spring Boot 自动配置：扫描 sys 包下组件。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.sys")
public class SysAutoConfiguration {
}
