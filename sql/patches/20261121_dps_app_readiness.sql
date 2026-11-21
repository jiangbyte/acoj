-- 工具调用轨迹、筛查任务 min_match_length、相似度精度
ALTER TABLE `task_reports`
  ADD COLUMN `min_match_length` int NULL DEFAULT NULL COMMENT 'GST 最小匹配长度（token 个数）' AFTER `threshold`;

ALTER TABLE `task_similarity`
  MODIFY COLUMN `similarity` decimal(10, 6) UNSIGNED NULL DEFAULT 0.000000 COMMENT '相似度（精确到 6 位小数）';

ALTER TABLE `task_reports`
  MODIFY COLUMN `avg_similarity` decimal(10, 6) UNSIGNED NULL DEFAULT 0.000000 COMMENT '平均相似度',
  MODIFY COLUMN `max_similarity` decimal(10, 6) UNSIGNED NULL DEFAULT 0.000000 COMMENT '最大相似度';

ALTER TABLE `task_similarity`
  ADD COLUMN `match_tiles` json NULL COMMENT 'GST 匹配块' AFTER `origin_token_texts`;

CREATE TABLE IF NOT EXISTS `sys_conversation_tool_trace` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对话ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `tool_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具名',
  `tool_args` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工具参数JSON',
  `tool_result` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工具返回摘要',
  `duration_ms` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SUCCESS/ERROR',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_user_problem`(`user_id` ASC, `problem_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'LLM 工具调用轨迹' ROW_FORMAT = DYNAMIC;
