/*
 ACOJ P0 OJ DDL + admin menu seeds
 Source: docs/p0-数据库设计.md
 Resource IDs: 204000-204099
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oj_problem
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem`;
CREATE TABLE `oj_problem` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_key` varchar(32) NOT NULL COMMENT '对外题号，如 P1001',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `statement_md` mediumtext NOT NULL COMMENT '题面 Markdown',
  `input_format` text NULL COMMENT '输入格式说明',
  `output_format` text NULL COMMENT '输出格式说明',
  `hint` text NULL COMMENT '提示',
  `samples` json NOT NULL DEFAULT ('[]') COMMENT '题面样例 [{input,output,explanation?}]',
  `difficulty` varchar(32) NOT NULL COMMENT 'EASY/MEDIUM/HARD',
  `time_limit_ms` int NOT NULL COMMENT 'CPU 时限毫秒，对应沙箱 cpu_time_ms',
  `memory_limit_bytes` bigint NOT NULL COMMENT '内存限额字节',
  `stack_limit_bytes` bigint NULL COMMENT '栈限额，空则用沙箱默认',
  `output_limit_bytes` bigint NULL COMMENT '输出限额，空则用沙箱默认',
  `judge_mode` varchar(32) NOT NULL DEFAULT 'STANDARD' COMMENT 'P0: STANDARD',
  `allowed_languages` json NOT NULL DEFAULT ('[]') COMMENT '允许语言 key 数组，如 ["cpp17","python3"]',
  `case_version` int NOT NULL DEFAULT 1 COMMENT '测例变更版本；提交时快照',
  `status` varchar(32) NOT NULL COMMENT 'DRAFT/PUBLISHED/DISABLED',
  `submit_count` int NOT NULL DEFAULT 0 COMMENT '提交总数（冗余）',
  `accept_count` int NOT NULL DEFAULT 0 COMMENT 'AC 总数（冗余）',
  `source` varchar(128) NULL COMMENT '来源文案',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '扩展信息',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_problem_key` (`problem_key`),
  KEY `idx_oj_problem_status_difficulty` (`status`, `difficulty`),
  KEY `idx_oj_problem_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 题目';

-- ----------------------------
-- Table structure for oj_problem_case
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem_case`;
CREATE TABLE `oj_problem_case` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_id` varchar(64) NOT NULL COMMENT '所属题目ID',
  `case_version` int NOT NULL COMMENT '测例包版本，与 oj_problem.case_version 对齐',
  `case_key` varchar(64) NOT NULL COMMENT '题内测例号，如 1、sample1',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '判题与展示顺序',
  `is_sample` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否样例（可对用户展示）',
  `score` int NOT NULL DEFAULT 0 COMMENT '预留 OI 分值；P0 STANDARD 可忽略',
  `input_storage` varchar(32) NOT NULL COMMENT 'INLINE/OBJECT',
  `output_storage` varchar(32) NOT NULL COMMENT 'INLINE/OBJECT',
  `input_text` mediumtext NULL COMMENT 'INLINE 输入；OBJECT 时 NULL',
  `output_text` mediumtext NULL COMMENT 'INLINE 期望输出；OBJECT 时 NULL',
  `input_object_key` varchar(512) NULL COMMENT 'OBJECT 输入对象键',
  `output_object_key` varchar(512) NULL COMMENT 'OBJECT 期望输出对象键',
  `input_bytes` int NOT NULL DEFAULT 0 COMMENT '输入字节数',
  `output_bytes` int NOT NULL DEFAULT 0 COMMENT '输出字节数',
  `checksum_sha256` varchar(64) NULL COMMENT '可选校验',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '扩展信息',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_problem_case_ver_key` (`problem_id`, `case_version`, `case_key`),
  KEY `idx_oj_problem_case_ver_sort` (`problem_id`, `case_version`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 题目测例（与题目组合，不复用 sys_file）';

-- ----------------------------
-- Table structure for oj_problem_solution
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem_solution`;
CREATE TABLE `oj_problem_solution` (
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

-- ----------------------------
-- Table structure for oj_problem_dry_run
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem_dry_run`;
CREATE TABLE `oj_problem_dry_run` (
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

-- ----------------------------
-- Table structure for oj_tag
-- ----------------------------
DROP TABLE IF EXISTS `oj_tag`;
CREATE TABLE `oj_tag` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `name` varchar(128) NOT NULL COMMENT '标签名称',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 题目标签';

-- ----------------------------
-- Table structure for oj_problem_tag
-- ----------------------------
DROP TABLE IF EXISTS `oj_problem_tag`;
CREATE TABLE `oj_problem_tag` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_id` varchar(64) NOT NULL COMMENT '题目ID',
  `tag_id` varchar(64) NOT NULL COMMENT '标签ID',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_problem_tag` (`problem_id`, `tag_id`),
  KEY `idx_oj_problem_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 题目-标签关联';

-- ----------------------------
-- Table structure for oj_submission
-- ----------------------------
DROP TABLE IF EXISTS `oj_submission`;
CREATE TABLE `oj_submission` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `problem_id` varchar(64) NOT NULL COMMENT '题目ID',
  `account_id` varchar(64) NOT NULL COMMENT '提交人账户ID',
  `language` varchar(32) NOT NULL COMMENT '语言 key，与 SparkSandbox 一致',
  `source_code` mediumtext NOT NULL COMMENT '源代码',
  `case_version` int NOT NULL COMMENT '提交时题目测例版本快照',
  `status` varchar(32) NOT NULL COMMENT 'PENDING/JUDGING/AC/WA/TLE/MLE/OLE/RE/CE/SE',
  `score` int NOT NULL DEFAULT 0 COMMENT 'P0: AC=100 否则 0',
  `time_ms` int NULL COMMENT '测点耗时汇总（如峰值或总和，实现写死一种）',
  `memory_bytes` bigint NULL COMMENT '测点内存峰值汇总',
  `compile_output` text NULL COMMENT '编译输出（CE）',
  `judge_message` varchar(512) NULL COMMENT '简短说明',
  `case_results` json NOT NULL DEFAULT ('[]') COMMENT '业务裁决后的测点摘要数组',
  `sandbox_raw` json NOT NULL DEFAULT ('{}') COMMENT '截断后的执行侧摘要（排障）',
  `queued_at` datetime(6) NULL COMMENT '入队时间',
  `judged_at` datetime(6) NULL COMMENT '终态时间',
  `judge_node_id` varchar(64) NULL COMMENT '当前/最后一次派发节点',
  `dispatch_count` int NOT NULL DEFAULT 0 COMMENT '已派发次数（含换机）',
  `tried_node_ids` json NOT NULL DEFAULT ('[]') COMMENT '已尝试节点 ID 列表',
  `judge_lease_until` datetime(6) NULL COMMENT '领取租约截止',
  `judge_lease_owner` varchar(128) NULL COMMENT 'Worker 实例 ID',
  `judge_token` varchar(64) NULL COMMENT '本次领取 token；终态 CAS 校验',
  `last_dispatch_error` varchar(512) NULL COMMENT '调度/传输错误摘要',
  `next_retry_at` datetime(6) NULL COMMENT '退避重试时间',
  `error_code` varchar(64) NULL COMMENT '如 NODE_UNAVAILABLE',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '扩展信息',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_oj_submission_problem_created` (`problem_id`, `created_at`),
  KEY `idx_oj_submission_account_created` (`account_id`, `created_at`),
  KEY `idx_oj_submission_status_queued` (`status`, `queued_at`),
  KEY `idx_oj_submission_lease` (`status`, `judge_lease_until`),
  KEY `idx_oj_submission_retry` (`status`, `next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 提交';

-- ----------------------------
-- Table structure for oj_user_problem_stat
-- ----------------------------
DROP TABLE IF EXISTS `oj_user_problem_stat`;
CREATE TABLE `oj_user_problem_stat` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `account_id` varchar(64) NOT NULL COMMENT '账户ID',
  `problem_id` varchar(64) NOT NULL COMMENT '题目ID',
  `status` varchar(32) NOT NULL COMMENT 'ATTEMPTED/ACCEPTED',
  `attempt_count` int NOT NULL DEFAULT 0 COMMENT '提交次数',
  `accepted_count` int NOT NULL DEFAULT 0 COMMENT 'AC 次数',
  `first_accepted_at` datetime(6) NULL COMMENT '首次 AC 时间',
  `last_submit_at` datetime(6) NULL COMMENT '最近提交时间',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '扩展信息',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_user_problem` (`account_id`, `problem_id`),
  KEY `idx_oj_user_problem_problem` (`problem_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 用户题目统计';

-- ----------------------------
-- Table structure for oj_judge_node
-- ----------------------------
DROP TABLE IF EXISTS `oj_judge_node`;
CREATE TABLE `oj_judge_node` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `code` varchar(64) NOT NULL COMMENT '节点编码',
  `name` varchar(128) NOT NULL COMMENT '展示名',
  `base_url` varchar(512) NOT NULL COMMENT 'SparkSandbox 根地址，如 http://10.0.0.11:8080',
  `signing_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否对该节点验签',
  `signing_secret_cipher` varchar(1024) NULL COMMENT '节点密钥密文；空则用全局默认',
  `admin_status` varchar(32) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED/DRAINING',
  `runtime_status` varchar(32) NOT NULL DEFAULT 'OFFLINE' COMMENT 'ONLINE/OFFLINE/UNHEALTHY',
  `circuit_state` varchar(32) NOT NULL DEFAULT 'CLOSED' COMMENT 'CLOSED/OPEN/HALF_OPEN',
  `circuit_opened_at` datetime(6) NULL COMMENT '熔断打开时间',
  `circuit_half_open_at` datetime(6) NULL COMMENT '进入半开时间',
  `weight` int NOT NULL DEFAULT 100 COMMENT '调度权重，越大越易被选中',
  `priority` int NOT NULL DEFAULT 100 COMMENT '并列时优先级，越小越优先',
  `max_concurrency` int NOT NULL DEFAULT 4 COMMENT 'ACOJ 侧最大在途',
  `inflight_count` int NOT NULL DEFAULT 0 COMMENT '持久化在途（与 JUDGING 聚合对账；不用 Redis）',
  `epoch` bigint NOT NULL DEFAULT 0 COMMENT '节点世代；OFFLINE/硬故障时递增',
  `total_dispatch` bigint NOT NULL DEFAULT 0 COMMENT '累计派发',
  `total_success` bigint NOT NULL DEFAULT 0 COMMENT '累计成功产出用户结果',
  `total_transport_fail` bigint NOT NULL DEFAULT 0 COMMENT '累计传输失败',
  `consecutive_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续传输/探活失败',
  `last_heartbeat_at` datetime(6) NULL COMMENT '最近探活成功',
  `last_selected_at` datetime(6) NULL COMMENT '最近被选中',
  `last_success_at` datetime(6) NULL COMMENT '最近 SUCCESS_RESULT',
  `last_error_at` datetime(6) NULL COMMENT '最近错误时间',
  `last_error_message` varchar(512) NULL COMMENT '最近错误摘要',
  `probe_latency_ms` int NULL COMMENT '最近探活 RTT',
  `supported_languages` json NOT NULL DEFAULT ('[]') COMMENT '空数组=全语言；否则白名单',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '机房/AZ/备注等',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_judge_node_code` (`code`),
  KEY `idx_oj_judge_node_admin_runtime` (`admin_status`, `runtime_status`, `circuit_state`),
  KEY `idx_oj_judge_node_runtime_inflight` (`runtime_status`, `inflight_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 执行机（SparkSandbox）';

-- ----------------------------
-- Table structure for oj_judge_dispatch
-- ----------------------------
DROP TABLE IF EXISTS `oj_judge_dispatch`;
CREATE TABLE `oj_judge_dispatch` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `submission_id` varchar(64) NOT NULL COMMENT '提交ID',
  `node_id` varchar(64) NOT NULL COMMENT '执行机ID',
  `node_epoch` bigint NOT NULL DEFAULT 0 COMMENT '派发时节点 epoch',
  `attempt_no` int NOT NULL COMMENT '该提交第几次派发',
  `worker_id` varchar(128) NOT NULL COMMENT 'Worker 实例 ID',
  `request_id` varchar(64) NOT NULL COMMENT '链路追踪 ID',
  `started_at` datetime(6) NOT NULL COMMENT '开始时间',
  `finished_at` datetime(6) NULL COMMENT '结束时间',
  `duration_ms` int NULL COMMENT '耗时毫秒',
  `outcome` varchar(32) NOT NULL COMMENT 'SUCCESS_RESULT/TRANSPORT_FAIL/SANDBOX_INTERNAL/CANCELLED_LEASE/TIMEOUT',
  `http_status` int NULL COMMENT 'HTTP 状态码',
  `error_code` varchar(64) NULL COMMENT '错误码',
  `error_message` varchar(512) NULL COMMENT '错误摘要（脱敏）',
  `user_verdict` varchar(32) NULL COMMENT '本轮用户结果 AC/WA/...；无则 NULL',
  `extra` json NOT NULL DEFAULT ('{}') COMMENT '摘要扩展；禁止完整源码与超大 stdout',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `created_by` varchar(64) NULL COMMENT '创建人',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `updated_by` varchar(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oj_judge_dispatch_attempt` (`submission_id`, `attempt_no`),
  KEY `idx_oj_judge_dispatch_node_started` (`node_id`, `started_at`),
  KEY `idx_oj_judge_dispatch_outcome_started` (`outcome`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OJ 派发审计';

-- ----------------------------
-- Menu seeds: OJ catalog / menus / buttons / pages (IDs 204000-204099)
-- ----------------------------
INSERT INTO `sys_resource` VALUES ('204000', NULL, 'oj', 'OJ 管理', 'CATALOG', '210001', '/oj', NULL, NULL, 'icon-park-outline:code', NULL, NULL, 25, 1, 0, 0, 'ENABLED', 'OJ 题目、标签、执行机与提交管理', NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

-- problem (full CRUD + pages, like notice 202201-208)
-- child IDs 204051-204058 avoid collision with sys-job 204011-204016
INSERT INTO `sys_resource` VALUES ('204010', '204000', 'oj-problem', '题目管理', 'MENU', '210001', '/oj/problem', '/oj/problem/index.vue', NULL, 'icon-park-outline:book-open', NULL, NULL, 1, 1, 0, 0, 'ENABLED', 'OJ 题目管理', NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204051', '204010', 'oj-problem-page', '分页题目', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204052', '204010', 'oj-problem-create', '新增题目', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204053', '204010', 'oj-problem-detail', '详情题目', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204054', '204010', 'oj-problem-update', '编辑题目', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204055', '204010', 'oj-problem-delete', '删除题目', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204056', '204010', 'oj-problem-create-page', '新增题目页', 'PAGE', '210001', '/oj/problem/create', '/oj/problem/form.vue', NULL, NULL, NULL, NULL, 60, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204057', '204010', 'oj-problem-edit-page', '编辑题目页', 'PAGE', '210001', '/oj/problem/edit', '/oj/problem/form.vue', NULL, NULL, NULL, NULL, 70, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204058', '204010', 'oj-problem-detail-page', '题目详情页', 'PAGE', '210001', '/oj/problem/detail', '/oj/problem/detail.vue', NULL, NULL, NULL, NULL, 80, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204059', '204010', 'oj-problem-cases-page', '题目测例页', 'PAGE', '210001', '/oj/problem/cases', '/oj/problem/cases.vue', NULL, NULL, NULL, NULL, 90, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204060', '204010', 'oj-problem-dry-runs-page', '题目试跑历史页', 'PAGE', '210001', '/oj/problem/dry-runs', '/oj/problem/dry-runs.vue', NULL, NULL, NULL, NULL, 95, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204066', '204010', 'oj-problem-solutions-page', '题目参考答案页', 'PAGE', '210001', '/oj/problem/solutions', '/oj/problem/solutions.vue', NULL, NULL, NULL, NULL, 92, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204067', '204010', 'oj-problem-dry-run-page', '题目试跑页', 'PAGE', '210001', '/oj/problem/dry-run', '/oj/problem/dry-run.vue', NULL, NULL, NULL, NULL, 91, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

-- tag (modal CRUD, buttons only)
-- child IDs 204061-204065 avoid collision with sys-job pages 204021-204024
INSERT INTO `sys_resource` VALUES ('204020', '204000', 'oj-tag', '标签管理', 'MENU', '210001', '/oj/tag', '/oj/tag/index.vue', NULL, 'icon-park-outline:tag-one', NULL, NULL, 2, 1, 0, 0, 'ENABLED', 'OJ 题目标签', NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204061', '204020', 'oj-tag-page', '分页标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204062', '204020', 'oj-tag-create', '新增标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204063', '204020', 'oj-tag-detail', '详情标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204064', '204020', 'oj-tag-update', '编辑标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204065', '204020', 'oj-tag-delete', '删除标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

-- judge-node (modal CRUD, buttons only)
INSERT INTO `sys_resource` VALUES ('204030', '204000', 'oj-judge-node', '执行机管理', 'MENU', '210001', '/oj/judge-node', '/oj/judge-node/index.vue', NULL, 'icon-park-outline:server', NULL, NULL, 3, 1, 0, 0, 'ENABLED', 'OJ 执行机（SparkSandbox）', NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204031', '204030', 'oj-judgenode-page', '分页执行机', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204032', '204030', 'oj-judgenode-create', '新增执行机', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204033', '204030', 'oj-judgenode-detail', '详情执行机', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204034', '204030', 'oj-judgenode-update', '编辑执行机', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204035', '204030', 'oj-judgenode-delete', '删除执行机', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

-- submission (page/detail + detail page)
INSERT INTO `sys_resource` VALUES ('204040', '204000', 'oj-submission', '提交管理', 'MENU', '210001', '/oj/submission', '/oj/submission/index.vue', NULL, 'icon-park-outline:list', NULL, NULL, 4, 1, 0, 0, 'ENABLED', 'OJ 提交记录', NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204041', '204040', 'oj-submission-page', '分页提交', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204042', '204040', 'oj-submission-detail', '详情提交', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_resource` VALUES ('204043', '204040', 'oj-submission-detail-page', '提交详情页', 'PAGE', '210001', '/oj/submission/detail', '/oj/submission/detail.vue', NULL, NULL, NULL, NULL, 30, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

-- ----------------------------
-- RESOURCE_PERMISSION seeds (relation IDs 8107200000001xxx)
-- pattern: subject=RESOURCE, account_type=ADMIN, like notice/dict
-- ----------------------------
INSERT INTO `sys_iam_relation` VALUES ('8107200000001001', 'RESOURCE', '204010', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '题目管理访问', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001002', 'RESOURCE', '204051', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '分页题目', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001003', 'RESOURCE', '204052', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:create', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '新增题目', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001004', 'RESOURCE', '204053', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:detail', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '详情题目', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001005', 'RESOURCE', '204054', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:update', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '编辑题目', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001006', 'RESOURCE', '204055', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problem:delete', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '删除题目', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

INSERT INTO `sys_iam_relation` VALUES ('8107200000001011', 'RESOURCE', '204020', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '标签管理访问', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001012', 'RESOURCE', '204061', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '分页标签', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001013', 'RESOURCE', '204062', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:create', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '新增标签', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001014', 'RESOURCE', '204063', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:detail', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '详情标签', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001015', 'RESOURCE', '204064', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:update', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '编辑标签', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001016', 'RESOURCE', '204065', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:tag:delete', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '删除标签', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

INSERT INTO `sys_iam_relation` VALUES ('8107200000001021', 'RESOURCE', '204030', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '执行机管理访问', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001022', 'RESOURCE', '204031', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '分页执行机', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001023', 'RESOURCE', '204032', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:create', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '新增执行机', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001024', 'RESOURCE', '204033', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:detail', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '详情执行机', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001025', 'RESOURCE', '204034', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:update', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '编辑执行机', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001026', 'RESOURCE', '204035', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenode:delete', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '删除执行机', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

INSERT INTO `sys_iam_relation` VALUES ('8107200000001031', 'RESOURCE', '204040', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submission:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '提交管理访问', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001032', 'RESOURCE', '204041', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submission:page', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '分页提交', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);
INSERT INTO `sys_iam_relation` VALUES ('8107200000001033', 'RESOURCE', '204042', 'ADMIN', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submission:detail', 'CASCADE', 'ALL', '[]', 0, 0, 'ENABLED', '详情提交', NULL, NULL, '{}', '2026-08-30 00:00:00.000000', NULL, '2026-08-30 00:00:00.000000', NULL);

SET FOREIGN_KEY_CHECKS = 1;
