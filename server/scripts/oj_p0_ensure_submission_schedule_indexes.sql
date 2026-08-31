-- OJ P0：核对提交调度扫描索引（已存在则跳过）
-- Author: Charlie

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = @db AND table_name = 'oj_submission' AND index_name = 'idx_oj_submission_status_queued'
    ),
    'SELECT 1',
    'ALTER TABLE `oj_submission` ADD KEY `idx_oj_submission_status_queued` (`status`, `queued_at`)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = @db AND table_name = 'oj_submission' AND index_name = 'idx_oj_submission_lease'
    ),
    'SELECT 1',
    'ALTER TABLE `oj_submission` ADD KEY `idx_oj_submission_lease` (`status`, `judge_lease_until`)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = @db AND table_name = 'oj_submission' AND index_name = 'idx_oj_submission_retry'
    ),
    'SELECT 1',
    'ALTER TABLE `oj_submission` ADD KEY `idx_oj_submission_retry` (`status`, `next_retry_at`)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
