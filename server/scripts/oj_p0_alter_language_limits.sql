/*
  Breaking: move resource limits from oj_problem to oj_problem_language_limit.
  Drop problem-level limits and allowed_languages (languages come from language_limit rows).
  No data backfill — recreate problems after applying.
*/

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `oj_problem_language_limit`
(
    `id`
    varchar
(
    64
) NOT NULL COMMENT '主键ID',
    `problem_id` varchar
(
    64
) NOT NULL COMMENT '所属题目ID',
    `language` varchar
(
    32
) NOT NULL COMMENT '语言 key，与 SparkSandbox 一致',
    `time_limit_ms` int NOT NULL COMMENT 'CPU 时限毫秒，对应沙箱 cpu_time_ms',
    `memory_limit_bytes` bigint NOT NULL COMMENT '内存限额字节',
    `stack_limit_bytes` bigint NULL COMMENT '栈限额，空则用沙箱默认',
    `output_limit_bytes` bigint NULL COMMENT '输出限额，空则用沙箱默认',
    `extra` json NOT NULL DEFAULT
(
    '{}'
) COMMENT '扩展信息',
    `created_at` datetime
(
    6
) NOT NULL DEFAULT CURRENT_TIMESTAMP
(
    6
) COMMENT '创建时间',
    `created_by` varchar
(
    64
) NULL COMMENT '创建人',
    `updated_at` datetime
(
    6
) NOT NULL DEFAULT CURRENT_TIMESTAMP
(
    6
) ON UPDATE CURRENT_TIMESTAMP
(
    6
) COMMENT '更新时间',
    `updated_by` varchar
(
    64
) NULL COMMENT '更新人',
    PRIMARY KEY
(
    `id`
),
    UNIQUE KEY `uk_oj_problem_language_limit`
(
    `problem_id`,
    `language`
),
    KEY `idx_oj_problem_language_limit_problem`
(
    `problem_id`
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='OJ 题目×语言资源限额';

ALTER TABLE `oj_problem`
DROP
COLUMN `time_limit_ms`,
  DROP
COLUMN `memory_limit_bytes`,
  DROP
COLUMN `stack_limit_bytes`,
  DROP
COLUMN `output_limit_bytes`,
  DROP
COLUMN `allowed_languages`;
