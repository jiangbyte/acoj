package github.jiangbyte.io.sys.modules.audit.job;

import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.common.log.config.HeiLogProperties;
import github.jiangbyte.io.sys.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 定时任务：按保留天数批量清理过期审计日志。
 * 执行参数（JSON）：
 * {"loginRetentionDays": 180, "operationRetentionDays": 365, "batchSize": 1000}；
 * 缺省回退 hei.log.audit.*。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogCleanupJob implements JobHandler {

    private final AuditService auditService;
    private final HeiLogProperties logProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String params) {
        int loginRetentionDays = resolveIntParam(params, "loginRetentionDays",
                logProperties.getAudit().getLoginRetentionDays());
        int operationRetentionDays = resolveIntParam(params, "operationRetentionDays",
                logProperties.getAudit().getOperationRetentionDays());
        int batchSize = resolveBatchSize(params);

        int deletedLogin = 0;
        if (loginRetentionDays > 0) {
            deletedLogin = auditService.cleanupExpiredLoginLogs(loginRetentionDays, batchSize);
        } else {
            log.info("AuditLogCleanupJob skipped login logs: loginRetentionDays={}", loginRetentionDays);
        }

        int deletedOperation = 0;
        if (operationRetentionDays > 0) {
            deletedOperation = auditService.cleanupExpiredOperationLogs(operationRetentionDays, batchSize);
        } else {
            log.info("AuditLogCleanupJob skipped operation logs: operationRetentionDays={}", operationRetentionDays);
        }

        log.info(
                "AuditLogCleanupJob deletedLogin={} deletedOperation={} loginRetentionDays={} operationRetentionDays={} batchSize={}",
                deletedLogin,
                deletedOperation,
                loginRetentionDays,
                operationRetentionDays,
                batchSize);
        return "deletedLogin=" + deletedLogin
                + ",deletedOperation=" + deletedOperation
                + ",loginRetentionDays=" + loginRetentionDays
                + ",operationRetentionDays=" + operationRetentionDays
                + ",batchSize=" + batchSize;
    }

    private int resolveBatchSize(String params) {
        Integer fromParam = readIntParam(params, "batchSize");
        if (fromParam != null && fromParam > 0) {
            return fromParam;
        }
        int configured = logProperties.getAudit().getCleanupBatchSize();
        return configured > 0 ? configured : 1000;
    }

    private int resolveIntParam(String params, String key, int fallback) {
        Integer fromParam = readIntParam(params, key);
        return fromParam != null ? fromParam : fallback;
    }

    private Integer readIntParam(String params, String key) {
        if (params == null || params.isBlank()) {
            return null;
        }
        try {
            Object value = objectMapper.readValue(params, Object.class);
            if (value instanceof Map<?, ?> map && map.get(key) != null) {
                return Integer.parseInt(String.valueOf(map.get(key)).trim());
            }
        } catch (Exception ex) {
            log.info("Unparseable job param '{}' for key '{}', fallback to YAML", params, key);
        }
        return null;
    }
}
