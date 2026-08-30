package github.jiangbyte.io.common.security.config;

import github.jiangbyte.io.common.security.ratelimit.RateLimitAspect;
import github.jiangbyte.io.common.security.ratelimit.RateLimitProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 限流自动配置。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "hei.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(
            RedissonClient redissonClient,
            RateLimitProperties properties,
            HeiSecurityProperties securityProperties) {
        return new RateLimitAspect(redissonClient, properties, securityProperties);
    }
}
