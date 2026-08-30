package github.jiangbyte.io.iam.modules.account.support;

import github.jiangbyte.io.common.notify.mail.MailSenderFacade;
import github.jiangbyte.io.common.notify.sms.SmsSenderFacade;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.sys.config.ConfigAppNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 账号生命周期通知：取消/清理时按场景发送邮件或短信。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class AccountLifecycleNotifier {

    private static final Logger log = LoggerFactory.getLogger(AccountLifecycleNotifier.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final MailSenderFacade mailSenderFacade;
    private final SmsSenderFacade smsSenderFacade;
    private final ConfigApi configApi;

    /**
     * 发送账号取消通知。
     * @param email 邮箱
     * @param phone 手机
     * @param cancelledAt 取消时间
     */
    public void notifyCancelled(String email, String phone, OffsetDateTime cancelledAt) {
        int retentionDays = configApi.getInt("ACCOUNT_CANCEL_RETENTION_DAYS", 15);
        OffsetDateTime purgeAt = cancelledAt == null
                ? OffsetDateTime.now().plusDays(retentionDays)
                : cancelledAt.plusDays(retentionDays);
        Map<String, Object> vars = baseVars();
        vars.put("retention_days", retentionDays);
        vars.put("purge_at", FMT.format(purgeAt));
    /** 按场景发送邮件。 */
        sendMail(email, "ACCOUNT_CANCELLED", vars);
    /** 按场景发送短信。 */
        sendSms(phone, "ACCOUNT_CANCELLED", vars);
    }

    /** 发送账号清理通知。 */
    public void notifyPurged(String email, String phone, OffsetDateTime purgedAt) {
        Map<String, Object> vars = baseVars();
        vars.put("purged_at", FMT.format(purgedAt == null ? OffsetDateTime.now() : purgedAt));
    /** 按场景发送邮件。 */
        sendMail(email, "ACCOUNT_PURGED", vars);
    /** 按场景发送短信。 */
        sendSms(phone, "ACCOUNT_PURGED", vars);
    }

    /** 构建通知模板基础变量。 */
    private Map<String, Object> baseVars() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("app_name", ConfigAppNames.resolve(configApi));
        return vars;
    }

    /** 按场景发送邮件。 */
    private void sendMail(String email, String scene, Map<String, Object> vars) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        try {
            mailSenderFacade.sendTemplated(scene, email.trim(), vars);
        } catch (Exception ex) {
            log.warn("Lifecycle mail {} failed: {}", scene, ex.getMessage());
        }
    }

    /** 按场景发送短信。 */
    private void sendSms(String phone, String scene, Map<String, Object> vars) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        String key = "SMS_TEMPLATE_" + scene;
        String raw = configApi.getValue(key, "");
        if (!StringUtils.hasText(raw) || !raw.contains("\"code\"") || raw.contains("\"code\": \"\"")
                || raw.contains("\"code\":\"\"")) {
            return;
        }
        try {
            smsSenderFacade.sendTemplated(scene, phone.trim(), vars);
        } catch (Exception ex) {
            log.warn("Lifecycle SMS {} failed: {}", scene, ex.getMessage());
        }
    }
}
