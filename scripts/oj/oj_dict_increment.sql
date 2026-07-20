-- OJ 枚举字典增量脚本
-- 为 OJ 模块创建字典条目，便于前端 DictSelect 组件使用
-- 幂等可重复执行

BEGIN;

-- OJ_PROBLEM_TYPE
INSERT INTO public.sys_dict VALUES ('5210001', 'OJ_PROBLEM_TYPE', '题目类型', 'OJ_PROBLEM_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210002', 'OJ_PROBLEM_TYPE_PROGRAM', '编程题', 'PROGRAM', '#2080f0', 'SYS', '5210001', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210003', 'OJ_PROBLEM_TYPE_OUTPUT_ONLY', '输出题', 'OUTPUT_ONLY', '#18a058', 'SYS', '5210001', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210004', 'OJ_PROBLEM_TYPE_FUNCTION', '函数题', 'FUNCTION', '#722ed1', 'SYS', '5210001', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210005', 'OJ_PROBLEM_TYPE_INTERACTIVE', '交互题', 'INTERACTIVE', '#f0a020', 'SYS', '5210001', 'ENABLED', 4, now(), NULL, now(), NULL),
  ('5210006', 'OJ_PROBLEM_TYPE_OBJECTIVE', '客观题', 'OBJECTIVE', '#d03050', 'SYS', '5210001', 'ENABLED', 5, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

-- OJ_JUDGE_MODE
INSERT INTO public.sys_dict VALUES ('5210011', 'OJ_JUDGE_MODE', '判题方式', 'OJ_JUDGE_MODE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210012', 'OJ_JUDGE_MODE_STANDARD', '标准模式', 'STANDARD', '#18a058', 'SYS', '5210011', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210013', 'OJ_JUDGE_MODE_SPECIAL_JUDGE', '特殊评测', 'SPECIAL_JUDGE', '#722ed1', 'SYS', '5210011', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210014', 'OJ_JUDGE_MODE_INTERACTIVE', '交互评测', 'INTERACTIVE', '#f0a020', 'SYS', '5210011', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210015', 'OJ_JUDGE_MODE_OUTPUT_ONLY', '仅输出', 'OUTPUT_ONLY', '#909399', 'SYS', '5210011', 'ENABLED', 4, now(), NULL, now(), NULL),
  ('5210016', 'OJ_JUDGE_MODE_FUNCTION', '函数评测', 'FUNCTION', '#2db7f5', 'SYS', '5210011', 'ENABLED', 5, now(), NULL, now(), NULL),
  ('5210017', 'OJ_JUDGE_MODE_OBJECTIVE', '客观题评测', 'OBJECTIVE', '#d03050', 'SYS', '5210011', 'ENABLED', 6, now(), NULL, now(), NULL),
  ('5210018', 'OJ_JUDGE_MODE_REMOTE', '远程评测', 'REMOTE', '#eb2f96', 'SYS', '5210011', 'ENABLED', 7, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

-- OJ_PROBLEM_VISIBILITY
INSERT INTO public.sys_dict VALUES ('5210021', 'OJ_PROBLEM_VISIBILITY', '题目可见性', 'OJ_PROBLEM_VISIBILITY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210022', 'OJ_PROBLEM_VISIBILITY_PUBLIC', '公开', 'PUBLIC', '#18a058', 'SYS', '5210021', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210023', 'OJ_PROBLEM_VISIBILITY_PRIVATE', '私有', 'PRIVATE', '#d03050', 'SYS', '5210021', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210024', 'OJ_PROBLEM_VISIBILITY_CONTEST_ONLY', '仅比赛', 'CONTEST_ONLY', '#f0a020', 'SYS', '5210021', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210025', 'OJ_PROBLEM_VISIBILITY_ORG_ONLY', '仅组织', 'ORG_ONLY', '#722ed1', 'SYS', '5210021', 'ENABLED', 4, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

-- OJ_PROBLEM_MEMBER_ROLE
INSERT INTO public.sys_dict VALUES ('5210031', 'OJ_PROBLEM_MEMBER_ROLE', '题目成员角色', 'OJ_PROBLEM_MEMBER_ROLE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210032', 'OJ_PROBLEM_MEMBER_ROLE_AUTHOR', '作者', 'AUTHOR', '#2080f0', 'SYS', '5210031', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210033', 'OJ_PROBLEM_MEMBER_ROLE_CURATOR', '维护者', 'CURATOR', '#18a058', 'SYS', '5210031', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210034', 'OJ_PROBLEM_MEMBER_ROLE_TESTER', '测试者', 'TESTER', '#f0a020', 'SYS', '5210031', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210035', 'OJ_PROBLEM_MEMBER_ROLE_VIEWER', '只读', 'VIEWER', '#909399', 'SYS', '5210031', 'ENABLED', 4, now(), NULL, now(), NULL),
  ('5210036', 'OJ_PROBLEM_MEMBER_ROLE_BANNED', '封禁', 'BANNED', '#d03050', 'SYS', '5210031', 'ENABLED', 5, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

-- OJ_TEST_CASE_TYPE
INSERT INTO public.sys_dict VALUES ('5210041', 'OJ_TEST_CASE_TYPE', '测试点类型', 'OJ_TEST_CASE_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210042', 'OJ_TEST_CASE_TYPE_NORMAL', '普通', 'NORMAL', '#18a058', 'SYS', '5210041', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210043', 'OJ_TEST_CASE_TYPE_PRETEST', '预测试', 'PRETEST', '#f0a020', 'SYS', '5210041', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210044', 'OJ_TEST_CASE_TYPE_GENERATED', '生成', 'GENERATED', '#722ed1', 'SYS', '5210041', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210045', 'OJ_TEST_CASE_TYPE_MANUAL', '手动', 'MANUAL', '#909399', 'SYS', '5210041', 'ENABLED', 4, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

-- OJ_OBJECTIVE_ANSWER_TYPE
INSERT INTO public.sys_dict VALUES ('5210051', 'OJ_OBJECTIVE_ANSWER_TYPE', '客观题答案类型', 'OJ_OBJECTIVE_ANSWER_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, now(), NULL, now(), NULL),
  ('5210052', 'OJ_OBJECTIVE_ANSWER_TYPE_SINGLE', '单选', 'SINGLE', '#2080f0', 'SYS', '5210051', 'ENABLED', 1, now(), NULL, now(), NULL),
  ('5210053', 'OJ_OBJECTIVE_ANSWER_TYPE_MULTIPLE', '多选', 'MULTIPLE', '#722ed1', 'SYS', '5210051', 'ENABLED', 2, now(), NULL, now(), NULL),
  ('5210054', 'OJ_OBJECTIVE_ANSWER_TYPE_FILL', '填空', 'FILL', '#18a058', 'SYS', '5210051', 'ENABLED', 3, now(), NULL, now(), NULL),
  ('5210055', 'OJ_OBJECTIVE_ANSWER_TYPE_TRUE_FALSE', '判断', 'TRUE_FALSE', '#f0a020', 'SYS', '5210051', 'ENABLED', 4, now(), NULL, now(), NULL)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, value = EXCLUDED.value, color = EXCLUDED.color, category = EXCLUDED.category, parent_id = EXCLUDED.parent_id, status = EXCLUDED.status, sort = EXCLUDED.sort, updated_at = now();

COMMIT;
