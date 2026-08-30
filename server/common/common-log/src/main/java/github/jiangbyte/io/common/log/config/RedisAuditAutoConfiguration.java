package github.jiangbyte.io.common.log.config;

import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.log.audit.AuditEventHandler;
import github.jiangbyte.io.common.log.audit.AuditEventPublisher;
import github.jiangbyte.io.common.log.audit.RedisAuditEventConsumer;
import github.jiangbyte.io.common.log.audit.RedisAuditEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Redis 审计自动配置：注册审计发布器和消费者。
 *
 * Author: Charlie
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnClass({StringRedisTemplate.class, RedisConnectionFactory.class})
@EnableConfigurationProperties(HeiLogProperties.class)
public class RedisAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper auditObjectMapper() {
        // Jackson 3 databind 内置 java.time 支持（JavaTimeInitializer），默认按 ISO-8601 字符串序列化时间，
        // 无需注册 JSR-310 模块，也无 WRITE_DATES_AS_TIMESTAMPS 开关
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventPublisher.class)
    public AuditEventPublisher auditEventPublisher(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            HeiLogProperties heiLogProperties) {
        return new RedisAuditEventPublisher(
                stringRedisTemplate,
                objectMapper,
                heiLogProperties.getAudit());
    }

    @Bean
    @ConditionalOnMissingBean(RedisAuditEventConsumer.class)
    public RedisAuditEventConsumer auditEventConsumer(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            HeiLogProperties heiLogProperties,
            @Autowired(required = false) List<AuditEventHandler> handlers) {
        return new RedisAuditEventConsumer(
                stringRedisTemplate,
                objectMapper,
                heiLogProperties.getAudit(),
                handlers);
    }
}
