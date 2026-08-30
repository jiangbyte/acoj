package github.jiangbyte.io.common.notify.config;

import github.jiangbyte.io.common.notify.NotifyConfigSource;
import github.jiangbyte.io.common.notify.mail.MailSenderFacade;
import github.jiangbyte.io.common.notify.push.PushSenderFacade;
import github.jiangbyte.io.common.notify.sms.SmsSenderFacade;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 通知模块自动配置：注册短信、邮件、推送发送门面。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ConditionalOnBean(NotifyConfigSource.class)
public class NotifyAutoConfiguration {

    /** 注册通知侧邮件发送门面。 */
    @Bean
    @ConditionalOnMissingBean
    public MailSenderFacade mailSenderFacade(NotifyConfigSource configSource) {
        return new MailSenderFacade(configSource);
    }

    /** 注册短信发送门面。 */
    @Bean
    @ConditionalOnMissingBean
    public SmsSenderFacade smsSenderFacade(NotifyConfigSource configSource) {
        return new SmsSenderFacade(configSource);
    }

    /** 注册推送发送门面。 */
    @Bean
    @ConditionalOnMissingBean
    public PushSenderFacade pushSenderFacade(NotifyConfigSource configSource) {
        return new PushSenderFacade(configSource);
    }
}
