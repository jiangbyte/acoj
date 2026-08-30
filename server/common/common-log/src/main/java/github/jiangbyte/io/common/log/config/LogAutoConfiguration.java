package github.jiangbyte.io.common.log.config;

import github.jiangbyte.io.common.log.audit.AuditEventPublisher;
import github.jiangbyte.io.common.log.audit.AuditOutboxWriter;
import github.jiangbyte.io.common.log.aspect.OperationAuditAspect;
import github.jiangbyte.io.common.log.format.HeiStructuredLoggingJsonMembersCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 日志模块自动配置：注册操作审计切面与 JSON 结构化日志成员定制器。
 *
 * Author: Charlie
 */
@AutoConfiguration
@AutoConfigureAfter(RedisAuditAutoConfiguration.class)
@EnableConfigurationProperties(HeiLoggingProperties.class)
public class LogAutoConfiguration {

    /** 注册操作审计切面。 */
    @Bean
    @ConditionalOnBean(AuditEventPublisher.class)
    public OperationAuditAspect operationAuditAspect(
            AuditEventPublisher auditEventPublisher,
            ObjectProvider<AuditOutboxWriter> auditOutboxWriter) {
        return new OperationAuditAspect(auditEventPublisher, auditOutboxWriter);
    }

    /** 注册 JSON 结构化日志成员定制器。 */
    @Bean
    @ConditionalOnMissingBean(StructuredLoggingJsonMembersCustomizer.class)
    public StructuredLoggingJsonMembersCustomizer<?> heiStructuredLoggingJsonMembersCustomizer(
            Environment environment) {
        return new HeiStructuredLoggingJsonMembersCustomizer(environment);
    }
}
