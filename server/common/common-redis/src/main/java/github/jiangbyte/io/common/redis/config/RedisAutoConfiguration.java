package github.jiangbyte.io.common.redis.config;

import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Redis 自动配置：定制 Redisson 连接池 / 超时等默认值。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(HeiRedisProperties.class)
public class RedisAutoConfiguration {

    /**
     * 为单机 Redisson 填充连接池 / 超时 / keepalive 默认值。
     * 显式 {@code spring.redis.redisson.config} YAML 仍优先做完整覆盖；
     * 本 customizer 仅在单机模式激活时补齐合理默认。
     */
    @Bean
    public RedissonAutoConfigurationCustomizer heiRedissonCustomizer(
            HeiRedisProperties properties,
            Environment environment) {
        return config -> {
            config.setThreads(properties.getThreads());
            config.setNettyThreads(properties.getNettyThreads());
            config.setTcpKeepAlive(properties.isKeepAlive());
            config.setTcpNoDelay(properties.isTcpNoDelay());

            if (!config.isSingleConfig()) {
                return;
            }
            SingleServerConfig single = config.useSingleServer();
            single.setTimeout(properties.getTimeout());
            single.setConnectTimeout(properties.getConnectTimeout());
            single.setIdleConnectionTimeout(properties.getIdleConnectionTimeout());
            single.setConnectionPoolSize(properties.getConnectionPoolSize());
            single.setConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize());
            single.setSubscriptionConnectionPoolSize(properties.getSubscriptionConnectionPoolSize());
            single.setSubscriptionConnectionMinimumIdleSize(properties.getSubscriptionConnectionMinimumIdleSize());
            single.setPingConnectionInterval(properties.getPingConnectionInterval());

            if (!StringUtils.hasText(single.getClientName())) {
                String appName = environment.getProperty("spring.application.name");
                if (StringUtils.hasText(appName)) {
                    single.setClientName(appName);
                }
            }
        };
    }
}
