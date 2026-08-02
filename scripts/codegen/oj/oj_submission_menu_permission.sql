-- Manual seed for biz.submission admin menu/permissions (static routes also wired).
BEGIN;

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, description, extra)
VALUES (
  '203300',
  '202030',
  'biz-submission',
  '提交管理',
  'CATALOG',
  NULLIF(NULL, ''),
  '/biz/submission',
  NULL,
  'icon-park-outline:upload-logs',
  3,
  true,
  false,
  false,
  'ENABLED',
  '提交管理',
  '{}'
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name, path = EXCLUDED.path, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, icon, sort, is_visible, is_cache, is_affix, status, description, extra)
VALUES (
  '203301',
  '203300',
  'biz_submission_submission',
  '提交',
  'MENU',
  NULLIF(NULL, ''),
  '/biz/submission/submission',
  '/biz/submission/submission/index.vue',
  'icon-park-outline:list',
  10,
  true,
  false,
  false,
  'ENABLED',
  NULL,
  '{}'
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name, path = EXCLUDED.path, component = EXCLUDED.component, updated_at = now();

INSERT INTO sys_resource (id, parent_id, code, name, resource_type, module_id, sort, is_visible, is_cache, is_affix, status, extra)
VALUES
  ('203302', '203301', 'biz_submission_submission_page', '分页提交', 'BUTTON', NULLIF(NULL, ''), 10, false, false, false, 'ENABLED', '{}'),
  ('203303', '203301', 'biz_submission_submission_detail', '详情提交', 'BUTTON', NULLIF(NULL, ''), 20, false, false, false, 'ENABLED', '{}'),
  ('203304', '203301', 'biz_submission_submission_delete', '删除提交', 'BUTTON', NULLIF(NULL, ''), 30, false, false, false, 'ENABLED', '{}'),
  ('203305', '203301', 'biz_submission_submission_rejudge', '重判提交', 'BUTTON', NULLIF(NULL, ''), 40, false, false, false, 'ENABLED', '{}')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_at = now();

INSERT INTO sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, extra)
VALUES
  ('203312', 'RESOURCE', '203302', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:submission:submission:page', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 10, 'ENABLED', '分页提交', '{}'),
  ('203313', 'RESOURCE', '203303', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:submission:submission:detail', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 20, 'ENABLED', '详情提交', '{}'),
  ('203314', 'RESOURCE', '203304', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:submission:submission:delete', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 30, 'ENABLED', '删除提交', '{}'),
  ('203315', 'RESOURCE', '203305', 'RESOURCE_PERMISSION', 'PERMISSION', '', 'biz:submission:submission:rejudge', 'CASCADE', 'ALLOW', 'ALL', '[]', false, 40, 'ENABLED', '重判提交', '{}')
ON CONFLICT (id)
DO UPDATE SET description = EXCLUDED.description, target_key = EXCLUDED.target_key, updated_at = now();

COMMIT;
