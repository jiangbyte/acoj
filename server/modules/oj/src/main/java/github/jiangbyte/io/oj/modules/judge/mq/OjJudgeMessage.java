package github.jiangbyte.io.oj.modules.judge.mq;

/**
 * 判题队列消息体：支持用户提交与管理端试跑。
 *
 * Author: Charlie
 */
public record OjJudgeMessage(
        String jobType,
        String submissionId,
        String dryRunId,
        Boolean stopOnFirstError,
        String requestId,
        long enqueueAt,
        String reason
) {
    public static final String TYPE_SUBMISSION = "SUBMISSION";
    public static final String TYPE_DRY_RUN = "DRY_RUN";

    public static final String REASON_SUBMIT = "SUBMIT";
    public static final String REASON_DRY_RUN = "DRY_RUN";
    public static final String REASON_FAILOVER = "FAILOVER";
    public static final String REASON_LEASE_REAP = "LEASE_REAP";
    public static final String REASON_RETRY_BACKOFF = "RETRY_BACKOFF";

    public static OjJudgeMessage submission(String submissionId, String requestId, String reason) {
        return new OjJudgeMessage(
                TYPE_SUBMISSION, submissionId, null, null, requestId, System.currentTimeMillis(), reason);
    }

    public static OjJudgeMessage dryRun(String dryRunId, String requestId, boolean stopOnFirstError) {
        return new OjJudgeMessage(
                TYPE_DRY_RUN, null, dryRunId, stopOnFirstError, requestId, System.currentTimeMillis(), REASON_DRY_RUN);
    }

    /**
     * 兼容旧调用：默认按用户提交。
     */
    public static OjJudgeMessage of(String submissionId, String requestId, String reason) {
        return submission(submissionId, requestId, reason);
    }

    public boolean isDryRun() {
        return TYPE_DRY_RUN.equalsIgnoreCase(jobType);
    }

    public String jobId() {
        return isDryRun() ? dryRunId : submissionId;
    }
}
