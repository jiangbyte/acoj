-- 修复 data_solved 缺少业务唯一索引导致的重复解题记录
-- 依赖：SolvedHandleMessage upsert (uk_user_problem_module)

-- 1. 去重：同一 (user_id, problem_id, module_type, module_id) 仅保留一条
--    优先保留已 AC(solved=1)，其次 update_time 最新，再次 id 最大
DELETE FROM data_solved
WHERE id NOT IN (
    SELECT keep_id FROM (
        SELECT SUBSTRING_INDEX(
                       GROUP_CONCAT(id ORDER BY solved DESC, update_time DESC, id DESC),
                       ',', 1
               ) AS keep_id
        FROM data_solved
        GROUP BY user_id, problem_id, module_type, module_id
    ) t
);

-- 2. 添加唯一索引（若已存在会报错，可忽略）
ALTER TABLE data_solved
    ADD UNIQUE INDEX uk_user_problem_module (user_id, problem_id, module_type, module_id);
