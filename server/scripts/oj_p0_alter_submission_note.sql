-- OJ P0: oj_submission 增加用户备注（门户提交记录）
ALTER TABLE `oj_submission`
    ADD COLUMN `note` varchar(255) NULL COMMENT '用户备注' AFTER `judge_message`;
