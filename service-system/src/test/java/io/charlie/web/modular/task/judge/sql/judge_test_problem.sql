-- Judge module JUnit test data
-- Run this script on the test DB, then execute JudgeModuleTest
-- User: junit_judge / junit123456
-- Problem: junit_judge_a_plus_b (A + B)

-- ----------------------------
-- Cleanup (idempotent)
-- ----------------------------
DELETE FROM `data_test_case` WHERE `problem_id` = 'junit_judge_a_plus_b';
DELETE FROM `data_problem` WHERE `id` = 'junit_judge_a_plus_b';
DELETE FROM `sys_user_role` WHERE `user_id` = 'junit_judge_user';
DELETE FROM `sys_user` WHERE `id` = 'junit_judge_user' OR `username` = 'junit_judge';

-- ----------------------------
-- Test user (normal USER / CLIENT)
-- Plain password: junit123456
-- BCrypt: $2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO
-- ----------------------------
INSERT INTO `sys_user` (
    `id`, `group_id`, `username`, `password`, `nickname`, `avatar`, `background`,
    `quote`, `gender`, `email`, `student_number`, `telephone`, `login_time`,
    `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_judge_user',
    '1',
    'junit_judge',
    '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
    'Judge Test User',
    NULL,
    NULL,
    'JudgeModuleTest',
    0,
    'junit_judge@test.local',
    NULL,
    NULL,
    NULL,
    0,
    NOW(),
    '0',
    NOW(),
    '0'
);

-- Bind USER role (sys_role.id = 3, code = USER)
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
('junit_judge_user_role', 'junit_judge_user', '3');

-- ----------------------------
-- Problem: A + B Problem
-- max_time=1000(ms)  max_memory=65536(KB)
-- ----------------------------
INSERT INTO `data_problem` (
    `id`, `display_id`, `category_id`, `title`, `source`, `url`,
    `max_time`, `max_memory`, `description`, `test_case`, `allowed_languages`,
    `difficulty`, `threshold`, `use_template`, `code_template`,
    `is_public`, `is_visible`, `use_ai`, `solved`,
    `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_judge_a_plus_b',
    'J-AB',
    '0',
    'A + B Problem (Judge Test)',
    'JUnit',
    NULL,
    1000,
    65536,
    'Compute the sum of two integers.\n\n## Input\nOne line with two integers A and B (-10^9 ≤ A,B ≤ 10^9).\n\n## Output\nPrint A + B.\n\n## Sample Input\n1 2\n\n## Sample Output\n3',
    JSON_ARRAY(
        JSON_OBJECT('input', '1 2', 'output', '3'),
        JSON_OBJECT('input', '100 200', 'output', '300')
    ),
    JSON_ARRAY('c', 'cpp', 'java', 'go', 'python'),
    1,
    0.50,
    0,
    NULL,
    1,
    1,
    0,
    0,
    0,
    NOW(),
    'junit_judge_user',
    NOW(),
    'junit_judge_user'
);

-- ----------------------------
-- Test cases
-- is_sample=1: sample; is_sample=0: formal cases (used when submitType=true)
-- ----------------------------
INSERT INTO `data_test_case` (
    `id`, `problem_id`, `case_sign`, `input_data`, `expected_output`,
    `input_file_path`, `input_file_size`, `output_file_path`, `output_file_size`,
    `is_sample`, `score`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES
(
    'junit_judge_case_s01',
    'junit_judge_a_plus_b',
    'sample-01',
    '1 2\n',
    '3\n',
    NULL, 0, NULL, 0,
    1, 0, 0,
    NOW(3), 'junit_judge_user', NOW(3), 'junit_judge_user'
),
(
    'junit_judge_case_01',
    'junit_judge_a_plus_b',
    'case-01',
    '1 2\n',
    '3\n',
    NULL, 0, NULL, 0,
    0, 50, 0,
    NOW(3), 'junit_judge_user', NOW(3), 'junit_judge_user'
),
(
    'junit_judge_case_02',
    'junit_judge_a_plus_b',
    'case-02',
    '100 200\n',
    '300\n',
    NULL, 0, NULL, 0,
    0, 50, 0,
    NOW(3), 'junit_judge_user', NOW(3), 'junit_judge_user'
),
(
    'junit_judge_case_03',
    'junit_judge_a_plus_b',
    'case-03',
    '-1 1\n',
    '0\n',
    NULL, 0, NULL, 0,
    0, 0, 0,
    NOW(3), 'junit_judge_user', NOW(3), 'junit_judge_user'
);
