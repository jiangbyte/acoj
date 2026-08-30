package github.jiangbyte.io.common.doc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * OpenAPI / Knife4j 文档自动配置：注册基础 OpenAPI Info（标题、版本与描述）。
 *
 * Author: Charlie
 */
@AutoConfiguration
public class DocAutoConfiguration {

    /** 注册基础 OpenAPI 文档信息。 */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("HEI Boot API")
                .version("0.1.0")
                .description("Backend API template. JSON fields use snake_case."));
    }
}
