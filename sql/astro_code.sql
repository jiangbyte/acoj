/*
 Navicat Premium Dump SQL

 Source Server         : 47.99.236.69
 Source Server Type    : MySQL
 Source Server Version : 90400 (9.4.0)
 Source Host           : 47.99.236.69:3306
 Source Schema         : astro_code

 Target Server Type    : MySQL
 Target Server Version : 90400 (9.4.0)
 File Encoding         : 65001

 Date: 26/10/2025 01:12:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS astro_code;

USE astro_code;

-- ----------------------------
-- Table structure for ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_memory`;
CREATE TABLE `ai_chat_memory`
(
    `id`              BIGINT                                                        NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `content`         LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NOT NULL,
    `type`            VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `timestamp`       TIMESTAMP                                                     NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    CONSTRAINT `chk_message_type` CHECK (`type` IN
                                         (_utf8mb4'USER', _utf8mb4'ASSISTANT', _utf8mb4'SYSTEM', _utf8mb4'TOOL'))
) ENGINE = InnoDB
  AUTO_INCREMENT = 1046
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_library
-- ----------------------------
DROP TABLE IF EXISTS `data_library`;
CREATE TABLE `data_library`
(
    `id`           VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `user_id`      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
    `set_id`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`       TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否是题集提交',
    `problem_id`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `submit_id`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
    `submit_time`  DATETIME                                                     NULL DEFAULT NULL COMMENT '提交时间',
    `language`     VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
    `code`         TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NULL COMMENT '源代码',
    `code_length`  INT                                                          NULL DEFAULT 0 COMMENT '源代码长度',
    `access_count` INT                                                          NULL DEFAULT 0 COMMENT '访问次数',
    `deleted`      TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`  DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time`  DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `problem_id` (`problem_id` ASC, `user_id` ASC) USING BTREE,
    INDEX `user_id` (`user_id` ASC, `problem_id` ASC) USING BTREE,
    INDEX `submit_id` (`submit_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '提交样本库'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_problem
-- ----------------------------
DROP TABLE IF EXISTS `data_problem`;
CREATE TABLE `data_problem`
(
    `id`                VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `display_id`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '展示ID',
    `category_id`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '分类',
    `title`             VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
    `source`            VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
    `url`               VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
    `max_time`          INT                                                           NULL DEFAULT 0 COMMENT '时间限制',
    `max_memory`        INT                                                           NULL DEFAULT 0 COMMENT '内存限制',
    `description`       TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '描述',
    `test_case`         JSON                                                          NULL COMMENT '用例',
    `allowed_languages` JSON                                                          NULL COMMENT '开放语言',
    `difficulty`        INT                                                           NULL DEFAULT 1 COMMENT '难度',
    `threshold`         DECIMAL(10, 2) UNSIGNED                                       NULL DEFAULT 0.50 COMMENT '阈值',
    `use_template`      TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '使用模板',
    `code_template`     JSON                                                          NULL COMMENT '模板代码',
    `is_public`         TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '是否公开',
    `is_visible`        TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '是否可见',
    `use_ai`            TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '是否使用AI',
    `solved`            BIGINT                                                        NULL DEFAULT 0 COMMENT '解决',
    `deleted`           TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`       DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`       DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '题目表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_problem_tag
-- ----------------------------
DROP TABLE IF EXISTS `data_problem_tag`;
CREATE TABLE `data_problem_tag`
(
    `problem_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
    `tag_id`     VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '题目标签关联表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_progress
-- ----------------------------
DROP TABLE IF EXISTS `data_progress`;
CREATE TABLE `data_progress`
(
    `id`           VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `user_id`      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
    `set_id`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `problem_id`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `status`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
    `extra_json`   JSON                                                         NULL COMMENT '额外信息',
    `completed`    TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否完成',
    `completed_at` DATETIME                                                     NULL DEFAULT NULL COMMENT '完成时间',
    `deleted`      TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`  DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time`  DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '题集进度表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_set
-- ----------------------------
DROP TABLE IF EXISTS `data_set`;
CREATE TABLE `data_set`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `set_type`    INT                                                           NULL DEFAULT 1 COMMENT '题集类型',
    `title`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
    `cover`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
    `description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '描述',
    `category_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '分类',
    `difficulty`  INT                                                           NULL DEFAULT 1 COMMENT '难度',
    `start_time`  DATETIME                                                      NULL DEFAULT NULL COMMENT '开始时间',
    `end_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '结束时间',
    `ex_json`     JSON                                                          NULL COMMENT '额外的信息',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    `is_visible`  TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '是否可见',
    `use_ai`      TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '是否使用AI',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '题集'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_set_problem
-- ----------------------------
DROP TABLE IF EXISTS `data_set_problem`;
CREATE TABLE `data_set_problem`
(
    `set_id`     VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题集ID',
    `problem_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目ID',
    `sort`       INT                                                          NOT NULL DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '题集题目'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_solved
-- ----------------------------
DROP TABLE IF EXISTS `data_solved`;
CREATE TABLE `data_solved`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `set_id`      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`      TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否是题集提交',
    `user_id`     VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
    `problem_id`  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `submit_id`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
    `solved`      TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否解决',
    `deleted`     TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_problem_set` (`user_id` ASC, `problem_id` ASC, `set_id` ASC, `is_set` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户解决表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for data_submit
-- ----------------------------
DROP TABLE IF EXISTS `data_submit`;
CREATE TABLE `data_submit`
(
    `id`                  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `user_id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ID',
    `set_id`              VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`              TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否是题集提交',
    `problem_id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `language`            VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
    `code`                TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NULL COMMENT '源代码',
    `code_length`         INT                                                          NULL DEFAULT 0 COMMENT '源代码长度',
    `submit_type`         TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '执行类型',
    `max_time`            INT                                                          NULL DEFAULT 0 COMMENT '最大耗时',
    `max_memory`          INT                                                          NULL DEFAULT 0 COMMENT '最大内存使用',
    `message`             TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NULL COMMENT '执行结果消息',
    `test_case`           JSON                                                         NULL COMMENT '测试用例结果',
    `status`              VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行状态',
    `is_finish`           TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '流程流转是否完成',
    `similarity`          DECIMAL(10, 2)                                               NULL DEFAULT 0.00 COMMENT '相似度',
    `task_id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似检测任务ID',
    `report_id`           VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '报告ID',
    `similarity_category` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似分级',
    `deleted`             TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`         DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time`         DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    `judge_task_id`       VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相似检测任务ID',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `problem_id` (`problem_id` ASC, `user_id` ASC) USING BTREE,
    INDEX `task_id` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '提交表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_article
-- ----------------------------
DROP TABLE IF EXISTS `sys_article`;
CREATE TABLE `sys_article`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `title`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
    `subtitle`    VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子标题',
    `cover`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
    `author`      VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
    `summary`     VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '摘要',
    `sort`        TINYINT                                                       NULL DEFAULT 99 COMMENT '排序',
    `to_url`      VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
    `parent_id`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '父级',
    `type`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '类型',
    `category`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '分类',
    `content`     TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '内容',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统文章表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_banner
-- ----------------------------
DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner`
(
    `id`                  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `title`               VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
    `banner`              VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '横幅',
    `button_text`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '按钮文字',
    `is_visible_button`   TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '按钮是否可见',
    `jump_module`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转模块',
    `jump_type`           VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转类别',
    `jump_target`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转目标',
    `target_blank`        TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '新窗口打开',
    `sort`                TINYINT                                                       NULL DEFAULT 99 COMMENT '排序',
    `subtitle`            VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子标题',
    `is_visible_subtitle` TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '子标题是否可见',
    `is_visible`          TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '是否可见',
    `deleted`             TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`         DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`         DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '横幅表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `name`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '分类表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_category
-- ----------------------------
INSERT INTO `sys_category`
VALUES ('0', '未分类', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378237393260546', '枚举', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378253524553729', '模拟', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378267390922753', '递归', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378286730858498', '分治', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378343525928961', '贪心', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378358273101825', '排序', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378372210774018', '二分查找', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378393635278849', '动态规划', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378407564562433', '搜索', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378431908302850', '线性结构', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378454280720385', '树形结构', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378475042525185', '图结构', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378497263947777', '字符串相关结构', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378518076084226', '数论', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378538779168770', '组合数学', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378602398371841', '计算几何', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378620748451841', '博弈论', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378637034934274', '概率与期望', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378653535326209', '矩阵运算与快速幂', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_category`
VALUES ('1961378680622141441', '背包问题', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`
(
    `id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `name`           VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `code`           VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编码',
    `value`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '值',
    `component_type` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件类型',
    `description`    VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
    `deleted`        TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    `config_type`    VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置分类',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config`
VALUES ('1982136140003225601', 'Admin端登录背景', 'APP_ADMIN_LOGIN_BACKGROUND',
        'http://120.26.180.149:9000/astro-code-oj/043d6be2e9c6415798985a3dc991754f.jpg', '6', '登录页面背景图片', 0,
        '2025-10-21 12:25:12', '1', '2025-10-24 09:01:10', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225602', 'Web端登录背景', 'APP_PC_LOGIN_BACKGROUND',
        'https://upload-bbs.miyoushe.com/upload/2024/07/11/292762008/54cbcd3b61a7d6cc4b194d5c48236b4a_8421561160147429361.jpg',
        '6', 'Web应用端登录背景', 0, '2025-10-21 12:25:12', '1', '2025-10-24 09:00:26', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225603', 'Web端注册背景', 'APP_PC_REGISTER_BACKGROUND',
        'https://upload-bbs.miyoushe.com/upload/2024/07/09/247364438/dbeba8ef3d93a4b86b74c080010e3f5e_8407127069598351562.jpg',
        '6', 'Web应用端注册背景', 0, '2025-10-21 12:25:12', '1', '2025-10-24 09:00:31', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225604', 'Web端显示名称', 'APP_PC_SHOW_APP_NAME', 'false', '1', 'false 显示 Logo，true 显示 名称',
        0, '2025-10-21 12:25:12', '1', '2025-10-24 09:00:15', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225605', '用户默认头像', 'APP_DEFAULT_AVATAR',
        'http://120.26.180.149:9000/astro-code-oj/85db17abd2f145a1aa0f4a487866e18e.png', '6', '', 0,
        '2025-10-21 12:25:12', '1', '2025-10-24 11:54:09', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225606', '用户默认背景', 'APP_DEFAULT_USER_BACKGROUND',
        'http://120.26.180.149:9000/astro-code-oj/b21583960cad4fe98ebc533e9153303f.jpg', '6', '', 0,
        '2025-10-21 12:25:12', '1', '2025-10-24 09:00:50', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225607', '2.应用Logo', 'APP_LOGO',
        'http://120.26.180.149:9000/astro-code-oj/1be69ed211b44bedab9b487ddea8d84d.png',
        '6', '建议 1:1 图片上传', 0, '2025-10-21 12:25:12', '1', '2025-10-24 11:48:40', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225608', '1.应用名称', 'APP_NAME', 'Astro Code', '2', '应用名称', 0, '2025-10-21 12:25:12', '1',
        '2025-10-24 11:45:24', '1', '5');
INSERT INTO `sys_config`
VALUES ('1982136140003225609', 'Admin端显示名称', 'APP_ADMIN_SHOW_APP_NAME', 'false', '1', '', 0, '2025-10-21 12:25:12',
        '1',
        '2025-10-24 09:01:27', '1', '5');

-- ----------------------------
-- Table structure for sys_conversation
-- ----------------------------
DROP TABLE IF EXISTS `sys_conversation`;
CREATE TABLE `sys_conversation`
(
    `id`                 VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `conversation_id`    VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '对话ID',
    `problem_id`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '题目ID',
    `set_id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`             TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '题集对话',
    `user_id`            VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '用户ID',
    `message_type`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息类型',
    `message_role`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '消息角色',
    `message_content`    LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci     NULL COMMENT '消息内容',
    `user_code`          TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '用户代码',
    `language`           VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '代码语言',
    `prompt_tokens`      INT                                                           NULL DEFAULT 0 COMMENT '提示Tokens',
    `completion_tokens`  INT                                                           NULL DEFAULT 0 COMMENT '完成Tokens',
    `total_tokens`       INT                                                           NULL DEFAULT 0 COMMENT '总Tokens',
    `response_time`      DATETIME                                                      NULL DEFAULT NULL COMMENT '响应时间',
    `streaming_duration` INT                                                           NULL DEFAULT NULL COMMENT '流式传输总耗时',
    `status`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '状态',
    `error_message`      TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '错误信息',
    `user_platform`      VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户平台',
    `ip_address`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
    `deleted`            TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`        DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`        DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '大模型对话表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '主键ID',
    `dict_type`   VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '字典类型',
    `type_label`  VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '类型名称',
    `dict_value`  VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典值',
    `dict_label`  VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典标签',
    `sort_order`  INT                                                           NULL DEFAULT 0 COMMENT '排序',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_type_code` (`dict_type` ASC, `dict_value` ASC) USING BTREE COMMENT '类型和编码唯一索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict`
VALUES ('1949386814273540097', 'PROBLEM_DIFFICULTY', '难度', '1', '简单', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949387747678171138', 'PROBLEM_DIFFICULTY', '难度', '2', '中等', 2, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949387822563274754', 'PROBLEM_DIFFICULTY', '难度', '3', '困难', 3, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949389559521005569', 'ALLOW_LANGUAGE', '开放语言', 'java', 'Java', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949389612713168898', 'ALLOW_LANGUAGE', '开放语言', 'cpp', 'Cpp', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949389698243416065', 'ALLOW_LANGUAGE', '开放语言', 'go', 'Go', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-24 11:56:07', '1');
INSERT INTO `sys_dict`
VALUES ('1949389728094277634', 'ALLOW_LANGUAGE', '开放语言', 'c', 'C', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949389906322837505', 'ALLOW_LANGUAGE', '开放语言', 'python', 'Python', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949640044622639105', 'YES_NO', '是否', 'true', '是', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949640252018388993', 'YES_NO', '是否', 'false', '否', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949671004755980290', 'SUBMIT_TYPE', '执行类型', 'false', '运行', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949671066571632641', 'SUBMIT_TYPE', '执行类型', 'true', '提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949688687626977282', 'PROBLEM_SET_TYPE', '题集类型', '1', '常规题集', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1949688825984483330', 'PROBLEM_SET_TYPE', '题集类型', '2', '限时题集', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1950883784481759233', 'JUDGE_STATUS', '判题状态', 'ACCEPTED', '答案正确', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195542354329602', 'JUDGE_STATUS', '判题状态', 'PENDING', '等待判题', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195583638863874', 'JUDGE_STATUS', '判题状态', 'JUDGING', '判题中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195620687151105', 'JUDGE_STATUS', '判题状态', 'COMPILING', '编译中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195658800791554', 'JUDGE_STATUS', '判题状态', 'RUNNING', '运行中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195722818453506', 'JUDGE_STATUS', '判题状态', 'WRONG_ANSWER', '答案错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195774957846529', 'JUDGE_STATUS', '判题状态', 'TIME_LIMIT_EXCEEDED', '时间超出限制', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195824698097666', 'JUDGE_STATUS', '判题状态', 'MEMORY_LIMIT_EXCEEDED', '内存超出限制', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195861415034882', 'JUDGE_STATUS', '判题状态', 'RUNTIME_ERROR', '运行时错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195903551012865', 'JUDGE_STATUS', '判题状态', 'COMPILATION_ERROR', '编译错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951195947666702337', 'JUDGE_STATUS', '判题状态', 'PRESENTATION_ERROR', '格式错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951196106874093570', 'JUDGE_STATUS', '判题状态', 'PARTIAL_ACCEPTED', '部分正确', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951196145809817601', 'JUDGE_STATUS', '判题状态', 'SYSTEM_ERROR', '系统错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951196241905516546', 'JUDGE_STATUS', '判题状态', 'REJUDGING', '重新判题中', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1951196276516913154', 'JUDGE_STATUS', '判题状态', 'UNKNOWN_ERROR', '未知错误', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1954009065375625218', 'JUDGE_STATUS', '判题状态', 'COMPILED_OK', '编译成功', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1963850874083848194', 'CLONE_LEVEL', '相似等级', 'HIGHLY_SUSPECTED', '高度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1963851262937772034', 'CLONE_LEVEL', '相似等级', 'MEDIUM_SUSPECTED', '中度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1963851317014933505', 'CLONE_LEVEL', '相似等级', 'LOW_SUSPECTED', '轻度可疑', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1963851373667397634', 'CLONE_LEVEL', '相似等级', 'NOT_REACHED', '未达阈值', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1963876843167334401', 'CLONE_LEVEL', '相似等级', 'NOT_DETECTED', '未检测', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1970841457780269058', 'MENU_TYPE', '菜单类型', '0', '目录', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1970841583638749185', 'MENU_TYPE', '菜单类型', '1', '菜单', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1971400843359612930', 'REPORT_TYPE', '报告类型', '1', '题目单提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1971401085530337282', 'REPORT_TYPE', '报告类型', '2', '题集单提交', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1971403102587875330', 'CHECK_MODE', '相似检测模式', '1', '自动', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1971403149677326337', 'CHECK_MODE', '相似检测模式', '2', '手动', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1971403185362464770', 'CHECK_MODE', '相似检测模式', '3', '定时', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980055469866659841', 'COMPONENT_TYPE', '配置组件类型', '1', '布尔（True/False）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980056161541427202', 'CONFIG_TYPE', '配置类型', '1', '背景图片', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083239932010497', 'CONFIG_TYPE', '配置类型', '2', '系统图标', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083284530044929', 'CONFIG_TYPE', '配置类型', '3', '相似检测', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083320596865026', 'CONFIG_TYPE', '配置类型', '4', '默认参数', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083563702919169', 'COMPONENT_TYPE', '配置组件类型', '2', '文本输入（单行）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083613787103234', 'COMPONENT_TYPE', '配置组件类型', '3', '文本输入（多行）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083741243613186', 'COMPONENT_TYPE', '配置组件类型', '4', '数字输入（整数）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980083995699453953', 'COMPONENT_TYPE', '配置组件类型', '5', '数字输入（小数）', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980090363294654466', 'CONFIG_TYPE', '配置类型', '5', '系统配置', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980091643719249922', 'COMPONENT_TYPE', '配置组件类型', '6', '图片上传', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980198864968962049', 'MESSAGE_TYPE', '对话类型', 'DailyConversation', '默认对话', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980199081676066817', 'MESSAGE_TYPE', '对话类型', 'ProblemSolvingIdeas', '解题思路', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980199161564975106', 'MESSAGE_TYPE', '对话类型', 'OptimizeCode', '优化代码', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980199239746801665', 'MESSAGE_TYPE', '对话类型', 'AnalyzeBoundary', '分析边界', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980199313264562177', 'MESSAGE_TYPE', '对话类型', 'AnalyzeComplexity', '分析复杂度', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980283142327119873', 'MESSAGE_ROLE', '对话角色', 'ASSISTANT', '助手', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1980283213173108738', 'MESSAGE_ROLE', '对话角色', 'USER', '用户', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1981232874954690561', 'JUMP_MODULE', '跳转模块', 'HREF', '外部链接', 0, 0, '2025-10-23 13:35:02', '1', '2025-10-23 13:35:02', '1');
INSERT INTO `sys_dict`
VALUES ('1981233060934324226', 'JUMP_MODULE', '跳转模块', 'NOTICE', '公告', 0, 0, '2025-10-23 13:35:46', '1', '2025-10-23 13:35:46', '1');
INSERT INTO `sys_dict`
VALUES ('1981233109919600641', 'JUMP_MODULE', '跳转模块', 'PERSONAL', '个人主页', 0, 0, '2025-10-23 13:35:58', '1', '2025-10-23 13:35:58', '1');
INSERT INTO `sys_dict`
VALUES ('1981233183928094722', 'JUMP_MODULE', '跳转模块', 'PROBLEM', '题目详情', 0, 0, '2025-10-23 13:36:15', '1', '2025-10-23 13:36:15', '1');
INSERT INTO `sys_dict`
VALUES ('1981233236465946625', 'JUMP_MODULE', '跳转模块', 'PAGE_PROBLEM', '题目页面', 0, 0, '2025-10-23 13:36:28', '1', '2025-10-23 13:36:28', '1');
INSERT INTO `sys_dict`
VALUES ('1981233285115678722', 'JUMP_MODULE', '跳转模块', 'SET', '题集详情', 0, 0, '2025-10-23 13:36:39', '1', '2025-10-23 13:36:39', '1');
INSERT INTO `sys_dict`
VALUES ('1981233328170209281', 'JUMP_MODULE', '跳转模块', 'PAGE_SET', '题集页面', 0, 0, '2025-10-23 13:36:50', '1', '2025-10-23 13:36:50', '1');
INSERT INTO `sys_dict`
VALUES ('1981233379021950978', 'JUMP_MODULE', '跳转模块', 'NOTDO', '不做处理', 0, 0, '2025-10-23 13:37:02', '1', '2025-10-23 13:37:02', '1');
INSERT INTO `sys_dict`
VALUES ('1981233496931741698', 'JUMP_TYPE', '跳转类别', 'URL', 'URL', 0, 0, '2025-10-23 13:37:30', '1', '2025-10-23 13:37:30', '1');
INSERT INTO `sys_dict`
VALUES ('1981233544852148225', 'JUMP_TYPE', '跳转类别', 'ID', 'ID', 0, 0, '2025-10-23 13:37:41', '1', '2025-10-23 13:37:41', '1');
INSERT INTO `sys_dict`
VALUES ('1981551937107005441', 'DATA_SCOPE', '数据范围', 'SELF', '仅自己', 0, 0, '2025-10-24 10:42:52', '1', '2025-10-24 10:42:52', '1');
INSERT INTO `sys_dict`
VALUES ('1981552252866793473', 'DATA_SCOPE', '数据范围', 'SELF_GROUP', '本组及下级', 0, 0, '2025-10-24 10:44:07', '1', '2025-10-24 10:44:07', '1');
INSERT INTO `sys_dict`
VALUES ('1981552438887886849', 'DATA_SCOPE', '数据范围', 'SELF_GROUP_ONLY', '仅本组', 0, 0, '2025-10-24 10:44:52', '1', '2025-10-24 10:44:52', '1');
INSERT INTO `sys_dict`
VALUES ('1981552501403987969', 'DATA_SCOPE', '数据范围', 'SPECIFIED_GROUP', '指定组', 0, 0, '2025-10-24 10:45:06', '1', '2025-10-24 10:45:06', '1');
INSERT INTO `sys_dict`
VALUES ('1981552564389851137', 'DATA_SCOPE', '数据范围', 'ALL', '所有数据', 0, 0, '2025-10-24 10:45:21', '1', '2025-10-24 10:45:21', '1');
INSERT INTO `sys_dict`
VALUES ('1981611294175309825', 'TIME_STATUS', '时间状态', '1', '未开始', 0, 0, '2025-10-24 14:38:44', '1', '2025-10-24 14:38:44', '1');
INSERT INTO `sys_dict`
VALUES ('1981612262153564161', 'TIME_STATUS', '时间状态', '2', '进行中', 0, 0, '2025-10-24 14:42:35', '1', '2025-10-24 14:42:35', '1');
INSERT INTO `sys_dict`
VALUES ('1981612310983651329', 'TIME_STATUS', '时间状态', '3', '已结束', 0, 0, '2025-10-24 14:42:46', '1', '2025-10-24 14:42:46', '1');
INSERT INTO `sys_dict`
VALUES ('1982137082970841089', 'SYS_GENDER', '性别', '0', '未知', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1982137082970841090', 'SYS_GENDER', '性别', '1', '男', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_dict`
VALUES ('1982137082970841091', 'SYS_GENDER', '性别', '2', '女', 2, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_group`;
CREATE TABLE `sys_group`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户组',
    `parent_id`   VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '父级用户组',
    `name`        VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `code`        VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '编码',
    `description` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
    `sort`        TINYINT                                                       NULL DEFAULT 99 COMMENT '排序',
    `admin_id`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '负责人',
    `group_type`  TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '系统组',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_group_code` (`code` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户组表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_group
-- ----------------------------
INSERT INTO `sys_group`
VALUES ('0', '0', '平台', 'DEFAULT_GROUP', '平台', 0, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-25 21:29:39', '1');
INSERT INTO `sys_group`
VALUES ('1982138236630949890', '0', '默认用户', 'DEFAULT_GROUP', '默认用户组', 1, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-25 21:29:39', '1');
INSERT INTO `sys_group`
VALUES ('1982138236630949891', '0', '系统管理组', 'SUPER_GROUP', '超级管理员组', 2, '1', 1, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_group`
VALUES ('1982138236630949892', '0', '管理员组', 'ADMIN_GROUP', '管理员组', 3, '1', 0, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`
(
    `id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `user_id`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '用户ID',
    `operation`      VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作',
    `method`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '方法',
    `params`         TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '参数',
    `ip`             VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP',
    `operation_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '操作时间',
    `category`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作分类',
    `module`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模块名称',
    `description`    VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
    `status`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作状态',
    `message`        TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '操作消息',
    `deleted`        TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统活动/日志记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '菜单ID',
    `pid`            VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT '0' COMMENT '父菜单ID',
    `name`           VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单名称（英文标识）',
    `path`           VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
    `component_path` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
    `title`          VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单标题',
    `icon`           VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
    `keep_alive`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '是否缓存',
    `visible`        TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '是否可见',
    `sort`           INT                                                           NULL DEFAULT 0 COMMENT '排序',
    `pined`          TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '是否固定',
    `menu_type`      INT                                                           NULL DEFAULT 0 COMMENT '菜单类型：0-目录，1-菜单',
    `ex_json`        JSON                                                          NULL COMMENT '额外信息',
    `deleted`        TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    `parameters`     VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单头部参数',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '菜单表'
  ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES ('1', '0', 'home', '/index', '/workbench/index.vue', '首页', 'icon-park-outline:home', 0, 1, 1, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('100', '0', 'content_index', '/content', NULL, '内容管理', 'icon-park-outline:setting-config', 0, 1, 3, 0, 0, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('10019', '0', 'system_index', '/system', NULL, '系统管理', 'icon-park-outline:setting-config', 0, 1, 4, 0, 0, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('101', '100', 'banner', '/content/banner', '/modular/sys/banner/index.vue', '横幅管理', 'icon-park-outline:ad-product', 0, 1, 1, 0, 1, '[
  \"/sys/banner/add\",
  \"/sys/banner/delete\",
  \"/sys/banner/detail\",
  \"/sys/banner/page\",
  \"/sys/banner/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('102', '100', 'notice', '/content/notice', '/modular/sys/notice/index.vue', '公告管理', 'icon-park-outline:volume-notice', 0, 1, 2, 0, 1, '[
  \"/sys/notice/add\",
  \"/sys/notice/delete\",
  \"/sys/notice/detail\",
  \"/sys/notice/page\",
  \"/sys/notice/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('103', '10019', 'role', '/system/role', '/modular/sys/role/index.vue', '角色管理', 'icon-park-outline:people-safe', 0, 1, 2, 0, 1, '[
  \"/sys/role/add\",
  \"/sys/role/delete\",
  \"/sys/role/detail\",
  \"/sys/role/page\",
  \"/sys/role/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('104', '10019', 'user', '/system/user', '/modular/sys/user/index.vue', '用户管理', 'icon-park-outline:user', 0, 1, 1, 0, 1, '[
  \"/sys/user/add\",
  \"/sys/user/delete\",
  \"/sys/user/detail\",
  \"/sys/user/page\",
  \"/sys/user/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('105', '10019', 'group', '/system/group', '/modular/sys/group/index.vue', '用户组管理', 'icon-park-outline:peoples', 0, 1, 3, 0, 1, '[
  \"/sys/group/add\",
  \"/sys/group/delete\",
  \"/sys/group/detail\",
  \"/sys/group/page\",
  \"/sys/group/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('10574', '10019', 'config', '/system/config', '/modular/sys/config/index.vue', '环境变量', 'icon-park-outline:enter-the-keyboard', 0, 1, 4, 0, 1, '[
  \"/sys/config/add\",
  \"/sys/config/delete\",
  \"/sys/config/detail\",
  \"/sys/config/page\",
  \"/sys/config/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('106', '10019', 'dict', '/system/dict', '/modular/sys/dict/index.vue', '字典管理', 'icon-park-outline:book', 0, 1, 5, 0, 1, '[
  \"/sys/dict/add\",
  \"/sys/dict/delete\",
  \"/sys/dict/detail\",
  \"/sys/dict/page\",
  \"/sys/dict/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('107', '10019', 'menu', '/system/menu', '/modular/sys/menu/index.vue', '菜单管理', 'icon-park-outline:application-menu', 0, 1, 6, 0, 1, '[
  \"/sys/menu/add\",
  \"/sys/menu/delete\",
  \"/sys/menu/detail\",
  \"/sys/menu/page\",
  \"/sys/menu/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('108', '10019', 'log', '/system/log', '/modular/sys/log/index.vue', '系统日志', 'icon-park-outline:log', 0, 1, 7, 0, 1, '[
  \"/sys/log/add\",
  \"/sys/log/delete\",
  \"/sys/log/detail\",
  \"/sys/log/page\",
  \"/sys/log/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:13:49', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('1979358311883591681', '0', 'outweb_index', '/outweb', NULL, '外部管理', 'icon-park-outline:application-two', 0, 1, 14, 0, 0, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('1979359157794377729', '1979358311883591681', 'nacos', '/outweb/nacos', '/outweb/nacos.vue', 'Nacos', 'icon-park-outline:setting-config', 0, 0, 2, 0, 1, '[]', 1, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('1979359562444050434', '1979358311883591681', 'dozzle120', '/outweb/dozzle120', '/outweb/index.vue', '120服务器日志', 'icon-park-outline:log', 0, 1, 3, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:50', '1', '{\n    \"href_inner\": \"http://120.26.180.149:9001/\"\n}');
INSERT INTO `sys_menu`
VALUES ('1979779968216379394', '1979358311883591681', 'dozzle47', '/outweb/dozzle47', '/outweb/index.vue', '47服务器日志', 'icon-park-outline:log', 0, 1, 2, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:45', '1', '{\r\n    \"href_inner\": \"http://47.99.236.69:9001/\"\r\n}');
INSERT INTO `sys_menu`
VALUES ('1979781411367333889', '1979358311883591681', 'portainer', '/outweb/portainer', '/outweb/index.vue', 'Portainer', 'icon-park-outline:application', 0, 1, 4, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:55', '1', '{\r\n    \"href_inner\": \"http://101.201.174.43:9020/\"\r\n}');
INSERT INTO `sys_menu`
VALUES ('1979784519254323202', '1979358311883591681', 'nacos', '/outweb/nacos', '/outweb/index.vue', 'Nacos', 'icon-park-outline:link-cloud', 0, 1, 5, 0, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:01:43', '1', '{\r\n    \"href_outter\": \"http://120.26.180.149:8848/nacos\"\r\n}');
INSERT INTO `sys_menu`
VALUES ('1979788036882870274', '1979358311883591681', 'minio', '/outweb/minio', '/outweb/index.vue', 'Minio', 'icon-park-outline:file-cabinet', 0, 1, 6, 0, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:01:50', '1', '{\r\n    \"href_outter\": \"http://120.26.180.149:9090/\"\r\n}');
INSERT INTO `sys_menu`
VALUES ('1979792211910332418', '1979358311883591681', 'dozzle101', '/outweb/dozzle101', '/outweb/index.vue', '101服务器日志', 'icon-park-outline:log', 0, 1, 1, 1, 1, '[]', 0, '2025-10-21 12:25:12', '1', '2025-10-24 12:00:40', '1', '{\r\n    \"href_inner\": \"http://101.201.174.43:9995/\"\r\n}');
INSERT INTO `sys_menu`
VALUES ('1980094089497628673', '10019', 'config_views', '/system/config/views', '/modular/sys/config/views.vue', '系统配置', 'icon-park-outline:enter-the-keyboard', 0, 1, 8, 0, 1, '[
  \"/sys/config/add\",
  \"/sys/config/delete\",
  \"/sys/config/detail\",
  \"/sys/config/edit\",
  \"/sys/config/page\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:14:10', '1', '');
INSERT INTO `sys_menu`
VALUES ('1980165871264997378', '10019', 'conversation', '/system/conversation', '/modular/sys/conversation/index.vue', '模型对话', 'icon-park-outline:ad-product', 0, 1, 9, 0, 1, '[
  \"/sys/conversation/edit\",
  \"/sys/conversation/page\",
  \"/sys/conversation/detail\",
  \"/sys/conversation/delete\",
  \"/sys/conversation/add\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:14:24', '1', '');
INSERT INTO `sys_menu`
VALUES ('2', '0', 'dashboard', '/dashboard', '/dashboard/index.vue', '仪表盘', 'icon-park-outline:dashboard', 0, 0, 2, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('3', '0', 'category', '/category', '/modular/sys/category/index.vue', '分类管理', 'icon-park-outline:category-management', 0, 1, 5, 0, 1, '[
  \"/sys/category/delete\",
  \"/sys/category/add\",
  \"/sys/category/detail\",
  \"/sys/category/page\",
  \"/sys/category/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('301', '0', 'problem', '/problem/list', '/modular/data/problem/index.vue', '题目列表', 'icon-park-outline:list', 0, 1, 7, 0, 1, '[
  \"/data/problem/add\",
  \"/data/problem/delete\",
  \"/data/problem/detail\",
  \"/data/problem/page\",
  \"/data/problem/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('302', '0', 'submit', '/submit', '/modular/data/submit/index.vue', '用户提交', 'icon-park-outline:upload', 0, 1, 9, 0, 1, '[
  \"/data/submit/add\",
  \"/data/submit/delete\",
  \"/data/submit/detail\",
  \"/data/submit/page\",
  \"/data/submit/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('303', '0', 'solved', '/solved', '/modular/data/solved/index.vue', '解决记录', 'icon-park-outline:checklist', 0, 1, 10, 0, 1, '[
  \"/data/solved/delete\",
  \"/data/solved/add\",
  \"/data/solved/detail\",
  \"/data/solved/page\",
  \"/data/solved/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('306', '0', 'library', '/data/library', '/modular/data/library/index.vue', '代码样本库', 'icon-park-outline:data-file', 0, 1, 11, 0, 1, '[
  \"/data/library/add\",
  \"/data/library/delete\",
  \"/data/library/detail\",
  \"/data/library/page\",
  \"/data/library/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('4', '0', 'tag', '/tag', '/modular/sys/tag/index.vue', '标签管理', 'icon-park-outline:tag', 0, 1, 6, 0, 1, '[
  \"/sys/tag/add\",
  \"/sys/tag/delete\",
  \"/sys/tag/detail\",
  \"/sys/tag/page\",
  \"/sys/tag/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('401', '0', 'set', '/set/list', '/modular/data/set/index.vue', '题集列表', 'icon-park-outline:list', 0, 1, 8, 0, 1, '[
  \"/data/set/add\",
  \"/data/set/delete\",
  \"/data/set/detail\",
  \"/data/set/page\",
  \"/data/set/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('407', '0', 'similarity', '/task/similarity', '/modular/data/similarity/index.vue', '检测任务详情', 'icon-park-outline:accept-email', 0, 0, 12, 0, 1, '[
  \"/task/similarity/add\",
  \"/task/similarity/detail\",
  \"/task/similarity/delete\",
  \"/task/similarity/page\",
  \"/task/similarity/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:15:28', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('500', '0', 'reports', '/reports', '/modular/data/reports/index.vue', '检测报告', 'icon-park-outline:database-download', 0, 1, 13, 0, 1, '[
  \"/task/reports/add\",
  \"/task/reports/delete\",
  \"/task/reports/detail\",
  \"/task/reports/page\",
  \"/task/reports/edit\"
]', 0, '2025-10-21 12:25:12', '1', '2025-10-25 23:15:42', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('600', '0', 'mine', '/mine', '/modular/mine/profile.vue', '个人中心', 'icon-park-outline:user', 0, 1, 50, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('700', '0', 'about', '/about', '/about.vue', '关于', 'icon-park-outline:info', 0, 1, 99, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);
INSERT INTO `sys_menu`
VALUES ('701', '0', 'report', '/visualization/submit/report/:reportId/task/:taskId', '/modular/visualization/submit/report.vue', '报告可视化', 'icon-park-outline:info', 0, 0, 99, 0, 1, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1', NULL);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `title`       VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '标题',
    `cover`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
    `url`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链接',
    `sort`        TINYINT                                                       NULL DEFAULT 99 COMMENT '排序',
    `content`     TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '内容',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    `is_visible`  TINYINT(1)                                                    NULL DEFAULT 1 COMMENT '是否可见',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '公告表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`               VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `name`             VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `code`             VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '编码',
    `data_scope`       VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '数据范围',
    `description`      VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
    `assign_group_ids` JSON                                                          NULL COMMENT '角色分配用户组',
    `deleted`          TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`      DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`      DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_role_code` (`code` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES ('1', '超级管理员', 'SUPER', 'ALL', '超级管理员', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-25 09:19:05', '1');
INSERT INTO `sys_role`
VALUES ('2', '普通管理员', 'ADMIN', 'ALL', '管理员', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-25 21:31:28', '1');
INSERT INTO `sys_role`
VALUES ('3', '普通用户', 'USER', 'SELF', '普通用户', NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-24 11:30:22', '1');
INSERT INTO `sys_role`
VALUES ('4', '用户组超管', 'SUPER_GROUP', 'SELF_GROUP', '用户组超管', NULL, 0, '2025-10-24 11:36:18', '1', '2025-10-24 11:36:51', '1');
INSERT INTO `sys_role`
VALUES ('5', '用户组管理员', 'GROUP', 'SELF_GROUP_ONLY', '用户组管理员', NULL, 0, '2025-10-24 11:37:33', '1', '2025-10-24 11:37:52', '1');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `role_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID',
    `menu_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色-菜单 关联表(1-N)'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_tag
-- ----------------------------
DROP TABLE IF EXISTS `sys_tag`;
CREATE TABLE `sys_tag`
(
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `name`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '标签表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tag
-- ----------------------------
INSERT INTO `sys_tag`
VALUES ('1961376491493249026', '枚举', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_tag`
VALUES ('1961376543280320514', '暴力', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_tag`
VALUES ('1961376569452777474', '贪心', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_tag`
VALUES ('1961376596086607874', '分治', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_tag`
VALUES ('1961376614860312578', '递归', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `group_id`       VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '用户组',
    `username`       VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '用户名',
    `password`       VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
    `nickname`       VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
    `avatar`         VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
    `background`     VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '背景图片',
    `quote`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '签名',
    `gender`         TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '性别',
    `email`          VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
    `student_number` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '学号',
    `telephone`      VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '电话',
    `login_time`     DATETIME                                                      NULL DEFAULT NULL COMMENT '登录时间',
    `deleted`        TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time`    DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`    VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_username` (`username` ASC) USING BTREE,
    INDEX `idx_telephone` (`telephone` ASC) USING BTREE,
    INDEX `idx_email` (`email` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES ('0', '1982138236630949890', 'default', '$2a$10$CGf0BGdflI3iG6Fl83Vjz.kE.XdhMSu.sstPIeNI.9WFz8FR/lmS.', 'Astro Code',
        NULL,
        NULL, NULL, 0,
        'default@163.com', NULL, NULL, NULL, 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');
INSERT INTO `sys_user`
VALUES ('1', '1982138236630949891', 'superadmin', '$2a$10$hS6E7n8tqGZMX2qOzLyk2.pospgtnLo.8gjXAtfttGruIh9AE2lgG', 'SuperAdmin',
        NULL,
        NULL, NULL, 1,
        'superadmin@163.com', NULL, NULL, '2025-10-26 01:11:44', 0, '2025-10-21 12:25:12', '1',
        '2025-10-26 01:11:44', '1');
INSERT INTO `sys_user`
VALUES ('2', '1982138236630949892', 'adminadmin', '$2a$10$hS6E7n8tqGZMX2qOzLyk2.pospgtnLo.8gjXAtfttGruIh9AE2lgG',
        'Admin', NULL,
        NULL, NULL, 0,
        'adminadmin@163.com', NULL, NULL, '2025-10-07 13:20:00', 0, '2025-10-21 12:25:12', '1',
        '2025-10-22 12:25:12', '1');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `user_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户-角色 关联表(1-N)'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES ('0', '3');
INSERT INTO `sys_user_role`
VALUES ('1', '1');
INSERT INTO `sys_user_role`
VALUES ('2', '2');

-- ----------------------------
-- Table structure for task_reports
-- ----------------------------
DROP TABLE IF EXISTS `task_reports`;
CREATE TABLE `task_reports`
(
    `id`                      VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `report_type`             INT                                                          NULL DEFAULT 0 COMMENT '报告类型',
    `task_id`                 VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务ID',
    `set_id`                  VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`                  TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否是题集提交',
    `problem_id`              VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `sample_count`            INT                                                          NULL DEFAULT 0 COMMENT '样例数量',
    `similarity_group_count`  INT                                                          NULL DEFAULT 0 COMMENT '相似组数量',
    `avg_similarity`          DECIMAL(10, 2) UNSIGNED                                      NULL DEFAULT 0.00 COMMENT '平均相似度',
    `max_similarity`          DECIMAL(10, 2) UNSIGNED                                      NULL DEFAULT 0.00 COMMENT '最大相似度',
    `threshold`               DECIMAL(10, 2) UNSIGNED                                      NULL DEFAULT 0.50 COMMENT '检测阈值',
    `similarity_distribution` JSON                                                         NULL COMMENT '相似度分布',
    `degree_statistics`       JSON                                                         NULL COMMENT '程度统计',
    `check_mode`              INT                                                          NULL DEFAULT 1 COMMENT '检测模式',
    `deleted`                 TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`             DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time`             DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '报告库表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for task_similarity
-- ----------------------------
DROP TABLE IF EXISTS `task_similarity`;
CREATE TABLE `task_similarity`
(
    `id`                 VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `task_id`            VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务ID',
    `task_type`          TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '手动',
    `problem_id`         VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目ID',
    `set_id`             VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题集ID',
    `is_set`             TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '是否是题集提交',
    `language`           VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编程语言',
    `similarity`         DECIMAL(10, 2) UNSIGNED                                      NULL DEFAULT 0.00 COMMENT '相似度',
    `submit_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交用户',
    `submit_code`        TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NULL COMMENT '源代码',
    `submit_code_length` INT                                                          NULL DEFAULT 0 COMMENT '源代码长度',
    `submit_id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交ID',
    `submit_time`        DATETIME                                                     NULL DEFAULT NULL COMMENT '提交时间',
    `submit_token_name`  JSON                                                         NULL COMMENT '提交用户Token名称',
    `submit_token_texts` JSON                                                         NULL COMMENT '提交用户Token内容',
    `origin_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样本用户',
    `origin_code`        TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NULL COMMENT '样本源代码',
    `origin_code_length` INT                                                          NULL DEFAULT 0 COMMENT '样本源代码长度',
    `origin_id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样本提交ID',
    `origin_time`        DATETIME                                                     NULL DEFAULT NULL COMMENT '样本提交时间',
    `origin_token_name`  JSON                                                         NULL COMMENT '样本用户Token名称',
    `origin_token_texts` JSON                                                         NULL COMMENT '样本用户Token内容',
    `deleted`            TINYINT(1)                                                   NULL DEFAULT 0 COMMENT '删除状态',
    `create_time`        DATETIME                                                     NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_time`        DATETIME                                                     NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user`        VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '检测结果任务库'
  ROW_FORMAT = DYNAMIC;

SET
    FOREIGN_KEY_CHECKS = 1;
