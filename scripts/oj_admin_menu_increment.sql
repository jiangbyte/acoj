BEGIN;

INSERT INTO public.sys_resource_module (
    id, name, code, client, icon, color, sort, status, description, extra, created_at, created_by, updated_at, updated_by
) VALUES
    ('210003', '在线判题', 'oj', 'ADMIN', 'icon-park-outline:code-computer', '#0f766e', 3, 'ENABLED', '在线判题管理资源模块', '{}'::json, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    client = EXCLUDED.client,
    icon = EXCLUDED.icon,
    color = EXCLUDED.color,
    sort = EXCLUDED.sort,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    extra = EXCLUDED.extra,
    updated_at = now(),
    updated_by = EXCLUDED.updated_by;

INSERT INTO public.sys_resource (
    id, parent_id, code, name, resource_type, module_id, path, component, redirect, icon, color, href, sort,
    is_visible, is_cache, is_affix, status, description, extra, created_at, created_by, updated_at, updated_by
) VALUES
    ('202000', NULL, 'oj', '在线判题', 'CATALOG', '210003', '/oj', NULL, NULL, 'icon-park-outline:code-computer', NULL, NULL, 20, true, false, false, 'ENABLED', '在线判题管理目录', '{}'::json, now(), NULL, now(), NULL),
    ('202001', '202000', 'oj-problem', '题目管理', 'MENU', '210003', '/oj/problem', '/oj/problem/index.vue', NULL, 'icon-park-outline:book-open', NULL, NULL, 1, true, true, false, 'ENABLED', '题目聚合维护', '{}'::json, now(), NULL, now(), NULL),
    ('202002', '202000', 'oj-contest', '竞赛管理', 'MENU', '210003', '/oj/contest', '/oj/contest/index.vue', NULL, 'icon-park-outline:trophy', NULL, NULL, 2, true, true, false, 'ENABLED', '竞赛聚合维护', '{}'::json, now(), NULL, now(), NULL),
    ('202003', '202000', 'oj-submission', '提交记录', 'MENU', '210003', '/oj/submission', '/oj/submission/index.vue', NULL, 'icon-park-outline:send-email', NULL, NULL, 3, true, true, false, 'ENABLED', '提交记录查询', '{}'::json, now(), NULL, now(), NULL),
    ('202004', '202000', 'oj-judge', '判题管理', 'CATALOG', '210003', '/oj/judge', NULL, NULL, 'icon-park-outline:terminal', NULL, NULL, 4, true, false, false, 'ENABLED', '判题机与任务管理目录', '{}'::json, now(), NULL, now(), NULL),
    ('202005', '202004', 'oj-judge-task', '判题任务', 'MENU', '210003', '/oj/judge/task', '/oj/judge/task/index.vue', NULL, 'icon-park-outline:list-checkbox', NULL, NULL, 1, true, false, false, 'ENABLED', '判题任务管理', '{}'::json, now(), NULL, now(), NULL),
    ('202006', '202004', 'oj-judge-node', '判题节点', 'MENU', '210003', '/oj/judge/node', '/oj/judge/node/index.vue', NULL, 'icon-park-outline:server', NULL, NULL, 2, true, false, false, 'ENABLED', '判题节点管理', '{}'::json, now(), NULL, now(), NULL),
    ('202007', '202004', 'oj-language', '语言配置', 'MENU', '210003', '/oj/judge/language', '/oj/judge/language/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 3, true, false, false, 'ENABLED', '判题语言配置', '{}'::json, now(), NULL, now(), NULL),
    ('202008', '202004', 'oj-runtime-version', '运行时版本', 'MENU', '210003', '/oj/judge/runtime-version', '/oj/judge/runtime_version/index.vue', NULL, 'icon-park-outline:versions', NULL, NULL, 4, true, false, false, 'ENABLED', '运行时版本管理', '{}'::json, now(), NULL, now(), NULL),
    ('202101', '202001', 'oj-problem-create', '新增题目', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202102', '202001', 'oj-problem-detail', '查看题目', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202103', '202001', 'oj-problem-update', '编辑题目', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202104', '202001', 'oj-problem-delete', '删除题目', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202201', '202002', 'oj-contest-create', '新增竞赛', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202202', '202002', 'oj-contest-detail', '查看竞赛', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202203', '202002', 'oj-contest-update', '编辑竞赛', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202204', '202002', 'oj-contest-delete', '删除竞赛', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202301', '202003', 'oj-submission-detail', '查看提交', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202302', '202003', 'oj-submission-delete', '删除提交', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202501', '202005', 'oj-judge-task-detail', '查看判题任务', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202502', '202005', 'oj-judge-task-delete', '删除判题任务', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202601', '202006', 'oj-judge-node-create', '新增判题节点', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202602', '202006', 'oj-judge-node-detail', '查看判题节点', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202603', '202006', 'oj-judge-node-update', '编辑判题节点', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202604', '202006', 'oj-judge-node-delete', '删除判题节点', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202701', '202007', 'oj-language-create', '新增语言', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202702', '202007', 'oj-language-detail', '查看语言', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202703', '202007', 'oj-language-update', '编辑语言', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202704', '202007', 'oj-language-delete', '删除语言', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202801', '202008', 'oj-runtime-version-create', '新增运行时版本', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202802', '202008', 'oj-runtime-version-detail', '查看运行时版本', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202803', '202008', 'oj-runtime-version-update', '编辑运行时版本', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('202804', '202008', 'oj-runtime-version-delete', '删除运行时版本', 'BUTTON', '210003', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    code = EXCLUDED.code,
    name = EXCLUDED.name,
    resource_type = EXCLUDED.resource_type,
    module_id = EXCLUDED.module_id,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    redirect = EXCLUDED.redirect,
    icon = EXCLUDED.icon,
    color = EXCLUDED.color,
    href = EXCLUDED.href,
    sort = EXCLUDED.sort,
    is_visible = EXCLUDED.is_visible,
    is_cache = EXCLUDED.is_cache,
    is_affix = EXCLUDED.is_affix,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    extra = EXCLUDED.extra,
    updated_at = now(),
    updated_by = EXCLUDED.updated_by;

INSERT INTO public.sys_iam_relation (
    id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode,
    effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, reason,
    expired_at, extra, created_at, created_by, updated_at, updated_by
) VALUES
    ('402001', 'RESOURCE', '202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problems:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 1, 'ENABLED', '题目分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402101', 'RESOURCE', '202101', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problems:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 2, 'ENABLED', '新增题目', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402102', 'RESOURCE', '202102', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problems:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 3, 'ENABLED', '查看题目', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402103', 'RESOURCE', '202103', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problems:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 4, 'ENABLED', '编辑题目', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402104', 'RESOURCE', '202104', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problems:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 5, 'ENABLED', '删除题目', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402002', 'RESOURCE', '202002', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:contests:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 10, 'ENABLED', '竞赛分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402201', 'RESOURCE', '202201', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:contests:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 11, 'ENABLED', '新增竞赛', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402202', 'RESOURCE', '202202', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:contests:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 12, 'ENABLED', '查看竞赛', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402203', 'RESOURCE', '202203', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:contests:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 13, 'ENABLED', '编辑竞赛', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402204', 'RESOURCE', '202204', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:contests:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 14, 'ENABLED', '删除竞赛', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402003', 'RESOURCE', '202003', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submissions:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 20, 'ENABLED', '提交分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402301', 'RESOURCE', '202301', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submissions:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 21, 'ENABLED', '查看提交', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402302', 'RESOURCE', '202302', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:submissions:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 22, 'ENABLED', '删除提交', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402005', 'RESOURCE', '202005', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgetasks:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 30, 'ENABLED', '判题任务分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402501', 'RESOURCE', '202501', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgetasks:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 31, 'ENABLED', '查看判题任务', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402502', 'RESOURCE', '202502', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgetasks:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 32, 'ENABLED', '删除判题任务', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402006', 'RESOURCE', '202006', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenodes:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 40, 'ENABLED', '判题节点分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402601', 'RESOURCE', '202601', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenodes:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 41, 'ENABLED', '新增判题节点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402602', 'RESOURCE', '202602', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenodes:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 42, 'ENABLED', '查看判题节点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402603', 'RESOURCE', '202603', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenodes:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 43, 'ENABLED', '编辑判题节点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402604', 'RESOURCE', '202604', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:judgenodes:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 44, 'ENABLED', '删除判题节点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402007', 'RESOURCE', '202007', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:languages:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 50, 'ENABLED', '语言分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402701', 'RESOURCE', '202701', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:languages:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 51, 'ENABLED', '新增语言', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402702', 'RESOURCE', '202702', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:languages:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 52, 'ENABLED', '查看语言', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402703', 'RESOURCE', '202703', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:languages:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 53, 'ENABLED', '编辑语言', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402704', 'RESOURCE', '202704', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:languages:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 54, 'ENABLED', '删除语言', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402008', 'RESOURCE', '202008', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:runtimeversions:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 60, 'ENABLED', '运行时版本分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402801', 'RESOURCE', '202801', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:runtimeversions:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 61, 'ENABLED', '新增运行时版本', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402802', 'RESOURCE', '202802', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:runtimeversions:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 62, 'ENABLED', '查看运行时版本', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402803', 'RESOURCE', '202803', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:runtimeversions:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 63, 'ENABLED', '编辑运行时版本', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('402804', 'RESOURCE', '202804', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:runtimeversions:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 64, 'ENABLED', '删除运行时版本', NULL, NULL, '{}'::json, now(), NULL, now(), NULL)
ON CONFLICT ON CONSTRAINT uq_sys_iam_relation_subject_relation_target DO UPDATE SET
    target_key = EXCLUDED.target_key,
    grant_mode = EXCLUDED.grant_mode,
    effect = EXCLUDED.effect,
    data_scope = EXCLUDED.data_scope,
    custom_scope_dept_ids = EXCLUDED.custom_scope_dept_ids,
    is_primary = EXCLUDED.is_primary,
    sort = EXCLUDED.sort,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    extra = EXCLUDED.extra,
    updated_at = now(),
    updated_by = EXCLUDED.updated_by;

INSERT INTO public.sys_iam_relation (
    id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode,
    effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, reason,
    expired_at, extra, created_at, created_by, updated_at, updated_by
)
SELECT
    '302' || lpad(row_number() OVER (ORDER BY resource.id)::text, 3, '0') AS id,
    'ROLE',
    '1',
    'SUBJECT_RESOURCE_GRANT',
    'RESOURCE',
    resource.id,
    '',
    'CASCADE',
    'ALLOW',
    'SELF',
    '[]'::json,
    false,
    99,
    'ENABLED',
    '超管角色默认资源授权',
    NULL,
    NULL,
    '{}'::json,
    now(),
    NULL,
    now(),
    NULL
FROM public.sys_resource resource
WHERE resource.id IN (
    '202000', '202001', '202002', '202003', '202004', '202005', '202006', '202007', '202008',
    '202101', '202102', '202103', '202104',
    '202201', '202202', '202203', '202204',
    '202301', '202302',
    '202501', '202502',
    '202601', '202602', '202603', '202604',
    '202701', '202702', '202703', '202704',
    '202801', '202802', '202803', '202804'
)
ON CONFLICT ON CONSTRAINT uq_sys_iam_relation_subject_relation_target DO UPDATE SET
    grant_mode = EXCLUDED.grant_mode,
    effect = EXCLUDED.effect,
    data_scope = EXCLUDED.data_scope,
    custom_scope_dept_ids = EXCLUDED.custom_scope_dept_ids,
    is_primary = EXCLUDED.is_primary,
    sort = EXCLUDED.sort,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    extra = EXCLUDED.extra,
    updated_at = now(),
    updated_by = EXCLUDED.updated_by;

COMMIT;
