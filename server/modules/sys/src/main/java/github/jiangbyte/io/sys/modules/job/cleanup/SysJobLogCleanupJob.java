package github.jiangbyte.io.sys.modules.job.cleanup;

import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.sys.modules.job.config.HeiJobProperties;
import github.jiangbyte.io.sys.modules.job.service.JobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 定时任务：按保留天数批量清理过期任务执行日志。
 * 执行参数（JSON）：{"retentionDays": 30, "batchSize": 1000}；缺省回退 hei.job.log.*。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysJobLogCleanupJob implements JobHandler {

    private final JobLogService jobLogService;
    private final HeiJobProperties jobProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String params) {
        int retentionDays = resolveRetentionDays(params);
        if (retentionDays <= 0) {
            log.info("SysJobLogCleanupJob skipped: retentionDays={}", retentionDays);
            return "skipped: retention disabled";
        }
        int batchSize = resolveBatchSize(params);
        int deleted = jobLogService.cleanupExpired(retentionDays, batchSize);
        log.info("SysJobLogCleanupJob deleted={} retentionDays={} batchSize={}", deleted, retentionDays, batchSize);
        return "deleted=" + deleted + ",retentionDays=" + retentionDays + ",batchSize=" + batchSize;
    }

    private int resolveRetentionDays(String params) {
        Integer fromParam = readIntParam(params, "retentionDays");
        if (fromParam != null) {
            return fromParam;
        }
        return jobProperties.getLog().getRetentionDays();
    }

    private int resolveBatchSize(String params) {
        Integer fromParam = readIntParam(params, "batchSize");
        if (fromParam != null && fromParam > 0) {
            return fromParam;
        }
        int configured = jobProperties.getLog().getBatchSize();
        return configured > 0 ? configured : 1000;
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
