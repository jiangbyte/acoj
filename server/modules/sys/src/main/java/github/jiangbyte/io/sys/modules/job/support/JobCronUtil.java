package github.jiangbyte.io.sys.modules.job.support;

import github.jiangbyte.io.common.core.exception.BizException;
import org.springframework.scheduling.support.CronExpression;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * 任务触发时间工具：CRON 表达式与固定间隔的统一计算与校验。
 *
 * Author: Charlie
 */
public final class JobCronUtil {

    /** 触发类型：CRON 表达式。 */
    public static final String TYPE_CRON = "CRON";

    /** 触发类型：固定间隔（秒）。 */
    public static final String TYPE_FIXED = "FIXED";

    private JobCronUtil() {
    }

    /** 校验触发配置合法性；不合法抛出 BizException。 */
    public static void validate(String triggerType, String triggerConfig) {
        if (TYPE_FIXED.equalsIgnoreCase(triggerType)) {
            if (triggerConfig == null || !triggerConfig.trim().matches("\\d+")
                    || Long.parseLong(triggerConfig.trim()) <= 0) {
                throw new BizException("固定间隔必须为正整数（秒）");
            }
            return;
        }
        if (!TYPE_CRON.equalsIgnoreCase(triggerType)) {
            throw new BizException("触发类型仅支持 " + TYPE_CRON + " 或 " + TYPE_FIXED);
        }
        try {
            if (CronExpression.parse(triggerConfig).next(ZonedDateTime.now()) == null) {
                throw new BizException("CRON 表达式在未来没有可执行时间");
            }
        } catch (IllegalArgumentException ex) {
            throw new BizException("CRON 表达式无效: " + ex.getMessage());
        }
    }

    /** 基于 from 计算下一次触发时间（调用前需先通过 validate）。 */
    public static OffsetDateTime computeNextRunTime(String triggerType, String triggerConfig, OffsetDateTime from) {
        if (TYPE_FIXED.equalsIgnoreCase(triggerType)) {
            return from.plusSeconds(Long.parseLong(triggerConfig.trim()));
        }
        ZonedDateTime next = CronExpression.parse(triggerConfig).next(from.toZonedDateTime());
        return next == null ? from : next.toOffsetDateTime();
    }
}
