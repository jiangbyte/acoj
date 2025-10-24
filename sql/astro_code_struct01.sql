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
  AUTO_INCREMENT = 1119
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
    `id`          VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `name`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
    `code`        VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '编码',
    `data_scope`  VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '数据范围',
    `description` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
    `level`       INT                                                           NULL DEFAULT 5 COMMENT '角色层级',
    `deleted`     TINYINT(1)                                                    NULL DEFAULT 0 COMMENT '删除状态',
    `create_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '创建时间戳',
    `create_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_time` DATETIME                                                      NULL DEFAULT NULL COMMENT '更新时间戳',
    `update_user` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_role_code` (`code` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色表'
  ROW_FORMAT = DYNAMIC;

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

SET FOREIGN_KEY_CHECKS = 1;
