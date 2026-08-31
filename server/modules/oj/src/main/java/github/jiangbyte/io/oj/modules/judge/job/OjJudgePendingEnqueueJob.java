package github.jiangbyte.io.oj.modules.judge.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PENDING 补偿入队：MQ 丢失 / confirm 失败后，按 DB 状态限流重发。
 * <p>由 sys_job + Lock4j 单例调度；先条件占位再 publish，防风暴。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgePendingEnqueueJob implements JobHandler {

    private static final String EXTRA_COMPENSATE_AT = "compensate_at";
    private static final String JSON_TYPE_HANDLER =
            "typeHandler=github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler";

    private final OjSubmissionMapper ojSubmissionMapper;
    private final OjJudgePublisher ojJudgePublisher;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String params) {
        int batchSize = readInt(params, "batchSize", 100);
        long staleMs = readLong(params, "staleMs", 60_000L);
        long compensateIntervalMs = readLong(params, "compensateIntervalMs", 60_000L);
        batchSize = Math.max(1, Math.min(batchSize, 500));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime staleBefore = now.minus(Duration.ofMillis(staleMs));

        List<OjSubmission> candidates = ojSubmissionMapper.selectList(
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(OjSubmission::getStatus, OjVerdict.PENDING.name())
                        .le(OjSubmission::getQueuedAt, staleBefore)
                        .and(w -> w.isNull(OjSubmission::getNextRetryAt)
                                .or()
                                .le(OjSubmission::getNextRetryAt, now))
                        .orderByAsc(OjSubmission::getQueuedAt)
                        .last("LIMIT " + batchSize));
        if (candidates.isEmpty()) {
            return "claimed=0,published=0";
        }

        int claimed = 0;
        int published = 0;
        for (OjSubmission row : candidates) {
            if (row == null || !StringUtils.hasText(row.getId())) {
                continue;
            }
            if (!claimCompensate(row, now, compensateIntervalMs)) {
                continue;
            }
            claimed++;
            try {
                String requestId = UUID.randomUUID().toString().replace("-", "");
                ojJudgePublisher.publishWork(
                        OjJudgeMessage.of(row.getId(), requestId, OjJudgeMessage.REASON_COMPENSATE));
                published++;
            } catch (Exception ex) {
                log.warn("pending compensate publish failed submissionId={}: {}", row.getId(), ex.toString());
            }
        }
        return "claimed=" + claimed + ",published=" + published;
    }

    /**
     * 条件占位：距上次补偿 ≥ interval 才写入 compensate_at；affected=1 才允许 publish。
     */
    private boolean claimCompensate(OjSubmission row, OffsetDateTime now, long compensateIntervalMs) {
        Map<String, Object> extra = row.getExtra() == null
                ? new HashMap<>()
                : new HashMap<>(row.getExtra());
        Object prev = extra.get(EXTRA_COMPENSATE_AT);
        if (prev != null) {
            try {
                OffsetDateTime last = OffsetDateTime.parse(String.valueOf(prev).trim());
                if (Duration.between(last, now).toMillis() < compensateIntervalMs) {
                    return false;
                }
            } catch (Exception ignored) {
                // 无法解析则允许覆盖
            }
        }
        extra.put(EXTRA_COMPENSATE_AT, now.toString());
        return ojSubmissionMapper.update(null, Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getExtra, extra, JSON_TYPE_HANDLER)
                .eq(OjSubmission::getId, row.getId())
                .eq(OjSubmission::getStatus, OjVerdict.PENDING.name())) > 0;
    }

    private int readInt(String params, String key, int defaultValue) {
        Object value = readParam(params, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private long readLong(String params, String key, long defaultValue) {
        Object value = readParam(params, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private Object readParam(String params, String key) {
        if (!StringUtils.hasText(params)) {
            return null;
        }
        try {
            Object root = objectMapper.readValue(params, Object.class);
            if (root instanceof Map<?, ?> map) {
                return map.get(key);
            }
        } catch (Exception ignored) {
            // fallback default
        }
        return null;
    }
}
