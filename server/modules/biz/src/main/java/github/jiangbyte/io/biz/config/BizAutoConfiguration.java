package github.jiangbyte.io.biz.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 业务模块 Spring Boot 自动配置：扫描 {@code github.jiangbyte.io.biz} 包下组件，使测试活动/目录/知识库/订单等业务 API 随 starter 引入自动注册。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.biz")
public class BizAutoConfiguration {
}
