/*
 Navicat Premium Dump SQL

 Source Server         : mysql-dev
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : astro_code

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 07/11/2025 12:24:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_memory`;
CREATE TABLE `ai_chat_memory`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` timestamp NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  CONSTRAINT `chk_message_type` CHECK (`type` in (_utf8mb4'USER',_utf8mb4'ASSISTANT',_utf8mb4'SYSTEM',_utf8mb4'TOOL'))
) ENGINE = InnoDB AUTO_INCREMENT = 1062 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_chat_memory
-- ----------------------------

-- ----------------------------
-- Table structure for data_contest
-- ----------------------------
DROP TABLE IF EXISTS `data_contest`;
CREATE TABLE `data_contest`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '竞赛描述',
  `contest_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '竞赛类型: ACM/OI/OIO',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规则类型',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `max_team_members` int NULL DEFAULT 1 COMMENT '最大团队成员数',
  `is_team_contest` tinyint(1) NULL DEFAULT 0 COMMENT '是否团队赛',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '访问密码',
  `register_start_time` datetime NULL DEFAULT NULL COMMENT '报名开始时间',
  `register_end_time` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  `contest_start_time` datetime NOT NULL COMMENT '竞赛开始时间',
  `contest_end_time` datetime NOT NULL COMMENT '竞赛结束时间',
  `frozen_time` int NULL DEFAULT 0 COMMENT '封榜时间(分钟)',
  `penalty_time` int NULL DEFAULT 20 COMMENT '罚时(分钟)',
  `allowed_languages` json NULL COMMENT '允许语言',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '竞赛表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_contest
-- ----------------------------

-- ----------------------------
-- Table structure for data_contest_auth
-- ----------------------------
DROP TABLE IF EXISTS `data_contest_auth`;
CREATE TABLE `data_contest_auth`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contest_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `is_auth` tinyint(1) NULL DEFAULT 0 COMMENT '是否已经认证',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_user`(`contest_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_contest_id`(`contest_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '竞赛认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_contest_auth
-- ----------------------------

-- ----------------------------
-- Table structure for data_contest_participant
-- ----------------------------
DROP TABLE IF EXISTS `data_contest_participant`;
CREATE TABLE `data_contest_participant`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contest_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `team_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '团队ID',
  `team_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '团队名称',
  `is_team_leader` tinyint(1) NULL DEFAULT 0 COMMENT '是否队长',
  `register_time` datetime NULL DEFAULT NULL COMMENT '报名时间',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contest_user`(`contest_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_contest_id`(`contest_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '竞赛参与表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_contest_participant
-- ----------------------------

-- ----------------------------
-- Table structure for data_contest_problem
-- ----------------------------
DROP TABLE IF EXISTS `data_contest_problem`;
CREATE TABLE `data_contest_problem`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contest_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `problem_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目在竞赛中的编号(A,B,C...)',
  `display_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示ID',
  `score` int NULL DEFAULT 0 COMMENT '题目分数(OI模式)',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `submit_count` int NULL DEFAULT 0 COMMENT '提交次数',
  `accept_count` int NULL DEFAULT 0 COMMENT '通过次数',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_contest_id`(`contest_id` ASC) USING BTREE,
  INDEX `idx_problem_code`(`problem_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '竞赛题目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_contest_problem
-- ----------------------------

-- ----------------------------
-- Table structure for data_judge_case
-- ----------------------------
DROP TABLE IF EXISTS `data_judge_case`;
CREATE TABLE `data_judge_case`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `submit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `case_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `input_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `output_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `expected_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `input_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `input_file_size` bigint NULL DEFAULT 0,
  `output_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `output_file_size` bigint NULL DEFAULT 0,
  `max_time` decimal(10, 2) NULL DEFAULT 0.00,
  `max_memory` decimal(10, 2) NULL DEFAULT 0.00,
  `is_sample` tinyint(1) NULL DEFAULT 0,
  `score` decimal(10, 2) NULL DEFAULT 0.00,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `exit_code` bigint NULL DEFAULT 0,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime(3) NULL DEFAULT NULL,
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime(3) NULL DEFAULT NULL,
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '判题结果用例表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_judge_case
-- ----------------------------

-- ----------------------------
-- Table structure for data_library
-- ----------------------------
DROP TABLE IF EXISTS `data_library`;
CREATE TABLE `data_library`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `submit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `language` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '源代码',
  `code_token` json NULL COMMENT '源代码Token标记',
  `code_token_name` json NULL COMMENT '样本用户Token名称',
  `code_token_texts` json NULL COMMENT '样本用户Token内容',
  `code_length` int NULL DEFAULT 0 COMMENT '源代码长度',
  `access_count` int NULL DEFAULT 0 COMMENT '访问次数',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_set_id`(`module_type` ASC, `module_id` ASC) USING BTREE,
  INDEX `idx_problem_id`(`problem_id` ASC) USING BTREE,
  INDEX `idx_language`(`language` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提交样本库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_library
-- ----------------------------

-- ----------------------------
-- Table structure for data_problem
-- ----------------------------
DROP TABLE IF EXISTS `data_problem`;
CREATE TABLE `data_problem`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `display_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示ID',
  `category_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '分类',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
  `max_time` int NULL DEFAULT 0 COMMENT '时间限制',
  `max_memory` int NULL DEFAULT 0 COMMENT '内存限制',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `test_case` json NULL COMMENT '用例',
  `allowed_languages` json NULL COMMENT '开放语言',
  `difficulty` int NULL DEFAULT 1 COMMENT '难度',
  `threshold` decimal(10, 2) UNSIGNED NULL DEFAULT 0.50 COMMENT '阈值',
  `use_template` tinyint(1) NULL DEFAULT 0 COMMENT '使用模板',
  `code_template` json NULL COMMENT '模板代码',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `use_ai` tinyint(1) NULL DEFAULT 0 COMMENT '是否使用AI',
  `solved` bigint NULL DEFAULT 0 COMMENT '解决',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_is_public`(`is_public` ASC) USING BTREE,
  INDEX `idx_is_visible`(`is_visible` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_problem
-- ----------------------------

-- ----------------------------
-- Table structure for data_problem_tag
-- ----------------------------
DROP TABLE IF EXISTS `data_problem_tag`;
CREATE TABLE `data_problem_tag`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `tag_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目标签关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_problem_tag
-- ----------------------------

-- ----------------------------
-- Table structure for data_progress
-- ----------------------------
DROP TABLE IF EXISTS `data_progress`;
CREATE TABLE `data_progress`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `extra_json` json NULL COMMENT '额外信息',
  `completed` tinyint(1) NULL DEFAULT 0 COMMENT '是否完成',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题集进度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_progress
-- ----------------------------

-- ----------------------------
-- Table structure for data_set
-- ----------------------------
DROP TABLE IF EXISTS `data_set`;
CREATE TABLE `data_set`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `set_type` int NULL DEFAULT 1 COMMENT '题集类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
  `category_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '分类',
  `difficulty` int NULL DEFAULT 1 COMMENT '难度',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `ex_json` json NULL COMMENT '额外的信息',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `use_ai` tinyint(1) NULL DEFAULT 0 COMMENT '是否使用AI',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_set_type`(`set_type` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题集' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_set
-- ----------------------------

-- ----------------------------
-- Table structure for data_set_problem
-- ----------------------------
DROP TABLE IF EXISTS `data_set_problem`;
CREATE TABLE `data_set_problem`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `set_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题集ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题集题目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_set_problem
-- ----------------------------

-- ----------------------------
-- Table structure for data_solved
-- ----------------------------
DROP TABLE IF EXISTS `data_solved`;
CREATE TABLE `data_solved`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `submit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
  `solved` tinyint(1) NULL DEFAULT 0 COMMENT '是否解决',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `first_solved_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `first_submit_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户解决表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_solved
-- ----------------------------

-- ----------------------------
-- Table structure for data_submit
-- ----------------------------
DROP TABLE IF EXISTS `data_submit`;
CREATE TABLE `data_submit`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `language` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '源代码',
  `code_length` int NULL DEFAULT 0 COMMENT '源代码长度',
  `submit_type` tinyint(1) NULL DEFAULT 0 COMMENT '执行类型',
  `max_time` int NULL DEFAULT 0 COMMENT '最大耗时',
  `max_memory` int NULL DEFAULT 0 COMMENT '最大内存使用',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行结果消息',
  `test_case` json NULL COMMENT '测试用例结果',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行状态',
  `is_finish` tinyint(1) NULL DEFAULT 0 COMMENT '流程流转是否完成',
  `similarity` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '相似度',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似检测任务ID',
  `report_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '报告ID',
  `similarity_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似分级',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  `judge_task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似检测任务ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `problem_id`(`problem_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_problem_id`(`problem_id` ASC) USING BTREE,
  INDEX `idx_language`(`language` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提交表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_submit
-- ----------------------------

-- ----------------------------
-- Table structure for data_test_case
-- ----------------------------
DROP TABLE IF EXISTS `data_test_case`;
CREATE TABLE `data_test_case`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `case_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `input_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `expected_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `input_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `input_file_size` bigint NULL DEFAULT 0,
  `output_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `output_file_size` bigint NULL DEFAULT 0,
  `is_sample` tinyint(1) NULL DEFAULT 0,
  `score` decimal(10, 2) NULL DEFAULT 0.00,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime(3) NULL DEFAULT NULL,
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime(3) NULL DEFAULT NULL,
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_problem_id`(`problem_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目测试用例表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_test_case
-- ----------------------------

-- ----------------------------
-- Table structure for sys_article
-- ----------------------------
DROP TABLE IF EXISTS `sys_article`;
CREATE TABLE `sys_article`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '摘要',
  `sort` tinyint NULL DEFAULT 99 COMMENT '排序',
  `to_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '父级',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '类型',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '分类',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_article
-- ----------------------------

-- ----------------------------
-- Table structure for sys_banner
-- ----------------------------
DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '横幅',
  `button_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '按钮文字',
  `is_visible_button` tinyint(1) NULL DEFAULT 1 COMMENT '按钮是否可见',
  `jump_module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转模块',
  `jump_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转类别',
  `jump_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转目标',
  `target_blank` tinyint(1) NULL DEFAULT 0 COMMENT '新窗口打开',
  `sort` tinyint NULL DEFAULT 99 COMMENT '排序',
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子标题',
  `is_visible_subtitle` tinyint(1) NULL DEFAULT 1 COMMENT '子标题是否可见',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '横幅表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_banner
-- ----------------------------
INSERT INTO `sys_banner` VALUES ('1983071920168431618', '', 'http://120.26.180.149:9000/astro-code-oj/e9408faf649f491cbe800d79d95046ea.png', '查看一下', 1, 'PERSONAL', 'ID', '1', 0, 1, '', 0, 1, 0, '2025-10-28 15:22:44', '1', '2025-10-28 23:24:58', '1');
INSERT INTO `sys_banner` VALUES ('1983086647729238018', '', 'http://120.26.180.149:9000/astro-code-oj/1b8aac18fb264535923c712d37309de3.png', '查看题库', 1, 'PAGE_PROBLEM', '', '', 0, 2, '', 0, 1, 0, '2025-10-28 16:21:15', '1', '2025-10-28 23:26:16', '1');
INSERT INTO `sys_banner` VALUES ('1983087654642708481', '', 'http://120.26.180.149:9000/astro-code-oj/4d9e85f943e7481b8c187d119ef892ef.png', '尝试一下', 1, 'PROBLEM', 'ID', '1983075943919448066', 0, 3, '', 0, 1, 0, '2025-10-28 16:25:16', '1', '2025-10-28 23:26:12', '1');
INSERT INTO `sys_banner` VALUES ('1983087728068194305', '', 'http://120.26.180.149:9000/astro-code-oj/403113f312a147e5bbb732ae032ccfb1.png', '尝试一下	', 1, 'PROBLEM', 'ID', '1983075943919448066', 0, 4, '', 0, 1, 0, '2025-10-28 16:25:33', '1', '2025-10-28 23:26:07', '1');

-- ----------------------------
-- Table structure for sys_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_category
-- ----------------------------
INSERT INTO `sys_category` VALUES ('0', '未分类', 0, '2025-10-21 12:25:12', '0', '2025-10-22 12:25:12', '0');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '值',
  `component_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件类型',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  `config_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置分类',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES ('1982136140003225601', 'Admin端登录背景', 'APP_ADMIN_LOGIN_BACKGROUND', 'http://120.26.180.149:9000/astro-code-oj/021143219b6643fb8289ffa417ad4a8d.png', '6', '登录页面背景图片', 0, '2025-10-21 12:25:12', '1', '2025-10-30 09:03:06', '1', '5', 8);
INSERT INTO `sys_config` VALUES ('1982136140003225602', 'Web端登录背景', 'APP_PC_LOGIN_BACKGROUND', 'http://120.26.180.149:9000/astro-code-oj/c2c720efeb1b45ee8ff63255367e29f5.png', '6', 'Web应用端登录背景', 0, '2025-10-21 12:25:12', '1', '2025-10-28 17:11:32', '1', '5', 5);
INSERT INTO `sys_config` VALUES ('1982136140003225603', 'Web端注册背景', 'APP_PC_REGISTER_BACKGROUND', 'http://120.26.180.149:9000/astro-code-oj/3c57c5d910b34d0aa0847b49e9d31a60.png', '6', 'Web应用端注册背景', 0, '2025-10-21 12:25:12', '1', '2025-10-28 17:11:36', '1', '5', 5);
INSERT INTO `sys_config` VALUES ('1982136140003225604', 'Web端显示名称', 'APP_PC_SHOW_APP_NAME', 'false', '1', 'false 显示 Logo，true 显示 名称', 0, '2025-10-21 12:25:12', '1', '2025-10-28 16:49:58', '1', '5', 7);
INSERT INTO `sys_config` VALUES ('1982136140003225605', '用户默认头像', 'APP_DEFAULT_AVATAR', 'http://120.26.180.149:9000/astro-code-oj/0e1cbf5a2a614929a6e103e57b2c1dbf.png', '6', '', 0, '2025-10-21 12:25:12', '1', '2025-10-30 09:02:14', '1', '5', 3);
INSERT INTO `sys_config` VALUES ('1982136140003225606', '用户默认背景', 'APP_DEFAULT_USER_BACKGROUND', 'http://120.26.180.149:9000/astro-code-oj/21977c80d7bc4e549e3f67e26e330946.png', '6', '', 0, '2025-10-21 12:25:12', '1', '2025-10-28 20:32:06', '1', '5', 4);
INSERT INTO `sys_config` VALUES ('1982136140003225607', '应用Logo', 'APP_LOGO', 'http://120.26.180.149:9000/astro-code-oj/a1c130a750c94856b028629382cf5b21.png', '6', '建议 1:1 图片上传', 0, '2025-10-21 12:25:12', '1', '2025-10-28 16:57:51', '1', '5', 1);
INSERT INTO `sys_config` VALUES ('1982136140003225608', '应用名称', 'APP_NAME', 'Astro Code', '2', '应用名称', 0, '2025-10-21 12:25:12', '1', '2025-10-28 16:49:22', '1', '5', 2);
INSERT INTO `sys_config` VALUES ('1982136140003225609', 'Admin端显示名称', 'APP_ADMIN_SHOW_APP_NAME', 'false', '1', '', 0, '2025-10-21 12:25:12', '1', '2025-10-28 16:50:16', '1', '5', 9);

-- ----------------------------
-- Table structure for sys_conversation
-- ----------------------------
DROP TABLE IF EXISTS `sys_conversation`;
CREATE TABLE `sys_conversation`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对话ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `set_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
  `is_set` tinyint(1) NULL DEFAULT 0 COMMENT '题集对话',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `message_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息类型',
  `message_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息角色',
  `message_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `user_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代码',
  `language` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '代码语言',
  `prompt_tokens` int NULL DEFAULT 0 COMMENT '提示Tokens',
  `completion_tokens` int NULL DEFAULT 0 COMMENT '完成Tokens',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总Tokens',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `streaming_duration` int NULL DEFAULT NULL COMMENT '流式传输总耗时',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `user_platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户平台',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '大模型对话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_conversation
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `dict_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典类型',
  `type_label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型名称',
  `dict_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典值',
  `dict_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典标签',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_code`(`dict_type` ASC, `dict_value` ASC) USING BTREE COMMENT '类型和编码唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES ('1949386814273540097', 'PROBLEM_DIFFICULTY', '难度', '1', '简单', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949387747678171138', 'PROBLEM_DIFFICULTY', '难度', '2', '中等', 2, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949387822563274754', 'PROBLEM_DIFFICULTY', '难度', '3', '困难', 3, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949389559521005569', 'ALLOW_LANGUAGE', '开放语言', 'java', 'Java', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949389612713168898', 'ALLOW_LANGUAGE', '开放语言', 'cpp', 'Cpp', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949389698243416065', 'ALLOW_LANGUAGE', '开放语言', 'go', 'Go', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-24 11:56:07', '1');
INSERT INTO `sys_dict` VALUES ('1949389728094277634', 'ALLOW_LANGUAGE', '开放语言', 'c', 'C', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949389906322837505', 'ALLOW_LANGUAGE', '开放语言', 'python', 'Python', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949640044622639105', 'YES_NO', '是否', 'true', '是', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949640252018388993', 'YES_NO', '是否', 'false', '否', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949671004755980290', 'SUBMIT_TYPE', '执行类型', 'false', '运行', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949671066571632641', 'SUBMIT_TYPE', '执行类型', 'true', '提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949688687626977282', 'PROBLEM_SET_TYPE', '题集类型', '1', '常规题集', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1949688825984483330', 'PROBLEM_SET_TYPE', '题集类型', '2', '限时题集', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1950883784481759233', 'JUDGE_STATUS', '判题状态', 'ACCEPTED', '答案正确', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195542354329602', 'JUDGE_STATUS', '判题状态', 'PENDING', '等待判题', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195583638863874', 'JUDGE_STATUS', '判题状态', 'JUDGING', '判题中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195620687151105', 'JUDGE_STATUS', '判题状态', 'COMPILING', '编译中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195658800791554', 'JUDGE_STATUS', '判题状态', 'RUNNING', '运行中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195722818453506', 'JUDGE_STATUS', '判题状态', 'WRONG_ANSWER', '答案错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195774957846529', 'JUDGE_STATUS', '判题状态', 'TIME_LIMIT_EXCEEDED', '时间超出限制', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195824698097666', 'JUDGE_STATUS', '判题状态', 'MEMORY_LIMIT_EXCEEDED', '内存超出限制', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195861415034882', 'JUDGE_STATUS', '判题状态', 'RUNTIME_ERROR', '运行时错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195903551012865', 'JUDGE_STATUS', '判题状态', 'COMPILATION_ERROR', '编译错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951195947666702337', 'JUDGE_STATUS', '判题状态', 'PRESENTATION_ERROR', '格式错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951196106874093570', 'JUDGE_STATUS', '判题状态', 'PARTIAL_ACCEPTED', '部分正确', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951196145809817601', 'JUDGE_STATUS', '判题状态', 'SYSTEM_ERROR', '系统错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951196241905516546', 'JUDGE_STATUS', '判题状态', 'REJUDGING', '重新判题中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1951196276516913154', 'JUDGE_STATUS', '判题状态', 'UNKNOWN_ERROR', '未知错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1954009065375625218', 'JUDGE_STATUS', '判题状态', 'COMPILED_OK', '编译成功', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1963850874083848194', 'CLONE_LEVEL', '相似等级', 'HIGHLY_SUSPECTED', '高度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1963851262937772034', 'CLONE_LEVEL', '相似等级', 'MEDIUM_SUSPECTED', '中度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1963851317014933505', 'CLONE_LEVEL', '相似等级', 'LOW_SUSPECTED', '轻度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1963851373667397634', 'CLONE_LEVEL', '相似等级', 'NOT_REACHED', '未达阈值', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1963876843167334401', 'CLONE_LEVEL', '相似等级', 'NOT_DETECTED', '未检测', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1970841457780269058', 'MENU_TYPE', '菜单类型', '0', '目录', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1970841583638749185', 'MENU_TYPE', '菜单类型', '1', '菜单', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1971400843359612930', 'REPORT_TYPE', '报告类型', '1', '题目单提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1971401085530337282', 'REPORT_TYPE', '报告类型', '2', '题集单提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1971403102587875330', 'CHECK_MODE', '相似检测模式', '1', '自动', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1971403149677326337', 'CHECK_MODE', '相似检测模式', '2', '手动', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1971403185362464770', 'CHECK_MODE', '相似检测模式', '3', '定时', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980055469866659841', 'COMPONENT_TYPE', '配置组件类型', '1', '布尔（True/False）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980056161541427202', 'CONFIG_TYPE', '配置类型', '1', '背景图片', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083239932010497', 'CONFIG_TYPE', '配置类型', '2', '系统图标', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083284530044929', 'CONFIG_TYPE', '配置类型', '3', '相似检测', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083320596865026', 'CONFIG_TYPE', '配置类型', '4', '默认参数', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083563702919169', 'COMPONENT_TYPE', '配置组件类型', '2', '文本输入（单行）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083613787103234', 'COMPONENT_TYPE', '配置组件类型', '3', '文本输入（多行）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083741243613186', 'COMPONENT_TYPE', '配置组件类型', '4', '数字输入（整数）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980083995699453953', 'COMPONENT_TYPE', '配置组件类型', '5', '数字输入（小数）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980090363294654466', 'CONFIG_TYPE', '配置类型', '5', '系统配置', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980091643719249922', 'COMPONENT_TYPE', '配置组件类型', '6', '图片上传', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980198864968962049', 'MESSAGE_TYPE', '对话类型', 'DailyConversation', '默认对话', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980199081676066817', 'MESSAGE_TYPE', '对话类型', 'ProblemSolvingIdeas', '解题思路', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980199161564975106', 'MESSAGE_TYPE', '对话类型', 'OptimizeCode', '优化代码', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980199239746801665', 'MESSAGE_TYPE', '对话类型', 'AnalyzeBoundary', '分析边界', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980199313264562177', 'MESSAGE_TYPE', '对话类型', 'AnalyzeComplexity', '分析复杂度', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980283142327119873', 'MESSAGE_ROLE', '对话角色', 'ASSISTANT', '助手', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1980283213173108738', 'MESSAGE_ROLE', '对话角色', 'USER', '用户', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1981232874954690561', 'JUMP_MODULE', '跳转模块', 'HREF', '外部链接', 0, 0, '2025-10-23 13:35:02', '1', '2025-10-23 13:35:02', '1');
INSERT INTO `sys_dict` VALUES ('1981233060934324226', 'JUMP_MODULE', '跳转模块', 'NOTICE', '公告', 0, 0, '2025-10-23 13:35:46', '1', '2025-10-23 13:35:46', '1');
INSERT INTO `sys_dict` VALUES ('1981233109919600641', 'JUMP_MODULE', '跳转模块', 'PERSONAL', '个人主页', 0, 0, '2025-10-23 13:35:58', '1', '2025-10-23 13:35:58', '1');
INSERT INTO `sys_dict` VALUES ('1981233183928094722', 'JUMP_MODULE', '跳转模块', 'PROBLEM', '题目详情', 0, 0, '2025-10-23 13:36:15', '1', '2025-10-23 13:36:15', '1');
INSERT INTO `sys_dict` VALUES ('1981233236465946625', 'JUMP_MODULE', '跳转模块', 'PAGE_PROBLEM', '题目页面', 0, 0, '2025-10-23 13:36:28', '1', '2025-10-23 13:36:28', '1');
INSERT INTO `sys_dict` VALUES ('1981233285115678722', 'JUMP_MODULE', '跳转模块', 'SET', '题集详情', 0, 0, '2025-10-23 13:36:39', '1', '2025-10-23 13:36:39', '1');
INSERT INTO `sys_dict` VALUES ('1981233328170209281', 'JUMP_MODULE', '跳转模块', 'PAGE_SET', '题集页面', 0, 0, '2025-10-23 13:36:50', '1', '2025-10-23 13:36:50', '1');
INSERT INTO `sys_dict` VALUES ('1981233379021950978', 'JUMP_MODULE', '跳转模块', 'NOTDO', '不做处理', 0, 0, '2025-10-23 13:37:02', '1', '2025-10-23 13:37:02', '1');
INSERT INTO `sys_dict` VALUES ('1981233496931741698', 'JUMP_TYPE', '跳转类别', 'URL', 'URL', 0, 0, '2025-10-23 13:37:30', '1', '2025-10-23 13:37:30', '1');
INSERT INTO `sys_dict` VALUES ('1981233544852148225', 'JUMP_TYPE', '跳转类别', 'ID', 'ID', 0, 0, '2025-10-23 13:37:41', '1', '2025-10-23 13:37:41', '1');
INSERT INTO `sys_dict` VALUES ('1981551937107005441', 'DATA_SCOPE', '数据范围', 'SELF', '仅自己', 0, 0, '2025-10-24 10:42:52', '1', '2025-10-24 10:42:52', '1');
INSERT INTO `sys_dict` VALUES ('1981552252866793473', 'DATA_SCOPE', '数据范围', 'SELF_GROUP', '本组及下级', 0, 0, '2025-10-24 10:44:07', '1', '2025-10-24 10:44:07', '1');
INSERT INTO `sys_dict` VALUES ('1981552438887886849', 'DATA_SCOPE', '数据范围', 'SELF_GROUP_ONLY', '仅本组', 0, 0, '2025-10-24 10:44:52', '1', '2025-10-24 10:44:52', '1');
INSERT INTO `sys_dict` VALUES ('1981552501403987969', 'DATA_SCOPE', '数据范围', 'SPECIFIED_GROUP', '指定组', 0, 0, '2025-10-24 10:45:06', '1', '2025-10-24 10:45:06', '1');
INSERT INTO `sys_dict` VALUES ('1981552564389851137', 'DATA_SCOPE', '数据范围', 'ALL', '所有数据', 0, 0, '2025-10-24 10:45:21', '1', '2025-10-24 10:45:21', '1');
INSERT INTO `sys_dict` VALUES ('1981611294175309825', 'TIME_STATUS', '时间状态', '1', '未开始', 0, 0, '2025-10-24 14:38:44', '1', '2025-10-24 14:38:44', '1');
INSERT INTO `sys_dict` VALUES ('1981612262153564161', 'TIME_STATUS', '时间状态', '2', '进行中', 0, 0, '2025-10-24 14:42:35', '1', '2025-10-24 14:42:35', '1');
INSERT INTO `sys_dict` VALUES ('1981612310983651329', 'TIME_STATUS', '时间状态', '3', '已结束', 0, 0, '2025-10-24 14:42:46', '1', '2025-10-24 14:42:46', '1');
INSERT INTO `sys_dict` VALUES ('1982137082970841089', 'SYS_GENDER', '性别', '0', '未知', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1982137082970841090', 'SYS_GENDER', '性别', '1', '男', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1982137082970841091', 'SYS_GENDER', '性别', '2', '女', 2, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict` VALUES ('1983422246512275457', 'JUDGE_STATUS', '判题状态', 'RUN_SUCCESS', '运行成功', 0, 0, '2025-10-29 14:34:48', '1', '2025-10-29 14:34:48', '1');
INSERT INTO `sys_dict` VALUES ('1986443880561328129', 'CONTEST_TYPE', '竞赛类型', 'ACM', 'ACM/ICPC赛制', 0, 0, '2025-11-06 22:41:42', '1', '2025-11-06 22:41:42', '1');
INSERT INTO `sys_dict` VALUES ('1986443938925068290', 'CONTEST_TYPE', '竞赛类型', 'OI', 'OI/IOI赛制', 0, 0, '2025-11-06 22:41:56', '1', '2025-11-06 22:41:56', '1');
INSERT INTO `sys_dict` VALUES ('1986443978200530945', 'CONTEST_TYPE', '竞赛类型', 'OIO', 'OIO赛制', 0, 0, '2025-11-06 22:42:05', '1', '2025-11-06 22:42:05', '1');

-- ----------------------------
-- Table structure for sys_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_group`;
CREATE TABLE `sys_group`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户组',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父级用户组',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `sort` tinyint NULL DEFAULT 99 COMMENT '排序',
  `admin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `group_type` tinyint(1) NULL DEFAULT 0 COMMENT '系统组',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_group_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_group
-- ----------------------------
INSERT INTO `sys_group` VALUES ('0', '0', '平台', 'PLATFORM', '平台', 0, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-25 21:29:39', '1');
INSERT INTO `sys_group` VALUES ('1', '0', '默认用户', 'DEFAULT_GROUP', '默认用户组', 1, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-25 21:29:39', '1');
INSERT INTO `sys_group` VALUES ('2', '0', '系统管理组', 'SUPER_GROUP', '超级管理员组', 2, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_group` VALUES ('3', '0', '管理员组', 'ADMIN_GROUP', '管理员组', 3, '1', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '参数',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP',
  `operation_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作分类',
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作状态',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作消息',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统活动/日志记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单ID',
  `pid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '父菜单ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单名称（英文标识）',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单标题',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `keep_alive` tinyint(1) NULL DEFAULT 0 COMMENT '是否缓存',
  `visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `pined` tinyint(1) NULL DEFAULT 0 COMMENT '是否固定',
  `menu_type` int NULL DEFAULT 0 COMMENT '菜单类型：0-目录，1-菜单',
  `ex_json` json NULL COMMENT '额外信息',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  `parameters` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单头部参数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE,
  INDEX `idx_menu_type`(`menu_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('1', '0', 'home', '/index', '/workbench/index.vue', '首页', 'icon-park-outline:home', 0, 1, 1, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('100', '0', 'content_index', '/content', NULL, '内容管理', 'icon-park-outline:setting-config', 0, 1, 3, 0, 0, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('10019', '0', 'system_index', '/system', NULL, '系统管理', 'icon-park-outline:setting-config', 0, 1, 4, 0, 0, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('101', '100', 'banner', '/content/banner', '/modular/sys/banner/index.vue', '横幅管理', 'icon-park-outline:ad-product', 0, 1, 1, 0, 1, '[\"/sys/banner/add\", \"/sys/banner/delete\", \"/sys/banner/detail\", \"/sys/banner/page\", \"/sys/banner/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('102', '100', 'notice', '/content/notice', '/modular/sys/notice/index.vue', '公告管理', 'icon-park-outline:volume-notice', 0, 1, 2, 0, 1, '[\"/sys/notice/add\", \"/sys/notice/delete\", \"/sys/notice/detail\", \"/sys/notice/page\", \"/sys/notice/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('103', '10019', 'role', '/system/role', '/modular/sys/role/index.vue', '角色管理', 'icon-park-outline:people-safe', 0, 1, 2, 0, 1, '[\"/sys/role/add\", \"/sys/role/delete\", \"/sys/role/detail\", \"/sys/role/page\", \"/sys/role/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('104', '10019', 'user', '/system/user', '/modular/sys/user/index.vue', '用户管理', 'icon-park-outline:user', 0, 1, 1, 0, 1, '[\"/sys/user/add\", \"/sys/user/delete\", \"/sys/user/detail\", \"/sys/user/page\", \"/sys/user/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('105', '10019', 'group', '/system/group', '/modular/sys/group/index.vue', '用户组管理', 'icon-park-outline:peoples', 0, 1, 3, 0, 1, '[\"/sys/group/add\", \"/sys/group/delete\", \"/sys/group/detail\", \"/sys/group/page\", \"/sys/group/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('10574', '10019', 'config', '/system/config', '/modular/sys/config/index.vue', '环境变量', 'icon-park-outline:enter-the-keyboard', 0, 1, 4, 0, 1, '[\"/sys/config/add\", \"/sys/config/delete\", \"/sys/config/detail\", \"/sys/config/page\", \"/sys/config/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('106', '10019', 'dict', '/system/dict', '/modular/sys/dict/index.vue', '字典管理', 'icon-park-outline:book', 0, 1, 5, 0, 1, '[\"/sys/dict/add\", \"/sys/dict/delete\", \"/sys/dict/detail\", \"/sys/dict/page\", \"/sys/dict/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('107', '10019', 'menu', '/system/menu', '/modular/sys/menu/index.vue', '菜单管理', 'icon-park-outline:application-menu', 0, 1, 6, 0, 1, '[\"/sys/menu/add\", \"/sys/menu/delete\", \"/sys/menu/detail\", \"/sys/menu/page\", \"/sys/menu/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('108', '10019', 'log', '/system/log', '/modular/sys/log/index.vue', '系统日志', 'icon-park-outline:log', 0, 1, 7, 0, 1, '[\"/sys/log/add\", \"/sys/log/delete\", \"/sys/log/detail\", \"/sys/log/page\", \"/sys/log/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:13:49', '1', NULL);
INSERT INTO `sys_menu` VALUES ('1979358311883591681', '0', 'outweb_index', '/outweb', NULL, '外部管理', 'icon-park-outline:application-two', 0, 1, 14, 0, 0, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('1979359562444050434', '1979358311883591681', 'dozzle120', '/outweb/dozzle120', '/outweb/index.vue', '120服务器日志', 'icon-park-outline:log', 0, 1, 3, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:50', '1', '{\n    \"href_inner\": \"http://120.26.180.149:9001/\"\n}');
INSERT INTO `sys_menu` VALUES ('1979779968216379394', '1979358311883591681', 'dozzle47', '/outweb/dozzle47', '/outweb/index.vue', '47服务器日志', 'icon-park-outline:log', 0, 1, 2, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:45', '1', '{\r\n    \"href_inner\": \"http://47.99.236.69:9001/\"\r\n}');
INSERT INTO `sys_menu` VALUES ('1979781411367333889', '1979358311883591681', 'portainer', '/outweb/portainer', '/outweb/index.vue', 'Portainer', 'icon-park-outline:application', 0, 1, 4, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:55', '1', '{\r\n    \"href_inner\": \"http://101.201.174.43:9020/\"\r\n}');
INSERT INTO `sys_menu` VALUES ('1979784519254323202', '1979358311883591681', 'nacos', '/outweb/nacos', '/outweb/index.vue', 'Nacos', 'icon-park-outline:link-cloud', 0, 1, 5, 0, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:01:43', '1', '{\r\n    \"href_outter\": \"http://120.26.180.149:8848/nacos\"\r\n}');
INSERT INTO `sys_menu` VALUES ('1979788036882870274', '1979358311883591681', 'minio', '/outweb/minio', '/outweb/index.vue', 'Minio', 'icon-park-outline:file-cabinet', 0, 1, 6, 0, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:01:50', '1', '{\r\n    \"href_outter\": \"http://120.26.180.149:9090/\"\r\n}');
INSERT INTO `sys_menu` VALUES ('1979792211910332418', '1979358311883591681', 'dozzle101', '/outweb/dozzle101', '/outweb/index.vue', '101服务器日志', 'icon-park-outline:log', 0, 1, 1, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:40', '1', '{\r\n    \"href_inner\": \"http://101.201.174.43:9995/\"\r\n}');
INSERT INTO `sys_menu` VALUES ('1980094089497628673', '10019', 'config_views', '/system/config/views', '/modular/sys/config/views.vue', '系统配置', 'icon-park-outline:enter-the-keyboard', 0, 1, 8, 0, 1, '[\"/sys/config/add\", \"/sys/config/delete\", \"/sys/config/detail\", \"/sys/config/edit\", \"/sys/config/page\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:14:10', '1', '');
INSERT INTO `sys_menu` VALUES ('1980165871264997378', '10019', 'conversation', '/system/conversation', '/modular/sys/conversation/index.vue', '模型对话', 'icon-park-outline:ad-product', 0, 1, 9, 0, 1, '[\"/sys/conversation/edit\", \"/sys/conversation/page\", \"/sys/conversation/detail\", \"/sys/conversation/delete\", \"/sys/conversation/add\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-03 17:04:19', '1', '');
INSERT INTO `sys_menu` VALUES ('1986444507181957121', '0', 'contest', '/contest/list', '/modular/data/contest/index.vue', '竞赛管理', 'icon-park-outline:list', 0, 1, 7, 0, 1, '[\"/data/contest/add\", \"/data/contest/delete\", \"/data/contest/detail\", \"/data/contest/page\", \"/data/contest/edit\", \"/data/contest/participant/add\", \"/data/contest/participant/delete\", \"/data/contest/participant/detail\", \"/data/contest/participant/page\", \"/data/contest/participant/edit\", \"/data/contest/problem/add\", \"/data/contest/problem/delete\", \"/data/contest/problem/detail\", \"/data/contest/problem/edit\", \"/data/contest/problem/page\"]', 0, '2025-11-06 22:44:12', '1', '2025-11-06 22:45:23', '1', '');
INSERT INTO `sys_menu` VALUES ('2', '0', 'dashboard', '/dashboard', '/dashboard/index.vue', '仪表盘', 'icon-park-outline:dashboard', 0, 0, 2, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('3', '0', 'category', '/category', '/modular/sys/category/index.vue', '分类管理', 'icon-park-outline:category-management', 0, 1, 5, 0, 1, '[\"/sys/category/delete\", \"/sys/category/add\", \"/sys/category/detail\", \"/sys/category/page\", \"/sys/category/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('301', '0', 'problem', '/problem/list', '/modular/data/problem/index.vue', '题目管理', 'icon-park-outline:list', 0, 1, 7, 0, 1, '[\"/data/problem/add\", \"/data/problem/delete\", \"/data/problem/detail\", \"/data/problem/page\", \"/data/problem/edit\", \"/data/test/case/page\", \"/data/test/case/add\", \"/data/test/case/delete\", \"/data/test/case/edit\", \"/data/test/case/detail\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-06 22:43:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('302', '0', 'submit', '/submit', '/modular/data/submit/index.vue', '用户提交', 'icon-park-outline:upload', 0, 1, 9, 0, 1, '[\"/data/submit/add\", \"/data/submit/delete\", \"/data/submit/detail\", \"/data/submit/page\", \"/data/submit/edit\", \"/data/judge/case/detail\", \"/data/judge/case/edit\", \"/data/judge/case/delete\", \"/data/judge/case/add\", \"/data/judge/case/page\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-04 16:30:15', '1', NULL);
INSERT INTO `sys_menu` VALUES ('303', '0', 'solved', '/solved', '/modular/data/solved/index.vue', '解决记录', 'icon-park-outline:checklist', 0, 1, 10, 0, 1, '[\"/data/solved/delete\", \"/data/solved/add\", \"/data/solved/detail\", \"/data/solved/page\", \"/data/solved/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('306', '0', 'library', '/data/library', '/modular/data/library/index.vue', '有效代码', 'icon-park-outline:data-file', 0, 1, 11, 0, 1, '[\"/data/library/add\", \"/data/library/delete\", \"/data/library/detail\", \"/data/library/page\", \"/data/library/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-04 09:30:09', '1', NULL);
INSERT INTO `sys_menu` VALUES ('4', '0', 'tag', '/tag', '/modular/sys/tag/index.vue', '标签管理', 'icon-park-outline:tag', 0, 1, 6, 0, 1, '[\"/sys/tag/add\", \"/sys/tag/delete\", \"/sys/tag/detail\", \"/sys/tag/page\", \"/sys/tag/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('401', '0', 'set', '/set/list', '/modular/data/set/index.vue', '题集训练', 'icon-park-outline:list', 0, 1, 8, 0, 1, '[\"/data/set/add\", \"/data/set/delete\", \"/data/set/detail\", \"/data/set/page\", \"/data/set/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-06 22:42:58', '1', NULL);
INSERT INTO `sys_menu` VALUES ('407', '0', 'similarity', '/task/similarity', '/modular/data/similarity/index.vue', '克隆片段', 'icon-park-outline:accept-email', 0, 0, 12, 0, 1, '[\"/task/similarity/add\", \"/task/similarity/detail\", \"/task/similarity/delete\", \"/task/similarity/page\", \"/task/similarity/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-11-04 09:30:21', '1', NULL);
INSERT INTO `sys_menu` VALUES ('500', '0', 'reports', '/reports', '/modular/data/reports/index.vue', '检测统计', 'icon-park-outline:database-download', 0, 1, 13, 0, 1, '[\"/task/reports/add\", \"/task/reports/delete\", \"/task/reports/detail\", \"/task/reports/page\", \"/task/reports/edit\"]', 0, '2025-10-21 12:25:12', '1', '2025-10-31 11:54:11', '1', NULL);
INSERT INTO `sys_menu` VALUES ('600', '0', 'mine', '/mine', '/modular/mine/profile.vue', '个人中心', 'icon-park-outline:user', 0, 1, 50, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('700', '0', 'about', '/about', '/about.vue', '关于', 'icon-park-outline:info', 0, 1, 99, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu` VALUES ('701', '0', 'report', '/visualization/submit/report/:reportId/task/:taskId', '/modular/visualization/submit/report.vue', '数据统计', 'icon-park-outline:info', 0, 0, 99, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-31 11:48:23', '1', NULL);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
  `sort` tinyint NULL DEFAULT 99 COMMENT '排序',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES ('1983074367356579842', '平台判题机制说明公告', 'http://120.26.180.149:9000/astro-code-oj/9b745c43c3bf4580b6f70ff05bf993ea.png', '', 2, '亲爱的用户：\n\n为了帮助大家更好地理解我们平台的判题机制，提高编程练习的效率，现将平台的判题流程和注意事项说明如下：\n\n## 判题流程概述\n\n我们的判题系统采用标准化流程对您的提交进行评测，具体流程如下：\n\n```mermaid\ngraph TD\n    A[用户提交代码] --> B[等待判题]\n    B --> C[编译代码]\n    C --> D{编译是否成功?}\n    D -->|否| E[编译错误]\n    D -->|是| F[运行程序]\n    F --> G[比对输出]\n    G --> H{输出是否正确?}\n    H -->|完全正确| I[答案正确]\n    H -->|部分正确| J[部分正确]\n    H -->|错误| K[答案错误]\n    F --> L{是否超时?}\n    L -->|是| M[时间超出限制]\n    F --> N{内存是否超限?}\n    N -->|是| O[内存超出限制]\n    F --> P{是否运行时错误?}\n    P -->|是| Q[运行时错误]\n```\n\n## 各编程语言配置说明\n\n| 语言 | 源文件要求 | 编译命令 | 运行命令 |\n|------|------------|----------|----------|\n| Go | Main.go | `go build -o Main Main.go` | `./Main` |\n| Java | Main.java | `javac -d . Main.java` | `java Main` |\n| C | Main.c | `gcc Main.c -o Main` | `./Main` |\n| C++ | Main.cpp | `g++ Main.cpp -o Main` | `./Main` |\n| Python | Main.py | 无需编译 | `python3 Main.py` |\n\n## 判题状态说明\n\n| 状态类型 | 状态代码 | 含义说明 |\n|----------|----------|----------|\n| 成功状态 | `ACCEPTED` | 程序输出完全正确 |\n| 成功状态 | `PARTIAL_ACCEPTED` | 程序输出部分正确 |\n| 编译相关 | `COMPILATION_ERROR` | 源代码编译失败 |\n| 编译相关 | `COMPILED_OK` | 源代码编译成功 |\n| 运行相关 | `TIME_LIMIT_EXCEEDED` | 程序运行超时 |\n| 运行相关 | `MEMORY_LIMIT_EXCEEDED` | 内存使用超限 |\n| 运行相关 | `RUNTIME_ERROR` | 程序运行时出错 |\n| 输出相关 | `WRONG_ANSWER` | 程序输出答案错误 |\n| 输出相关 | `PRESENTATION_ERROR` | 输出格式不符合要求 |\n\n## 重要注意事项\n\n### 1. 代码规范要求\n- **文件命名**：系统文件将命名为`Main`（具体如`Main.java`、`Main.py`等）\n- **入口类**：Java程序必须包含`Main`类，且该类应为`public`\n- **程序入口**：所有程序都应有`main`函数作为程序入口点\n\n### 2. 输入输出要求\n- 使用标准输入输出（`stdin/stdout`）\n- 不要使用文件输入输出，否则会导致判题失败\n- 输出格式必须与题目要求完全一致，包括空格、换行（末尾换行将忽略）等\n\n### 3. 资源限制\n- **时间限制**：各题目有不同的时间限制，通常在`1-10`秒（显示为`ms`）之间\n- **内存限制**：通常为`1MB-250MB`（显示为`KB`），请避免创建过大的数据结构\n\n### 4. 常见问题解决\n- **编译错误**：检查语法错误、缺少分号、括号不匹配等\n- **运行时错误**：检查数组越界、空指针、除零错误等\n- **时间超限**：优化算法时间复杂度，避免无效循环\n- **内存超限**：减少不必要的内存分配，使用更高效的数据结构\n\n## 建议与技巧\n\n1. **本地测试**：在提交前务必在本地进行充分测试\n2. **边界情况**：特别注意输入数据的边界条件测试\n3. **特殊值**：考虑空输入、极大值、极小值等情况\n4. **代码简洁**：避免不必要的复杂逻辑，保持代码清晰\n\n如果您在判题过程中遇到任何问题，或有改进建议，欢迎通过反馈渠道联系我们。祝您编程愉快！\n\n平台技术团队敬上', 0, '2025-10-28 15:32:28', '1', '2025-10-29 09:13:06', '1', 1);
INSERT INTO `sys_notice` VALUES ('1983191820421623809', '关于判题机临时停机维护的公告', 'http://120.26.180.149:9000/astro-code-oj/0953d4479afe4ef0afc1c6c5489cd5d1.png', '', 1, '尊敬的各位用户：\n\n为了提升OJ（Online Judge）系统的稳定性与判题效率，为您提供更优质的服务，我们将对**判题机集群**进行例行维护与升级。\n\n在此期间，**代码提交和在线判题功能将暂时无法使用**。给您带来的不便，我们深表歉意，敬请谅解。\n\n**■ 维护时间安排**\n\n*   **开始时间：** 2025年10月31日（周五）\n*   **结束时间：** 2025年11月5日（周二）\n\n**■ 影响范围**\n\n*   **受影响的功能：**\n    *   代码提交\n    *   实时判题\n    *   评测结果返回\n*   **不受影响的功能：**\n    *   网站正常访问、浏览题目\n    *   用户登录、注册\n\n**■ 维护内容**\n\n **系统优化：** 更新判题核心，修复已知问题，增强对多种编程语言的支持。\n\n**■ 注意事项**\n\n1.  维护期间，您仍然可以浏览题目和进行思考。\n2.  维护结束后，所有功能将恢复正常。如果维护提前完成，我们将第一时间恢复服务。\n\n再次感谢您的理解与支持！我们将尽全力缩短维护时间，并为您带来更稳定、更快速的判题体验。', 0, '2025-10-28 23:19:11', '1', '2025-10-29 19:01:08', '1', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
  `data_scope` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据范围',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `assign_group_ids` json NULL COMMENT '角色分配用户组',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_code`(`code` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_data_scope`(`data_scope` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES ('1', '超级管理员', 'SUPER', 'ALL', '超级管理员（拥有系统最高权限）', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-28 14:50:55', '1');
INSERT INTO `sys_role` VALUES ('2', '普通管理员', 'ADMIN', 'ALL', '管理员（仅次于超级管理员）', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-28 14:50:28', '1');
INSERT INTO `sys_role` VALUES ('3', '普通用户', 'USER', 'SELF', '普通用户（什么权限都没有）', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-28 14:50:12', '1');
INSERT INTO `sys_role` VALUES ('4', '用户组超管', 'SUPER_GROUP', 'SELF_GROUP', '用户组超管（能够管理本组以及本组一下）', NULL, 0, '2025-10-24 11:36:18', '1', '2025-10-28 14:49:32', '1');
INSERT INTO `sys_role` VALUES ('5', '用户组管理员', 'GROUP', 'SELF_GROUP_ONLY', '用户组管理员（能够管理本组）', '[]', 0, '2025-10-24 11:37:33', '1', '2025-11-03 17:13:29', '1');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID',
  `menu_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-菜单 关联表(1-N)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES ('1', '1', '1');
INSERT INTO `sys_role_menu` VALUES ('10', '1', '106');
INSERT INTO `sys_role_menu` VALUES ('11', '1', '107');
INSERT INTO `sys_role_menu` VALUES ('12', '1', '108');
INSERT INTO `sys_role_menu` VALUES ('13', '1', '1979358311883591681');
INSERT INTO `sys_role_menu` VALUES ('14', '1', '1979359562444050434');
INSERT INTO `sys_role_menu` VALUES ('15', '1', '1979779968216379394');
INSERT INTO `sys_role_menu` VALUES ('16', '1', '1979781411367333889');
INSERT INTO `sys_role_menu` VALUES ('17', '1', '1979784519254323202');
INSERT INTO `sys_role_menu` VALUES ('18', '1', '1979788036882870274');
INSERT INTO `sys_role_menu` VALUES ('19', '1', '1979792211910332418');
INSERT INTO `sys_role_menu` VALUES ('2', '1', '100');
INSERT INTO `sys_role_menu` VALUES ('20', '1', '1980094089497628673');
INSERT INTO `sys_role_menu` VALUES ('21', '1', '1980165871264997378');
INSERT INTO `sys_role_menu` VALUES ('22', '1', '3');
INSERT INTO `sys_role_menu` VALUES ('23', '1', '301');
INSERT INTO `sys_role_menu` VALUES ('24', '1', '302');
INSERT INTO `sys_role_menu` VALUES ('25', '1', '303');
INSERT INTO `sys_role_menu` VALUES ('26', '1', '306');
INSERT INTO `sys_role_menu` VALUES ('27', '1', '4');
INSERT INTO `sys_role_menu` VALUES ('28', '1', '401');
INSERT INTO `sys_role_menu` VALUES ('29', '1', '500');
INSERT INTO `sys_role_menu` VALUES ('3', '1', '10019');
INSERT INTO `sys_role_menu` VALUES ('30', '1', '600');
INSERT INTO `sys_role_menu` VALUES ('31', '1', '700');
INSERT INTO `sys_role_menu` VALUES ('32', '2', '1');
INSERT INTO `sys_role_menu` VALUES ('33', '2', '100');
INSERT INTO `sys_role_menu` VALUES ('34', '2', '10019');
INSERT INTO `sys_role_menu` VALUES ('35', '2', '101');
INSERT INTO `sys_role_menu` VALUES ('36', '2', '102');
INSERT INTO `sys_role_menu` VALUES ('37', '2', '103');
INSERT INTO `sys_role_menu` VALUES ('38', '2', '104');
INSERT INTO `sys_role_menu` VALUES ('39', '2', '105');
INSERT INTO `sys_role_menu` VALUES ('4', '1', '101');
INSERT INTO `sys_role_menu` VALUES ('40', '2', '106');
INSERT INTO `sys_role_menu` VALUES ('41', '2', '107');
INSERT INTO `sys_role_menu` VALUES ('42', '2', '108');
INSERT INTO `sys_role_menu` VALUES ('43', '2', '1980094089497628673');
INSERT INTO `sys_role_menu` VALUES ('44', '2', '1980165871264997378');
INSERT INTO `sys_role_menu` VALUES ('45', '2', '3');
INSERT INTO `sys_role_menu` VALUES ('46', '2', '301');
INSERT INTO `sys_role_menu` VALUES ('47', '2', '302');
INSERT INTO `sys_role_menu` VALUES ('49', '2', '303');
INSERT INTO `sys_role_menu` VALUES ('5', '1', '102');
INSERT INTO `sys_role_menu` VALUES ('50', '2', '306');
INSERT INTO `sys_role_menu` VALUES ('51', '2', '4');
INSERT INTO `sys_role_menu` VALUES ('52', '2', '401');
INSERT INTO `sys_role_menu` VALUES ('53', '2', '500');
INSERT INTO `sys_role_menu` VALUES ('54', '2', '600');
INSERT INTO `sys_role_menu` VALUES ('55', '2', '700');
INSERT INTO `sys_role_menu` VALUES ('56', '4', '1');
INSERT INTO `sys_role_menu` VALUES ('57', '4', '100');
INSERT INTO `sys_role_menu` VALUES ('58', '4', '101');
INSERT INTO `sys_role_menu` VALUES ('59', '4', '102');
INSERT INTO `sys_role_menu` VALUES ('6', '1', '103');
INSERT INTO `sys_role_menu` VALUES ('60', '4', '104');
INSERT INTO `sys_role_menu` VALUES ('61', '4', '105');
INSERT INTO `sys_role_menu` VALUES ('62', '4', '301');
INSERT INTO `sys_role_menu` VALUES ('63', '4', '302');
INSERT INTO `sys_role_menu` VALUES ('64', '4', '401');
INSERT INTO `sys_role_menu` VALUES ('65', '4', '600');
INSERT INTO `sys_role_menu` VALUES ('66', '4', '700');
INSERT INTO `sys_role_menu` VALUES ('67', '5', '1');
INSERT INTO `sys_role_menu` VALUES ('68', '5', '100');
INSERT INTO `sys_role_menu` VALUES ('69', '5', '101');
INSERT INTO `sys_role_menu` VALUES ('7', '1', '104');
INSERT INTO `sys_role_menu` VALUES ('70', '5', '102');
INSERT INTO `sys_role_menu` VALUES ('71', '5', '301');
INSERT INTO `sys_role_menu` VALUES ('72', '5', '302');
INSERT INTO `sys_role_menu` VALUES ('73', '5', '303');
INSERT INTO `sys_role_menu` VALUES ('74', '5', '306');
INSERT INTO `sys_role_menu` VALUES ('75', '5', '401');
INSERT INTO `sys_role_menu` VALUES ('76', '5', '500');
INSERT INTO `sys_role_menu` VALUES ('77', '5', '600');
INSERT INTO `sys_role_menu` VALUES ('78', '5', '700');
INSERT INTO `sys_role_menu` VALUES ('8', '1', '105');
INSERT INTO `sys_role_menu` VALUES ('9', '1', '10574');

-- ----------------------------
-- Table structure for sys_tag
-- ----------------------------
DROP TABLE IF EXISTS `sys_tag`;
CREATE TABLE `sys_tag`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tag
-- ----------------------------
INSERT INTO `sys_tag` VALUES ('0', '无标签', 0, '2025-10-20 15:33:59', '0', '2025-10-20 15:33:59', '0');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户组',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `background` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '背景图片',
  `quote` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '签名',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `telephone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `login_time` datetime NULL DEFAULT NULL COMMENT '登录时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_telephone`(`telephone` ASC) USING BTREE,
  UNIQUE INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('0', '1', 'default', '$2a$10$CGf0BGdflI3iG6Fl83Vjz.kE.XdhMSu.sstPIeNI.9WFz8FR/lmS.', 'Astro Code', 'http://120.26.180.149:9000/astro-code-oj/843a7d9124af408993d3c2c268065228.png', 'http://120.26.180.149:9000/astro-code-oj/256e801aaa3143cb976f97cb3dd22c67.jpg', '欢迎使用 Astro Code OJ', 0, 'default@163.com', NULL, NULL, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-29 17:29:53', '1');
INSERT INTO `sys_user` VALUES ('1', '2', 'superadmin', '$2a$10$hS6E7n8tqGZMX2qOzLyk2.pospgtnLo.8gjXAtfttGruIh9AE2lgG', 'SuperAdmin', 'http://120.26.180.149:9000/astro-code-oj/0d4aeb3ee4fc4624bb947226db9b80a6.jpg', 'http://120.26.180.149:9000/astro-code-oj/a6cf5977866e49d78bf4423e7aeda1df.jpg', '你好，世界', 1, 'superadmin@163.com', NULL, NULL, '2025-11-06 22:48:37', 0, '2025-10-21 12:25:12', '1', '2025-11-06 22:48:37', '1');
INSERT INTO `sys_user` VALUES ('2', '3', 'adminadmin', '$2a$10$hS6E7n8tqGZMX2qOzLyk2.pospgtnLo.8gjXAtfttGruIh9AE2lgG', 'Admin', 'http://120.26.180.149:9000/astro-code-oj/4b3319245b80424b8645b99bf780ffde.jpg', 'http://120.26.180.149:9000/astro-code-oj/ca4196dde3bf44e78f8b219fb6a213a0.jpg', '你好，世界', 0, 'adminadmin@163.com', NULL, NULL, '2025-10-07 13:20:00', 0, '2025-10-21 12:25:12', '1', '2025-10-28 14:48:40', '1');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户-角色 关联表(1-N)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES ('1', '0', '3');
INSERT INTO `sys_user_role` VALUES ('2', '1', '1');
INSERT INTO `sys_user_role` VALUES ('3', '2', '2');
INSERT INTO `sys_user_role` VALUES ('4', '3', '1');
INSERT INTO `sys_user_role` VALUES ('5', '3', '2');
INSERT INTO `sys_user_role` VALUES ('6', '4', '1');
INSERT INTO `sys_user_role` VALUES ('7', '4', '2');
INSERT INTO `sys_user_role` VALUES ('8', '5', '1');
INSERT INTO `sys_user_role` VALUES ('9', '5', '2');

-- ----------------------------
-- Table structure for task_reports
-- ----------------------------
DROP TABLE IF EXISTS `task_reports`;
CREATE TABLE `task_reports`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `report_type` int NULL DEFAULT 0 COMMENT '报告类型',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务ID',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `sample_count` int NULL DEFAULT 0 COMMENT '样例数量',
  `similarity_group_count` int NULL DEFAULT 0 COMMENT '相似组数量',
  `avg_similarity` decimal(10, 2) UNSIGNED NULL DEFAULT 0.00 COMMENT '平均相似度',
  `max_similarity` decimal(10, 2) UNSIGNED NULL DEFAULT 0.00 COMMENT '最大相似度',
  `threshold` decimal(10, 2) UNSIGNED NULL DEFAULT 0.50 COMMENT '检测阈值',
  `similarity_distribution` json NULL COMMENT '相似度分布',
  `degree_statistics` json NULL COMMENT '程度统计',
  `check_mode` int NULL DEFAULT 1 COMMENT '检测模式',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_problem_id`(`problem_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '报告库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_reports
-- ----------------------------

-- ----------------------------
-- Table structure for task_similarity
-- ----------------------------
DROP TABLE IF EXISTS `task_similarity`;
CREATE TABLE `task_similarity`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务ID',
  `task_type` tinyint(1) NULL DEFAULT 0 COMMENT '手动',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
  `module_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块类型',
  `module_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块ID',
  `language` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
  `similarity` decimal(10, 2) UNSIGNED NULL DEFAULT 0.00 COMMENT '相似度',
  `submit_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交用户',
  `submit_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '源代码',
  `submit_code_length` int NULL DEFAULT 0 COMMENT '源代码长度',
  `submit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `submit_code_token` json NULL COMMENT '源代码Token标记',
  `submit_token_name` json NULL COMMENT '提交用户Token名称',
  `submit_token_texts` json NULL COMMENT '提交用户Token内容',
  `origin_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样本用户',
  `origin_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '样本源代码',
  `origin_code_length` int NULL DEFAULT 0 COMMENT '样本源代码长度',
  `origin_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样本提交ID',
  `origin_time` datetime NULL DEFAULT NULL COMMENT '样本提交时间',
  `origin_code_token` json NULL COMMENT '源代码Token标记',
  `origin_token_name` json NULL COMMENT '样本用户Token名称',
  `origin_token_texts` json NULL COMMENT '样本用户Token内容',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间戳',
  `create_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间戳',
  `update_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '检测结果任务库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_similarity
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
