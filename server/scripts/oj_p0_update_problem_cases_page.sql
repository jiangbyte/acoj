-- OJ P0 增量：题目测例独立页资源
-- Author: Charlie

INSERT INTO `sys_resource` (`id`, `parent_id`, `code`, `name`, `resource_type`, `module_id`, `path`, `component`, `redirect`, `icon`, `color`, `href`, `sort`, `is_visible`, `is_cache`, `is_affix`, `status`, `description`, `layout`, `extra`, `created_at`, `created_by`, `updated_at`, `updated_by`)
SELECT '204059', '204010', 'oj-problem-cases-page', '题目测例页', 'PAGE', '210001', '/oj/problem/cases', '/oj/problem/cases.vue', NULL, NULL, NULL, NULL, 90, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_resource` WHERE `id` = '204059');
