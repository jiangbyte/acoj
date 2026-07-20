-- OJ Problem 子实体权限增量脚本
-- 为 8 个子实体（tag, sample, dataset, test_case, tag_relation, asset, member, objective_answer）
-- 注册 BUTTON 资源、RESOURCE_PERMISSION 关系、超管角色授权
-- 幂等可重复执行

BEGIN;

-- ============================
-- 1. 注册 BUTTON 资源
-- ============================
INSERT INTO public.sys_resource (
    id, parent_id, code, name, resource_type, module_id, path, component, redirect, icon, color, href, sort,
    is_visible, is_cache, is_affix, status, description, extra, created_at, created_by, updated_at, updated_by
) VALUES
    -- tag
    ('5212001', '5202001', 'oj-problem-tag-create', '新增标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212002', '5202001', 'oj-problem-tag-detail', '查看标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 11, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212003', '5202001', 'oj-problem-tag-update', '编辑标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 12, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212004', '5202001', 'oj-problem-tag-delete', '删除标签', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 13, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- sample
    ('5212011', '5202001', 'oj-problem-sample-create', '新增样例', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212012', '5202001', 'oj-problem-sample-detail', '查看样例', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 21, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212013', '5202001', 'oj-problem-sample-update', '编辑样例', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 22, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212014', '5202001', 'oj-problem-sample-delete', '删除样例', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 23, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- dataset
    ('5212021', '5202001', 'oj-dataset-create', '新增数据集', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212022', '5202001', 'oj-dataset-detail', '查看数据集', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 31, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212023', '5202001', 'oj-dataset-update', '编辑数据集', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 32, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212024', '5202001', 'oj-dataset-delete', '删除数据集', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 33, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- test_case
    ('5212031', '5202001', 'oj-test-case-create', '新增测试点', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212032', '5202001', 'oj-test-case-detail', '查看测试点', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 41, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212033', '5202001', 'oj-test-case-update', '编辑测试点', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 42, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212034', '5202001', 'oj-test-case-delete', '删除测试点', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 43, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- tag_relation
    ('5212041', '5202001', 'oj-problem-tag-relation-create', '新增标签关联', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212042', '5202001', 'oj-problem-tag-relation-detail', '查看标签关联', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 51, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212043', '5202001', 'oj-problem-tag-relation-update', '编辑标签关联', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 52, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212044', '5202001', 'oj-problem-tag-relation-delete', '删除标签关联', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 53, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- asset
    ('5212051', '5202001', 'oj-problem-asset-create', '新增附件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212052', '5202001', 'oj-problem-asset-detail', '查看附件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 61, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212053', '5202001', 'oj-problem-asset-update', '编辑附件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 62, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212054', '5202001', 'oj-problem-asset-delete', '删除附件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 63, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- member
    ('5212061', '5202001', 'oj-problem-member-create', '新增协作者', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212062', '5202001', 'oj-problem-member-detail', '查看协作者', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 71, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212063', '5202001', 'oj-problem-member-update', '编辑协作者', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 72, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212064', '5202001', 'oj-problem-member-delete', '删除协作者', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 73, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    -- objective_answer
    ('5212071', '5202001', 'oj-objective-answer-create', '新增客观题答案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 80, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212072', '5202001', 'oj-objective-answer-detail', '查看客观题答案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 81, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212073', '5202001', 'oj-objective-answer-update', '编辑客观题答案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 82, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5212074', '5202001', 'oj-objective-answer-delete', '删除客观题答案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 83, true, false, false, 'ENABLED', NULL, '{}'::json, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id, code = EXCLUDED.code, name = EXCLUDED.name,
    resource_type = EXCLUDED.resource_type, module_id = EXCLUDED.module_id, path = EXCLUDED.path,
    component = EXCLUDED.component, redirect = EXCLUDED.redirect, icon = EXCLUDED.icon,
    color = EXCLUDED.color, href = EXCLUDED.href, sort = EXCLUDED.sort,
    is_visible = EXCLUDED.is_visible, is_cache = EXCLUDED.is_cache, is_affix = EXCLUDED.is_affix,
    status = EXCLUDED.status, description = EXCLUDED.description, extra = EXCLUDED.extra,
    updated_at = now(), updated_by = EXCLUDED.updated_by;

-- ============================
-- 2. 注册 RESOURCE_PERMISSION 关系
-- ============================
INSERT INTO public.sys_iam_relation (
    id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode,
    effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, reason,
    expired_at, extra, created_at, created_by, updated_at, updated_by
) VALUES
    -- tag
    ('5412001', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtags:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 100, 'ENABLED', '标签分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412002', 'RESOURCE', '5212001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtags:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 101, 'ENABLED', '新增标签', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412003', 'RESOURCE', '5212002', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtags:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 102, 'ENABLED', '查看标签', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412004', 'RESOURCE', '5212003', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtags:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 103, 'ENABLED', '编辑标签', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412005', 'RESOURCE', '5212004', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtags:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 104, 'ENABLED', '删除标签', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- sample
    ('5412011', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemsamples:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 110, 'ENABLED', '样例分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412012', 'RESOURCE', '5212011', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemsamples:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 111, 'ENABLED', '新增样例', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412013', 'RESOURCE', '5212012', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemsamples:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 112, 'ENABLED', '查看样例', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412014', 'RESOURCE', '5212013', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemsamples:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 113, 'ENABLED', '编辑样例', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412015', 'RESOURCE', '5212014', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemsamples:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 114, 'ENABLED', '删除样例', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- dataset
    ('5412021', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:datasets:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 120, 'ENABLED', '数据集分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412022', 'RESOURCE', '5212021', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:datasets:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 121, 'ENABLED', '新增数据集', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412023', 'RESOURCE', '5212022', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:datasets:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 122, 'ENABLED', '查看数据集', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412024', 'RESOURCE', '5212023', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:datasets:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 123, 'ENABLED', '编辑数据集', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412025', 'RESOURCE', '5212024', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:datasets:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 124, 'ENABLED', '删除数据集', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- test_case
    ('5412031', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:testcases:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 130, 'ENABLED', '测试点分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412032', 'RESOURCE', '5212031', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:testcases:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 131, 'ENABLED', '新增测试点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412033', 'RESOURCE', '5212032', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:testcases:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 132, 'ENABLED', '查看测试点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412034', 'RESOURCE', '5212033', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:testcases:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 133, 'ENABLED', '编辑测试点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412035', 'RESOURCE', '5212034', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:testcases:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 134, 'ENABLED', '删除测试点', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- tag_relation
    ('5412041', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtagrelations:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 140, 'ENABLED', '标签关联分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412042', 'RESOURCE', '5212041', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtagrelations:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 141, 'ENABLED', '新增标签关联', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412043', 'RESOURCE', '5212042', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtagrelations:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 142, 'ENABLED', '查看标签关联', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412044', 'RESOURCE', '5212043', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtagrelations:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 143, 'ENABLED', '编辑标签关联', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412045', 'RESOURCE', '5212044', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemtagrelations:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 144, 'ENABLED', '删除标签关联', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- asset
    ('5412051', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemassets:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 150, 'ENABLED', '附件分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412052', 'RESOURCE', '5212051', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemassets:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 151, 'ENABLED', '新增附件', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412053', 'RESOURCE', '5212052', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemassets:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 152, 'ENABLED', '查看附件', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412054', 'RESOURCE', '5212053', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemassets:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 153, 'ENABLED', '编辑附件', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412055', 'RESOURCE', '5212054', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemassets:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 154, 'ENABLED', '删除附件', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- member
    ('5412061', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemmembers:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 160, 'ENABLED', '协作者分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412062', 'RESOURCE', '5212061', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemmembers:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 161, 'ENABLED', '新增协作者', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412063', 'RESOURCE', '5212062', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemmembers:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 162, 'ENABLED', '查看协作者', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412064', 'RESOURCE', '5212063', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemmembers:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 163, 'ENABLED', '编辑协作者', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412065', 'RESOURCE', '5212064', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:problemmembers:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 164, 'ENABLED', '删除协作者', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    -- objective_answer
    ('5412071', 'RESOURCE', '5202001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:objectiveanswers:page', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 170, 'ENABLED', '客观题答案分页', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412072', 'RESOURCE', '5212071', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:objectiveanswers:create', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 171, 'ENABLED', '新增客观题答案', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412073', 'RESOURCE', '5212072', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:objectiveanswers:detail', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 172, 'ENABLED', '查看客观题答案', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412074', 'RESOURCE', '5212073', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:objectiveanswers:update', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 173, 'ENABLED', '编辑客观题答案', NULL, NULL, '{}'::json, now(), NULL, now(), NULL),
    ('5412075', 'RESOURCE', '5212074', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'oj:objectiveanswers:delete', 'CASCADE', 'ALLOW', 'ALL', '[]'::json, false, 174, 'ENABLED', '删除客观题答案', NULL, NULL, '{}'::json, now(), NULL, now(), NULL)
ON CONFLICT ON CONSTRAINT uq_sys_iam_relation_subject_relation_target DO UPDATE SET
    target_key = EXCLUDED.target_key, grant_mode = EXCLUDED.grant_mode, effect = EXCLUDED.effect,
    data_scope = EXCLUDED.data_scope, custom_scope_dept_ids = EXCLUDED.custom_scope_dept_ids,
    is_primary = EXCLUDED.is_primary, sort = EXCLUDED.sort, status = EXCLUDED.status,
    description = EXCLUDED.description, extra = EXCLUDED.extra,
    updated_at = now(), updated_by = EXCLUDED.updated_by;

-- ============================
-- 3. 超管角色授权子实体资源
-- ============================
INSERT INTO public.sys_iam_relation (
    id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode,
    effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, reason,
    expired_at, extra, created_at, created_by, updated_at, updated_by
)
SELECT
    '53' || lpad((row_number() OVER (ORDER BY resource.id)::int + 300)::text, 3, '0') AS id,
    'ROLE', '1', 'SUBJECT_RESOURCE_GRANT', 'RESOURCE', resource.id, '',
    'CASCADE', 'ALLOW', 'SELF', '[]'::json, false, 99, 'ENABLED',
    '超管角色默认资源授权', NULL, NULL, '{}'::json, now(), NULL, now(), NULL
FROM public.sys_resource resource
WHERE resource.id IN (
    '5212001', '5212002', '5212003', '5212004',
    '5212011', '5212012', '5212013', '5212014',
    '5212021', '5212022', '5212023', '5212024',
    '5212031', '5212032', '5212033', '5212034',
    '5212041', '5212042', '5212043', '5212044',
    '5212051', '5212052', '5212053', '5212054',
    '5212061', '5212062', '5212063', '5212064',
    '5212071', '5212072', '5212073', '5212074'
)
ON CONFLICT ON CONSTRAINT uq_sys_iam_relation_subject_relation_target DO UPDATE SET
    grant_mode = EXCLUDED.grant_mode, effect = EXCLUDED.effect, data_scope = EXCLUDED.data_scope,
    custom_scope_dept_ids = EXCLUDED.custom_scope_dept_ids, is_primary = EXCLUDED.is_primary,
    sort = EXCLUDED.sort, status = EXCLUDED.status, description = EXCLUDED.description,
    extra = EXCLUDED.extra, updated_at = now(), updated_by = EXCLUDED.updated_by;

COMMIT;
