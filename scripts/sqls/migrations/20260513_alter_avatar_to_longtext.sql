ALTER TABLE `sys_user` MODIFY COLUMN `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像';
ALTER TABLE `client_user` MODIFY COLUMN `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像';
