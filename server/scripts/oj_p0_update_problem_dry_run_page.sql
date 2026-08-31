-- OJ P0 增量：题目试跑独立页资源
-- Author: Charlie
-- 注意：204061 已被 oj-tag-page 占用，试跑页使用 204067

INSERT INTO `sys_resource` (`id`, `parent_id`, `code`, `name`, `resource_type`, `module_id`, `path`, `component`, `redirect`, `icon`, `color`, `href`, `sort`, `is_visible`, `is_cache`, `is_affix`, `status`, `description`, `layout`, `extra`, `created_at`, `created_by`, `updated_at`, `updated_by`)
SELECT '204067', '204010', 'oj-problem-dry-run-page', '题目试跑页', 'PAGE', '210001', '/oj/problem/dry-run', '/oj/problem/dry-run.vue', NULL, NULL, NULL, NULL, 91, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_resource` WHERE `code` = 'oj-problem-dry-run-page');
