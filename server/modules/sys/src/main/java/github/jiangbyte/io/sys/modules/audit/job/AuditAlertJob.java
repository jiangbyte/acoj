package github.jiangbyte.io.sys.modules.audit.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.common.notify.mail.MailSenderFacade;
import github.jiangbyte.io.common.notify.push.PushSenderFacade;
import github.jiangbyte.io.sys.config.RuntimeSettings;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import github.jiangbyte.io.sys.modules.audit.entity.SysAlertLog;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditLog;
import github.jiangbyte.io.sys.modules.audit.mapper.SysAlertLogMapper;
import github.jiangbyte.io.sys.modules.audit.mapper.SysOperationAuditLogMapper;
import github.jiangbyte.io.sys.modules.config.support.AuditAlertTestSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审计告警定时任务：按配置规则扫描并发送告警。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditAlertJob implements JobHandler {

    private static final String RULE_BRUTE_FORCE = "audit_volume";
    private static final String RULE_UNUSUAL_HOURS = "unusual_hours";
    private static final String RULE_SENSITIVE_OPS = "sensitive_ops";
    private static final String RULE_BULK_DELETE = "bulk_delete";
    private static final String RULE_IP_ANOMALY = "ip_anomaly";

    /** 凌晨 0-6 点的角色/权限变更等敏感动作。 */
    private static final List<String> SENSITIVE_ACTIONS = List.of(
            "role_create", "role_grant", "permission_change", "permission_grant");

    private final SysOperationAuditLogMapper auditLogMapper;
    private final SysAlertLogMapper alertLogMapper;
    private final PushSenderFacade pushSenderFacade;
    private final MailSenderFacade mailSenderFacade;
    private final AuditAlertTestSender auditAlertTestSender;

    @Override
    public String execute(String params) {
        RuntimeSettings settings = RuntimeSettingsHolder.get();
        if (!settings.getBoolean("AUDIT_ALERT_ENABLED", true)) {
            log.info("Audit alert disabled via AUDIT_ALERT_ENABLED=false");
            return "disabled";
        }

        int fired = 0;
        if (settings.getBoolean("AUDIT_ALERT_RULE_BRUTE_FORCE", true)) {
            if (evaluateBruteForce(settings)) {
                fired++;
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_RULE_UNUSUAL_HOURS", true)) {
            if (evaluateUnusualHours(settings)) {
                fired++;
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_RULE_SENSITIVE_OPS", true)) {
            if (evaluateSensitiveOps(settings)) {
                fired++;
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_RULE_BULK_DELETE", true)) {
            if (evaluateBulkDelete(settings)) {
                fired++;
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_RULE_IP_ANOMALY", true)) {
            if (evaluateIpAnomaly(settings)) {
                fired++;
            }
        }

        return "done fired=" + fired;
    }

    /**
     * 暴力破解近似检测：分析窗口内审计日志总量超过阈值则告警。
     *
     * @return 是否实际发出告警
     */
    private boolean evaluateBruteForce(RuntimeSettings settings) {
        int windowSeconds = Math.max(60, settings.getInt("AUDIT_ALERT_ANALYSIS_INTERVAL_SECONDS", 60));
        long threshold = Math.max(1L, settings.getLong("AUDIT_ALERT_BRUTE_FORCE_THRESHOLD", 10));
        OffsetDateTime since = OffsetDateTime.now().minusSeconds(windowSeconds);
        Long count = auditLogMapper.selectCount(Wrappers.<SysOperationAuditLog>lambdaQuery()
                .ge(SysOperationAuditLog::getCreatedAt, since));
        long volume = count == null ? 0L : count;
        log.info("Audit volume in last {}s: {}, threshold={}", windowSeconds, volume, threshold);
        if (volume < threshold) {
            return false;
        }

        String summary = "Audit log volume " + volume + " exceeded threshold " + threshold
                + " in last " + windowSeconds + " seconds";
        Map<String, Object> details = new HashMap<>();
        details.put("volume", volume);
        details.put("threshold", threshold);
        details.put("window_seconds", windowSeconds);
        details.put("window_minutes", Math.max(1, windowSeconds / 60));
        details.put("since", since.toString());

        int cooldownSeconds = Math.max(
                windowSeconds,
                settings.getInt("AUDIT_ALERT_ALERT_COOLDOWN_SECONDS", 1800));
        return fireAlert(settings, RULE_BRUTE_FORCE, "WARNING", summary, details, cooldownSeconds);
    }

    /**
     * 非常时段检测：凌晨 0-6 点出现角色/权限变更等敏感操作。
     *
     * @return 是否实际发出告警
     */
    private boolean evaluateUnusualHours(RuntimeSettings settings) {
        OffsetDateTime now = OffsetDateTime.now();
        if (now.getHour() > 5) {
            return false;
        }
        OffsetDateTime since = now.minusHours(1);
        List<SysOperationAuditLog> logs = auditLogMapper.selectList(Wrappers.<SysOperationAuditLog>lambdaQuery()
                .ge(SysOperationAuditLog::getCreatedAt, since)
                .in(SysOperationAuditLog::getAction, SENSITIVE_ACTIONS));
        if (logs.isEmpty()) {
            return false;
        }
        Set<String> actions = logs.stream()
                .map(SysOperationAuditLog::getAction)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Object> details = new HashMap<>();
        details.put("count", logs.size());
        details.put("actions", actions);
        String summary = "凌晨 " + now.getHour() + " 时检测到 " + logs.size() + " 次敏感操作";
        return fireAlert(settings, RULE_UNUSUAL_HOURS, "WARNING", summary, details, cooldownSeconds(settings));
    }

    /**
     * 敏感操作检测：5 分钟内角色授权/权限变更等敏感操作。
     *
     * @return 是否实际发出告警
     */
    private boolean evaluateSensitiveOps(RuntimeSettings settings) {
        OffsetDateTime since = OffsetDateTime.now().minusSeconds(300);
        List<SysOperationAuditLog> logs = auditLogMapper.selectList(Wrappers.<SysOperationAuditLog>lambdaQuery()
                .ge(SysOperationAuditLog::getCreatedAt, since)
                .in(SysOperationAuditLog::getAction,
                        List.of("role_grant", "permission_change", "permission_grant")));
        if (logs.isEmpty()) {
            return false;
        }
        Map<String, Long> byAccount = logs.stream()
                .filter(l -> StringUtils.hasText(l.getAccountId()))
                .collect(Collectors.groupingBy(SysOperationAuditLog::getAccountId, Collectors.counting()));
        boolean fired = false;
        for (Map.Entry<String, Long> entry : byAccount.entrySet()) {
            Map<String, Object> details = new HashMap<>();
            details.put("account_id", entry.getKey());
            details.put("count", entry.getValue());
            String summary = "账户 " + entry.getKey() + " 执行了敏感操作 (" + entry.getValue() + " 次)";
            fired |= fireAlert(settings, RULE_SENSITIVE_OPS, "WARNING", summary, details, cooldownSeconds(settings));
        }
        return fired;
    }

    /**
     * 批量删除检测：同账户 5 分钟内删除操作达到阈值。
     *
     * @return 是否实际发出告警
     */
    private boolean evaluateBulkDelete(RuntimeSettings settings) {
        long threshold = Math.max(1L, settings.getLong("AUDIT_ALERT_BULK_DELETE_THRESHOLD", 20));
        int windowSeconds = Math.max(60, settings.getInt("AUDIT_ALERT_BULK_DELETE_WINDOW_SECONDS", 300));
        OffsetDateTime since = OffsetDateTime.now().minusSeconds(windowSeconds);
        List<SysOperationAuditLog> logs = auditLogMapper.selectList(Wrappers.<SysOperationAuditLog>lambdaQuery()
                .ge(SysOperationAuditLog::getCreatedAt, since)
                .eq(SysOperationAuditLog::getAction, "delete"));
        Map<String, Long> byAccount = logs.stream()
                .filter(l -> StringUtils.hasText(l.getAccountId()))
                .collect(Collectors.groupingBy(SysOperationAuditLog::getAccountId, Collectors.counting()));
        boolean fired = false;
        for (Map.Entry<String, Long> entry : byAccount.entrySet()) {
            if (entry.getValue() < threshold) {
                continue;
            }
            Map<String, Object> details = new HashMap<>();
            details.put("account_id", entry.getKey());
            details.put("count", entry.getValue());
            details.put("threshold", threshold);
            details.put("window_seconds", windowSeconds);
            String summary = "账户 " + entry.getKey() + " 在 " + Math.max(1, windowSeconds / 60) + " 分钟内删除了 " + entry.getValue() + " 条记录";
            fired |= fireAlert(settings, RULE_BULK_DELETE, "WARNING", summary, details, cooldownSeconds(settings));
        }
        return fired;
    }

    /**
     * 异地 IP 检测：同账户 15 分钟内从多个不同 IP 成功登录达到阈值。
     *
     * @return 是否实际发出告警
     */
    private boolean evaluateIpAnomaly(RuntimeSettings settings) {
        long threshold = Math.max(1L, settings.getLong("AUDIT_ALERT_IP_ANOMALY_THRESHOLD", 3));
        int windowSeconds = Math.max(60, settings.getInt("AUDIT_ALERT_IP_ANOMALY_WINDOW_SECONDS", 900));
        OffsetDateTime since = OffsetDateTime.now().minusSeconds(windowSeconds);
        List<SysOperationAuditLog> logs = auditLogMapper.selectList(Wrappers.<SysOperationAuditLog>lambdaQuery()
                .ge(SysOperationAuditLog::getCreatedAt, since)
                .eq(SysOperationAuditLog::getAction, "login")
                .eq(SysOperationAuditLog::getSuccess, true)
                .isNotNull(SysOperationAuditLog::getAccountId));
        Map<String, Set<String>> ipsByAccount = new HashMap<>();
        for (SysOperationAuditLog log : logs) {
            String ip = log.getIp() == null ? "" : log.getIp();
            ipsByAccount.computeIfAbsent(log.getAccountId(), k -> new HashSet<>()).add(ip);
        }
        boolean fired = false;
        for (Map.Entry<String, Set<String>> entry : ipsByAccount.entrySet()) {
            if (entry.getValue().size() < threshold) {
                continue;
            }
            Map<String, Object> details = new HashMap<>();
            details.put("account_id", entry.getKey());
            details.put("ip_count", entry.getValue().size());
            details.put("threshold", threshold);
            details.put("window_seconds", windowSeconds);
            String summary = "账户 " + entry.getKey() + " 在 " + Math.max(1, windowSeconds / 60) + " 分钟内从 "
                    + entry.getValue().size() + " 个不同 IP 登录";
            fired |= fireAlert(settings, RULE_IP_ANOMALY, "WARNING", summary, details, cooldownSeconds(settings));
        }
        return fired;
    }

    private static int cooldownSeconds(RuntimeSettings settings) {
        return Math.max(60, settings.getInt("AUDIT_ALERT_ALERT_COOLDOWN_SECONDS", 1800));
    }

    /**
     * 公共告警出口：冷却期抑制 → 通知渠道 → 写入 sys_alert_log。
     *
     * @param cooldownSeconds 冷却窗口（秒），同规则在此窗口内只告警一次
     * @return 是否实际发出告警
     */
    private boolean fireAlert(
            RuntimeSettings settings,
            String ruleName,
            String severity,
            String summary,
            Map<String, Object> details,
            int cooldownSeconds) {
        OffsetDateTime cooldownSince = OffsetDateTime.now().minusSeconds(cooldownSeconds);
        Long recentAlerts = alertLogMapper.selectCount(Wrappers.<SysAlertLog>lambdaQuery()
                .eq(SysAlertLog::getRuleName, ruleName)
                .ge(SysAlertLog::getCreatedAt, cooldownSince));
        if (recentAlerts != null && recentAlerts > 0) {
            log.info(
                    "Audit alert suppressed: rule={} cooldown={}s recent={}",
                    ruleName,
                    cooldownSeconds,
                    recentAlerts);
            return false;
        }

        List<String> notified = notifyChannels(settings, summary);

        SysAlertLog alert = new SysAlertLog();
        alert.setRuleName(ruleName);
        alert.setSeverity(severity);
        alert.setSummary(summary);
        alert.setDetails(details);
        alert.setNotifiedVia(notified.isEmpty() ? "sys_alert_log" : String.join(",", notified));
        alert.setCreatedAt(OffsetDateTime.now());
        alertLogMapper.insert(alert);
        log.info("Wrote sys_alert_log rule={} id={}", ruleName, alert.getId());
        return true;
    }

    private List<String> notifyChannels(RuntimeSettings settings, String summary) {
        List<String> notified = new ArrayList<>();
        if (settings.getBoolean("AUDIT_ALERT_NOTIFY_PUSH", true)) {
            try {
                pushSenderFacade.send("审计告警", summary);
                notified.add("push");
            } catch (Exception ex) {
                log.info("Push notify failed: {}", ex.getMessage());
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_NOTIFY_EMAIL", true)) {
            String to = settings.get("AUDIT_ALERT_NOTIFY_EMAIL_TO", "").trim();
            if (StringUtils.hasText(to)) {
                try {
                    mailSenderFacade.send(to, "审计告警", summary);
                    notified.add("email");
                } catch (Exception ex) {
                    log.info("Email notify failed: {}", ex.getMessage());
                }
            } else {
                log.info("Email notify skipped: AUDIT_ALERT_NOTIFY_EMAIL_TO empty");
            }
        }
        if (settings.getBoolean("AUDIT_ALERT_NOTIFY_CUSTOM_WEBHOOK", false)) {
            String webhook = settings.get("AUDIT_ALERT_WEBHOOK_URL", "").trim();
            if (StringUtils.hasText(webhook)) {
                try {
                    auditAlertTestSender.testWebhook(webhook, settings.get("AUDIT_ALERT_WEBHOOK_SECRET", ""));
                    notified.add("webhook");
                } catch (Exception ex) {
                    log.info("Webhook notify failed: {}", ex.getMessage());
                }
            }
        }
        return notified;
    }
}
