-- Teaching management menus + permissions for clazz/course/team admin modules.
-- Parent catalog 203900 under biz catalog 202030.
-- Adjust module_id before executing when needed.
BEGIN;

-- Catalog: 教学管理
INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, description, extra)
VALUES (
  '203900',
  '202030',
  'biz-teach',
  '教学管理',
  'CATALOG',
  NULLIF(NULL, ''),
  '/biz/teach',
  NULL,
  'icon-park-outline:school',
  4,
  true,
  false,
  false,
  'ENABLED',
  '教学管理',
  '{}'
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name, path = EXCLUDED.path, updated_at = now();

-- ========== Clazz ==========
INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, extra)
VALUES (
  '203901',
  '203900',
  'biz_clazz',
  '班级管理',
  'MENU',
  NULLIF(NULL, ''),
  '/biz/clazz',
  '/biz/clazz/index.vue',
  'icon-park-outline:peoples',
  10,
  true,
  false,
  false,
  'ENABLED',
  '{}'
)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, path = EXCLUDED.path, component = EXCLUDED.component, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170010000001', '203901', 'biz_clazz_page', '分页班级', 'BUTTON', NULLIF(NULL, ''), 10, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170010000011', 'RESOURCE', '7487852170010000001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:clazz:page', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 10, 'ENABLED', '分页班级', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170010000002', '203901', 'biz_clazz_create', '新增班级', 'BUTTON', NULLIF(NULL, ''), 20, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170010000012', 'RESOURCE', '7487852170010000002', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:clazz:create', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 20, 'ENABLED', '新增班级', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170010000003', '203901', 'biz_clazz_detail', '详情班级', 'BUTTON', NULLIF(NULL, ''), 30, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170010000013', 'RESOURCE', '7487852170010000003', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:clazz:detail', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 30, 'ENABLED', '详情班级', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170010000004', '203901', 'biz_clazz_update', '编辑班级', 'BUTTON', NULLIF(NULL, ''), 40, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170010000014', 'RESOURCE', '7487852170010000004', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:clazz:update', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 40, 'ENABLED', '编辑班级', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170010000005', '203901', 'biz_clazz_delete', '删除班级', 'BUTTON', NULLIF(NULL, ''), 50, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170010000015', 'RESOURCE', '7487852170010000005', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:clazz:delete', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 50, 'ENABLED', '删除班级', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

-- ========== Course ==========
INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, extra)
VALUES (
  '203902',
  '203900',
  'biz_course',
  '课程管理',
  'MENU',
  NULLIF(NULL, ''),
  '/biz/course',
  '/biz/course/index.vue',
  'icon-park-outline:book',
  20,
  true,
  false,
  false,
  'ENABLED',
  '{}'
)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, path = EXCLUDED.path, component = EXCLUDED.component, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('203904', '203902', 'biz_course_detail_page', '课程详情页', 'MENU', NULLIF(NULL, ''), '/biz/course/detail', '/biz/course/detail.vue', 25, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, path = EXCLUDED.path, component = EXCLUDED.component, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170020000001', '203902', 'biz_course_page', '分页课程', 'BUTTON', NULLIF(NULL, ''), 10, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170020000011', 'RESOURCE', '7487852170020000001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:course:page', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 10, 'ENABLED', '分页课程', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170020000002', '203902', 'biz_course_create', '新增课程', 'BUTTON', NULLIF(NULL, ''), 20, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170020000012', 'RESOURCE', '7487852170020000002', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:course:create', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 20, 'ENABLED', '新增课程', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170020000003', '203902', 'biz_course_detail', '详情课程', 'BUTTON', NULLIF(NULL, ''), 30, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170020000013', 'RESOURCE', '7487852170020000003', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:course:detail', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 30, 'ENABLED', '详情课程', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170020000004', '203902', 'biz_course_update', '编辑课程', 'BUTTON', NULLIF(NULL, ''), 40, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170020000014', 'RESOURCE', '7487852170020000004', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:course:update', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 40, 'ENABLED', '编辑课程', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170020000005', '203902', 'biz_course_delete', '删除课程', 'BUTTON', NULLIF(NULL, ''), 50, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170020000015', 'RESOURCE', '7487852170020000005', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:course:delete', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 50, 'ENABLED', '删除课程', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

-- ========== Team ==========
INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, extra)
VALUES (
  '203903',
  '203900',
  'biz_team',
  '小组管理',
  'MENU',
  NULLIF(NULL, ''),
  '/biz/team',
  '/biz/team/index.vue',
  'icon-park-outline:peoples-two',
  30,
  true,
  false,
  false,
  'ENABLED',
  '{}'
)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, path = EXCLUDED.path, component = EXCLUDED.component, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170030000001', '203903', 'biz_team_page', '分页小组', 'BUTTON', NULLIF(NULL, ''), 10, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170030000011', 'RESOURCE', '7487852170030000001', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:team:page', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 10, 'ENABLED', '分页小组', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170030000002', '203903', 'biz_team_create', '新增小组', 'BUTTON', NULLIF(NULL, ''), 20, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170030000012', 'RESOURCE', '7487852170030000002', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:team:create', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 20, 'ENABLED', '新增小组', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170030000003', '203903', 'biz_team_detail', '详情小组', 'BUTTON', NULLIF(NULL, ''), 30, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170030000013', 'RESOURCE', '7487852170030000003', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:team:detail', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 30, 'ENABLED', '详情小组', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170030000004', '203903', 'biz_team_update', '编辑小组', 'BUTTON', NULLIF(NULL, ''), 40, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170030000014', 'RESOURCE', '7487852170030000004', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:team:update', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 40, 'ENABLED', '编辑小组', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES ('7487852170030000005', '203903', 'biz_team_delete', '删除小组', 'BUTTON', NULLIF(NULL, ''), 50, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();
INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES ('7487852170030000015', 'RESOURCE', '7487852170030000005', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:team:delete', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 50, 'ENABLED', '删除小组', '{}')
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description, updated_at = now();

COMMIT;
