-- OJ P0 增量：题目参考答案独立页资源
-- Author: Charlie

INSERT INTO `sys_resource` (`id`, `parent_id`, `code`, `name`, `resource_type`, `module_id`, `path`, `component`, `redirect`, `icon`, `color`, `href`, `sort`, `is_visible`, `is_cache`, `is_affix`, `status`, `description`, `layout`, `extra`, `created_at`, `created_by`, `updated_at`, `updated_by`)
SELECT '204066', '204010', 'oj-problem-solutions-page', '题目参考答案页', 'PAGE', '210001', '/oj/problem/solutions', '/oj/problem/solutions.vue', NULL, NULL, NULL, NULL, 92, 0, 0, 0, 'ENABLED', NULL, NULL, '{}', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_resource` WHERE `id` = '204066');
