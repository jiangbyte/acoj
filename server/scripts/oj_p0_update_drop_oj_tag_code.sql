-- OJ P0 增量：oj_tag 去掉标签编码 code
-- 允许不兼容：后续均为新数据

ALTER TABLE `oj_tag` DROP INDEX `uk_oj_tag_code`;
ALTER TABLE `oj_tag` DROP COLUMN `code`;
