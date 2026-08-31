-- OJ P0 增量：参考答案 / 试跑历史表 + 试跑历史页资源
-- Author: Charlie

CREATE TABLE IF NOT EXISTS `oj_problem_solution` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_id` varchar(64) NOT NULL COMMENT '所属题目ID',
  `language` varchar(32) NOT NULL COMMENT '语言 key，与 SparkSandbox 一致',
  `source` mediumtext NOT NULL COMMENT '参考答案源码',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '同题默认答案（仅一条为 1）',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `remark` varchar(255) NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_problem_solution_lang` (`problem_id`, `language`),
  KEY `idx_oj_problem_solution_problem` (`problem_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 题目多语言参考答案';

CREATE TABLE IF NOT EXISTS `oj_problem_dry_run` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_id` varchar(64) NOT NULL COMMENT '题目ID',
  `case_version` int NOT NULL COMMENT '试跑时测例版本',
  `mode` varchar(32) NOT NULL COMMENT 'SINGLE/ALL',
  `case_key` varchar(64) NULL COMMENT 'SINGLE 时测例号',
  `limit_mode` varchar(32) NOT NULL COMMENT 'PROBLEM/RELAXED',
  `language` varchar(32) NOT NULL COMMENT '语言 key',
  `source` mediumtext NOT NULL COMMENT '实际执行源码快照',
  `source_from` varchar(32) NOT NULL COMMENT 'STORED/OVERRIDE',
  `overall_status` varchar(32) NOT NULL COMMENT 'AC/WA/TLE/.../SE',
  `max_time_ms` int NULL COMMENT '测点耗时峰值',
  `max_memory_bytes` bigint NULL COMMENT '测点内存峰值',
  `suggested_time_ms` int NULL COMMENT '建议时限毫秒',
  `suggested_memory_bytes` bigint NULL COMMENT '建议内存字节',
  `applied_time_ms` int NULL COMMENT '本次传给沙箱的 CPU 时限',
  `applied_memory_bytes` bigint NULL COMMENT '本次传给沙箱的内存限额',
  `case_results` json NOT NULL DEFAULT ('[]') COMMENT '每测例 verdict/time/memory/message（可截断）',
  `node_id` varchar(64) NULL COMMENT '执行机ID',
  `error_message` varchar(512) NULL COMMENT '传输/系统错误摘要',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_oj_problem_dry_run_problem_created` (`problem_id`, `created_at`),
  KEY `idx_oj_problem_dry_run_publish` (`problem_id`, `case_version`, `mode`, `limit_mode`, `overall_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 管理端测例试跑历史';

INSERT INTO `sys_resource` (`id`, `parent_id`, `code`, `name`, `resource_type`, `module_id`, `path`, `component`, `redirect`, `icon`, `color`, `href`, `sort`, `is_visible`, `is_cache`, `is_affix`, `status`, `description`, `layout`, `extra`, `created_at`, `created_by`, `updated_at`, `updated_by`)
SELECT '204060', '204010', 'oj-problem-dry-runs-page', '题目试跑历史页', 'PAGE', '210001', '/oj/problem/dry-runs', '/oj/problem/dry-runs.vue', NULL, NULL, NULL, NULL, 95, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_resource` WHERE `id` = '204060');

INSERT INTO `sys_resource` (`id`, `parent_id`, `code`, `name`, `resource_type`, `module_id`, `path`, `component`, `redirect`, `icon`, `color`, `href`, `sort`, `is_visible`, `is_cache`, `is_affix`, `status`, `description`, `layout`, `extra`, `created_at`, `created_by`, `updated_at`, `updated_by`)
SELECT '204066', '204010', 'oj-problem-solutions-page', '题目参考答案页', 'PAGE', '210001', '/oj/problem/solutions', '/oj/problem/solutions.vue', NULL, NULL, NULL, NULL, 92, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_resource` WHERE `id` = '204066');
