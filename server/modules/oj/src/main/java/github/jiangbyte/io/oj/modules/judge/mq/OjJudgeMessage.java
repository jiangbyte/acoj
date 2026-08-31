package github.jiangbyte.io.oj.modules.judge.mq;

/**
 * 判题队列消息体。
 *
 * Author: Charlie
 */
public record OjJudgeMessage(
        String submissionId,
        String requestId,
        long enqueueAt,
        String reason
) {
    public static final String REASON_SUBMIT = "SUBMIT";
    public static final String REASON_FAILOVER = "FAILOVER";
    public static final String REASON_LEASE_REAP = "LEASE_REAP";
    public static final String REASON_RETRY_BACKOFF = "RETRY_BACKOFF";

    public static OjJudgeMessage of(String submissionId, String requestId, String reason) {
        return new OjJudgeMessage(submissionId, requestId, System.currentTimeMillis(), reason);
    }
}
