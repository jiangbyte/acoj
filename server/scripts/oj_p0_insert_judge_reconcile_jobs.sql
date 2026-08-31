-- OJ P0：判题调度对账 / 探活 / 租约回收任务（sys_job + Lock4j 单例）
-- Author: Charlie
-- 幂等：按 id 不存在才插入

INSERT INTO `sys_job` (
  `id`, `name`, `handler`, `trigger_type`, `trigger_config`, `params`,
  `last_run_time`, `next_run_time`, `last_result`, `enabled`, `description`, `sort`,
  `created_at`, `created_by`, `updated_at`, `updated_by`
)
SELECT * FROM (
  SELECT
    '7541000000000000101' AS id,
    'OJ 执行机心跳超时' AS name,
    'github.jiangbyte.io.oj.modules.judge.job.OjJudgeHeartbeatJob' AS handler,
    'FIXED' AS trigger_type,
    '15' AS trigger_config,
    '{}' AS params,
    NULL AS last_run_time,
    CURRENT_TIMESTAMP(6) AS next_run_time,
    NULL AS last_result,
    1 AS enabled,
    '超时未心跳 → OFFLINE，并推进熔断半开' AS description,
    101 AS sort,
    CURRENT_TIMESTAMP(6) AS created_at,
    NULL AS created_by,
    CURRENT_TIMESTAMP(6) AS updated_at,
    NULL AS updated_by
  UNION ALL SELECT
    '7541000000000000102',
    'OJ 判题租约回收',
    'github.jiangbyte.io.oj.modules.judge.job.OjJudgeLeaseReaperJob',
    'FIXED',
    '15',
    '{}',
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL,
    1,
    '过期 JUDGING → PENDING 并 MQ 再入队；dispatch CAS 成功才减 inflight',
    102,
    CURRENT_TIMESTAMP(6),
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL
  UNION ALL SELECT
    '7541000000000000103',
    'OJ PENDING 补偿入队',
    'github.jiangbyte.io.oj.modules.judge.job.OjJudgePendingEnqueueJob',
    'FIXED',
    '60',
    '{"batchSize":100,"staleMs":60000,"compensateIntervalMs":60000}',
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL,
    1,
    'PENDING 滞留补偿 publish（LIMIT+占位限频，防 MQ 风暴）',
    103,
    CURRENT_TIMESTAMP(6),
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL
  UNION ALL SELECT
    '7541000000000000104',
    'OJ 在途 inflight 对账',
    'github.jiangbyte.io.oj.modules.judge.job.OjJudgeInflightReconcileJob',
    'FIXED',
    '60',
    '{}',
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL,
    1,
    '以 JUDGING 聚合修复 oj_judge_node.inflight_count',
    104,
    CURRENT_TIMESTAMP(6),
    NULL,
    CURRENT_TIMESTAMP(6),
    NULL
) AS t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job` j WHERE j.`id` = t.id
);
