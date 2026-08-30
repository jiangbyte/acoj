-- Similarity module JUnit test data (C_01 ~ C_04)
-- Run this script on the test DB, then execute SimilarityModuleTest
-- Primary user: junit_sim / junit123456
-- Problem: junit_sim_sum (array sum, for library pairing)
--
-- Samples (all C++):
--   BASE / C_01: identical
--   C_02: completely different (string greeting, no loop/array)
--   C_03: renamed variables/functions only
--   C_04: added comments and whitespace

-- ----------------------------
-- Cleanup (idempotent)
-- ----------------------------
DELETE FROM `data_library` WHERE `problem_id` = 'junit_sim_sum'
   OR `id` LIKE 'junit_sim_lib_%';
DELETE FROM `data_test_case` WHERE `problem_id` = 'junit_sim_sum';
DELETE FROM `data_problem` WHERE `id` = 'junit_sim_sum';
DELETE FROM `sys_user_role` WHERE `user_id` LIKE 'junit_sim%';
DELETE FROM `sys_user` WHERE `id` LIKE 'junit_sim%' OR `username` LIKE 'junit_sim%';

-- ----------------------------
-- Test users
-- Plain password for all: junit123456
-- ----------------------------
INSERT INTO `sys_user` (
    `id`, `group_id`, `username`, `password`, `nickname`, `avatar`, `background`,
    `quote`, `gender`, `email`, `student_number`, `telephone`, `login_time`,
    `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES
('junit_sim_user', '1', 'junit_sim',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Test User', NULL, NULL, 'SimilarityModuleTest', 0,
 'junit_sim@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0'),
('junit_sim_u_base', '1', 'junit_sim_base',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Sample - Base', NULL, NULL, 'BASE', 0,
 'junit_sim_base@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0'),
('junit_sim_u_c01', '1', 'junit_sim_c01',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Sample - C01 Identical', NULL, NULL, 'C_01', 0,
 'junit_sim_c01@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0'),
('junit_sim_u_c02', '1', 'junit_sim_c02',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Sample - C02 Different', NULL, NULL, 'C_02', 0,
 'junit_sim_c02@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0'),
('junit_sim_u_c03', '1', 'junit_sim_c03',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Sample - C03 Rename', NULL, NULL, 'C_03', 0,
 'junit_sim_c03@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0'),
('junit_sim_u_c04', '1', 'junit_sim_c04',
 '$2a$10$QD.NTjOqLUNez0Be6Gx.DexkDj9aqdZ2rrB397XsIRqfabJRY4BeO',
 'Similarity Sample - C04 Format', NULL, NULL, 'C_04', 0,
 'junit_sim_c04@test.local', NULL, NULL, NULL,
 0, NOW(), '0', NOW(), '0');

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
('junit_sim_role', 'junit_sim_user', '3'),
('junit_sim_role_base', 'junit_sim_u_base', '3'),
('junit_sim_role_c01', 'junit_sim_u_c01', '3'),
('junit_sim_role_c02', 'junit_sim_u_c02', '3'),
('junit_sim_role_c03', 'junit_sim_u_c03', '3'),
('junit_sim_role_c04', 'junit_sim_u_c04', '3');

-- ----------------------------
-- Problem (threshold=0.50, system default; C_02 asserts < 0.3)
-- ----------------------------
INSERT INTO `data_problem` (
    `id`, `display_id`, `category_id`, `title`, `source`, `url`,
    `max_time`, `max_memory`, `description`, `test_case`, `allowed_languages`,
    `difficulty`, `threshold`, `use_template`, `code_template`,
    `is_public`, `is_visible`, `use_ai`, `solved`,
    `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_sum',
    'J-SIM',
    '0',
    'Array Sum (Similarity Test)',
    'JUnit',
    NULL,
    1000,
    65536,
    'Write a function that sums elements of an integer array. Used as similarity module test material.',
    JSON_ARRAY(JSON_OBJECT('input', '5\n1 2 3 4 5', 'output', '15')),
    JSON_ARRAY('cpp', 'c', 'java', 'go', 'python'),
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
    'junit_sim_user',
    NOW(),
    'junit_sim_user'
);

INSERT INTO `data_test_case` (
    `id`, `problem_id`, `case_sign`, `input_data`, `expected_output`,
    `input_file_path`, `input_file_size`, `output_file_path`, `output_file_size`,
    `is_sample`, `score`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES
('junit_sim_case_s01', 'junit_sim_sum', 'sample-01', '5\n1 2 3 4 5\n', '15\n',
 NULL, 0, NULL, 0, 1, 0, 0, NOW(3), 'junit_sim_user', NOW(3), 'junit_sim_user'),
('junit_sim_case_01', 'junit_sim_sum', 'case-01', '5\n1 2 3 4 5\n', '15\n',
 NULL, 0, NULL, 0, 0, 100, 0, NOW(3), 'junit_sim_user', NOW(3), 'junit_sim_user');

-- ----------------------------
-- Library samples (code_token may be null; filled at runtime / ingest)
-- UNIQUE(user_id, module_type, module_id, problem_id, language) → one row per user
-- ----------------------------

-- BASE: baseline code
INSERT INTO `data_library` (
    `id`, `user_id`, `module_type`, `module_id`, `problem_id`, `submit_id`,
    `submit_time`, `language`, `code`, `code_token`, `code_token_name`, `code_token_texts`,
    `code_length`, `access_count`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_lib_base',
    'junit_sim_u_base',
    'PROBLEM',
    'junit_sim_sum',
    'junit_sim_sum',
    NULL,
    NOW(),
    'cpp',
    '#include <iostream>\nusing namespace std;\nint sumArray(int numbers[], int length) {\n    int total = 0;\n    for (int i = 0; i < length; i++) {\n        total += numbers[i];\n    }\n    return total;\n}\nint main() {\n    int arr[5] = {1, 2, 3, 4, 5};\n    cout << sumArray(arr, 5) << endl;\n    return 0;\n}\n',
    NULL, NULL, NULL,
    0, 0, 0, NOW(), 'junit_sim_user', NOW(), 'junit_sim_user'
);

-- C_01: identical to BASE
INSERT INTO `data_library` (
    `id`, `user_id`, `module_type`, `module_id`, `problem_id`, `submit_id`,
    `submit_time`, `language`, `code`, `code_token`, `code_token_name`, `code_token_texts`,
    `code_length`, `access_count`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_lib_c01',
    'junit_sim_u_c01',
    'PROBLEM',
    'junit_sim_sum',
    'junit_sim_sum',
    NULL,
    NOW(),
    'cpp',
    '#include <iostream>\nusing namespace std;\nint sumArray(int numbers[], int length) {\n    int total = 0;\n    for (int i = 0; i < length; i++) {\n        total += numbers[i];\n    }\n    return total;\n}\nint main() {\n    int arr[5] = {1, 2, 3, 4, 5};\n    cout << sumArray(arr, 5) << endl;\n    return 0;\n}\n',
    NULL, NULL, NULL,
    0, 0, 0, NOW(), 'junit_sim_user', NOW(), 'junit_sim_user'
);

-- C_02: completely different (string greeting; no loop/array/sum)
INSERT INTO `data_library` (
    `id`, `user_id`, `module_type`, `module_id`, `problem_id`, `submit_id`,
    `submit_time`, `language`, `code`, `code_token`, `code_token_name`, `code_token_texts`,
    `code_length`, `access_count`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_lib_c02',
    'junit_sim_u_c02',
    'PROBLEM',
    'junit_sim_sum',
    'junit_sim_sum',
    NULL,
    NOW(),
    'cpp',
    '#include <string>\n\nstd::string buildGreeting(const std::string& name) {\n    if (name.empty()) {\n        return std::string(\"guest\");\n    }\n    return std::string(\"hello-\") + name;\n}\n\nint main() {\n    std::string message = buildGreeting(\"acoj\");\n    return message.size() > 0 ? 0 : 1;\n}\n',
    NULL, NULL, NULL,
    0, 0, 0, NOW(), 'junit_sim_user', NOW(), 'junit_sim_user'
);

-- C_03: renamed variables/functions only
INSERT INTO `data_library` (
    `id`, `user_id`, `module_type`, `module_id`, `problem_id`, `submit_id`,
    `submit_time`, `language`, `code`, `code_token`, `code_token_name`, `code_token_texts`,
    `code_length`, `access_count`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_lib_c03',
    'junit_sim_u_c03',
    'PROBLEM',
    'junit_sim_sum',
    'junit_sim_sum',
    NULL,
    NOW(),
    'cpp',
    '#include <iostream>\nusing namespace std;\nint accumulateList(int vals[], int n) {\n    int result = 0;\n    for (int idx = 0; idx < n; idx++) {\n        result += vals[idx];\n    }\n    return result;\n}\nint main() {\n    int data[5] = {1, 2, 3, 4, 5};\n    cout << accumulateList(data, 5) << endl;\n    return 0;\n}\n',
    NULL, NULL, NULL,
    0, 0, 0, NOW(), 'junit_sim_user', NOW(), 'junit_sim_user'
);

-- C_04: added comments and indentation whitespace
INSERT INTO `data_library` (
    `id`, `user_id`, `module_type`, `module_id`, `problem_id`, `submit_id`,
    `submit_time`, `language`, `code`, `code_token`, `code_token_name`, `code_token_texts`,
    `code_length`, `access_count`, `deleted`, `create_time`, `create_user`, `update_time`, `update_user`
) VALUES (
    'junit_sim_lib_c04',
    'junit_sim_u_c04',
    'PROBLEM',
    'junit_sim_sum',
    'junit_sim_sum',
    NULL,
    NOW(),
    'cpp',
    '#include <iostream>\nusing namespace std;\n\n// Sum all array elements\nint sumArray(int numbers[], int length) {\n    int total = 0;   // accumulator\n\n    for (int i = 0; i < length; i++) {\n            total += numbers[i];\n    }\n\n    return total;\n}\n\nint main() {\n    /* test data */\n    int arr[5] = {1, 2, 3, 4, 5};\n\n    cout << sumArray(arr, 5) << endl;\n    return 0;\n}\n',
    NULL, NULL, NULL,
    0, 0, 0, NOW(), 'junit_sim_user', NOW(), 'junit_sim_user'
);
