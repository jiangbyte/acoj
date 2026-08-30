package github.jiangbyte.io.common.security.config;

import github.jiangbyte.io.common.security.cors.SaTokenCorsSupport;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

/**
 * Sa-Token CORS 自动配置：启动时安装 corsHandle 策略。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(HeiSecurityProperties.class)
public class SaTokenCorsAutoConfiguration {

    private final HeiSecurityProperties securityProperties;
    private final Environment environment;

    public SaTokenCorsAutoConfiguration(HeiSecurityProperties securityProperties, Environment environment) {
        this.securityProperties = securityProperties;
        this.environment = environment;
    }

    /** 安装 Sa-Token CORS 处理策略。 */
    @PostConstruct
    public void installCorsHandle() {
        SaTokenCorsSupport.install(securityProperties, environment);
    }
}
