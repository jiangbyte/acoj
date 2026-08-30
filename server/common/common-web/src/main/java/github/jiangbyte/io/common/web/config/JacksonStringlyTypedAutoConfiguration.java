package github.jiangbyte.io.common.web.config;

import github.jiangbyte.io.common.web.jackson.SensitiveNameJacksonModule;
import github.jiangbyte.io.common.web.jackson.StringlyTypedJacksonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * Jackson 字符串友好反序列化与 {@code @Sensitive} 响应脱敏自动配置。
 *
 * Author: Charlie
 */
@AutoConfiguration
public class JacksonStringlyTypedAutoConfiguration {

    /** 注册字符串友好 Jackson Module。 */
    @Bean
    @Order(0)
    public JsonMapperBuilderCustomizer heiStringlyTypedJacksonCustomizer() {
        return builder -> builder.addModule(new StringlyTypedJacksonModule());
    }

    /** 仅序列化标注了 {@code @Sensitive} 的属性为掩码。 */
    @Bean
    @Order(1)
    public JsonMapperBuilderCustomizer heiSensitiveAnnotationJacksonCustomizer() {
        return builder -> builder.addModule(new SensitiveNameJacksonModule());
    }
}
