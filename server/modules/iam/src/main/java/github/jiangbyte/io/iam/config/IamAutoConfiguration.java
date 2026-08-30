package github.jiangbyte.io.iam.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * IAM 模块 Spring Boot 自动配置：扫描 github.jiangbyte.io.iam 包下组件。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.iam")
public class IamAutoConfiguration {
}
