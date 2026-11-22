-- 筛查任务执行状态：列表展示 + 未完成禁用 Actions
ALTER TABLE `task_reports`
  ADD COLUMN `exec_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COMPLETED' COMMENT 'PENDING/PROCESSING/COMPLETED/FAILED' AFTER `check_mode`,
  ADD COLUMN `exec_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因摘要' AFTER `exec_status`;
