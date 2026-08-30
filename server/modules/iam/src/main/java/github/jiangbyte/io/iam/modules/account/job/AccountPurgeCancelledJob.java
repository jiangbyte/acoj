package github.jiangbyte.io.iam.modules.account.job;

import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.iam.modules.account.service.AccountService;
import github.jiangbyte.io.sys.config.ConfigApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 定时任务：清理已取消且超过保留期的账号数据（物理清理）。
 * 执行参数（JSON）：{"retentionDays": 30}，兼容旧纯数字传参。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountPurgeCancelledJob implements JobHandler {

    private final AccountService accountService;
    private final ConfigApi configApi;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String params) {
        int retentionDays = resolveRetentionDays(params);
        int purged = accountService.purgeExpiredCancelledAccounts(retentionDays);
        log.info("Purged {} cancelled account(s) with retentionDays={}", purged, retentionDays);
        return "purged=" + purged;
    }

    private int resolveRetentionDays(String params) {
        if (params != null && !params.isBlank()) {
            try {
                Object value = objectMapper.readValue(params, Object.class);
                if (value instanceof Map<?, ?> map && map.get("retentionDays") != null) {
                    return Integer.parseInt(String.valueOf(map.get("retentionDays")).trim());
                }
                // 兼容旧纯数字传参
                return Integer.parseInt(params.trim());
            } catch (NumberFormatException ex) {
                log.info("Invalid job param '{}', fallback to ACCOUNT_CANCEL_RETENTION_DAYS", params);
            } catch (Exception ex) {
                log.info("Unparseable job param '{}', fallback to ACCOUNT_CANCEL_RETENTION_DAYS", params);
            }
        }
        return configApi.getInt("ACCOUNT_CANCEL_RETENTION_DAYS", 15);
    }
}
