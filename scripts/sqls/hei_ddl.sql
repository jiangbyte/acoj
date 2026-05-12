-- =============================================================================
-- Hei FastAPI 标准 DDL
-- 合并自 hei_data.sql + migration.sql，可直接用于初始建库
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `hei_data`;

CREATE DATABASE IF NOT EXISTS `hei_data` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `hei_data`;

-- =============================================================================
-- 用户
-- =============================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `account`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '账号',
    `password`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
    `nickname`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '昵称',
    `avatar`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
    `motto`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '座右铭',
    `gender`           varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '性别',
    `birthday`         date                                                          NULL DEFAULT NULL COMMENT '生日',
    `email`            varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '电子邮箱',
    `github`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT 'GitHub',
    `phone`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '手机号',
    `org_id`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '所属组织ID',
    `position_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '所属职位ID',
    `status`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `last_login_at`    datetime                                                      NULL DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '最后登录IP',
    `login_count`      int                                                           NULL DEFAULT 0 COMMENT '登录次数',
    `is_deleted`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`       datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`       datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_account` (`account`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- C端用户
-- =============================================================================
DROP TABLE IF EXISTS `client_user`;
CREATE TABLE `client_user`
(
    `id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `account`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '账号',
    `password`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
    `nickname`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '昵称',
    `avatar`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
    `motto`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '座右铭',
    `gender`           varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '性别',
    `birthday`         date                                                          NULL DEFAULT NULL COMMENT '生日',
    `email`            varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '电子邮箱',
    `github`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT 'GitHub',
    `status`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `last_login_at`    datetime                                                      NULL DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '最后登录IP',
    `login_count`      int                                                           NULL DEFAULT 0 COMMENT '登录次数',
    `is_deleted`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`       datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`       datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_account` (`account`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'C端用户'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 角色
-- =============================================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色编码',
    `name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色名称',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色类别',
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
    `status`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `extra`       text                                                          NULL COMMENT '扩展信息',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 用户组
-- =============================================================================
DROP TABLE IF EXISTS `sys_group`;
CREATE TABLE `sys_group`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '用户组编码',
    `name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '用户组名称',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '用户组类别',
    `parent_id`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '父用户组ID',
    `org_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '所属组织ID',
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户组描述',
    `status`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `extra`       text                                                          NULL COMMENT '扩展信息',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户组'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 组织
-- =============================================================================
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '组织编码',
    `name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '组织名称',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '组织类别',
    `parent_id`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '父组织ID',
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组织描述',
    `status`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `extra`       text                                                          NULL COMMENT '扩展信息',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE,
    INDEX `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '组织'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 职位
-- =============================================================================
DROP TABLE IF EXISTS `sys_position`;
CREATE TABLE `sys_position`
(
    `id`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '职位编码',
    `name`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '职位名称',
    `category`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '职位类别',
    `org_id`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '所属组织ID',
    `group_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '所属用户组ID',
    `description`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位描述',
    `status`        varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`     int                                                           NULL DEFAULT 0 COMMENT '排序',
    `extra`         text                                                          NULL COMMENT '扩展信息',
    `is_deleted`    varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '职位'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 资源
-- =============================================================================
DROP TABLE IF EXISTS `sys_resource`;
CREATE TABLE `sys_resource`
(
    `id`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '资源编码',
    `name`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '资源名称',
    `category`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '资源分类：BACKEND_MENU-后台菜单，FRONTEND_MENU-前台菜单，BACKEND_BUTTON-后台按钮，FRONTEND_BUTTON-前台按钮',
    `type`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '资源类型：DIRECTORY-目录，MENU-菜单，BUTTON-按钮，INTERNAL_LINK-内链，EXTERNAL_LINK-外链',
    `description`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源描述',
    `parent_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '父资源ID',
    `route_path`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
    `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
    `redirect_path`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '重定向路径',
    `icon`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '资源图标',
    `color`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '资源颜色（前台资源使用）',
    `is_visible`     varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'YES' COMMENT '是否可见',
    `is_cache`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '是否缓存',
    `is_affix`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '是否固定',
    `is_hidden`      varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '是否隐藏',
    `is_breadcrumb`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'YES' COMMENT '是否显示面包屑',
    `external_url`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外链地址',
    `extra`          text                                                          NULL COMMENT '扩展信息',
    `status`         varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`      int                                                           NULL DEFAULT 0 COMMENT '排序',
    `is_deleted`     varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`     datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`     datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '资源'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 模块
-- =============================================================================
DROP TABLE IF EXISTS `sys_module`;
CREATE TABLE `sys_module`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '模块编码',
    `name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '模块名称',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '模块类别',
    `icon`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '模块图标',
    `color`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '模块颜色',
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块描述',
    `is_visible`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'YES' COMMENT '是否可见',
    `status`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '模块'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 字典
-- =============================================================================
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`
(
    `id`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '字典编码',
    `label`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典标签',
    `value`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典值',
    `color`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '字典颜色',
    `category`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '字典分类',
    `parent_id`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '父字典ID',
    `status`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`  int                                                           NULL DEFAULT 0 COMMENT '排序',
    `is_deleted` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '字典'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 通知
-- =============================================================================
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`
(
    `id`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `title`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
    `summary`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知摘要',
    `content`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL DEFAULT NULL COMMENT '通知内容',
    `cover`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片',
    `category`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '通知类别',
    `type`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '通知类型',
    `level`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NORMAL' COMMENT '通知级别',
    `view_count` int                                                           NULL DEFAULT 0 COMMENT '浏览次数',
    `is_top`     varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '是否置顶',
    `position`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '通知位置',
    `status`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`  int                                                           NULL DEFAULT 0 COMMENT '排序',
    `is_deleted` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_category_type` (`category`, `type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '通知'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 轮播图
-- =============================================================================
DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `title`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '轮播标题',
    `image`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '轮播图片',
    `url`         varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转地址',
    `link_type`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'URL' COMMENT '链接类型',
    `summary`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轮播摘要',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL DEFAULT NULL COMMENT '轮播描述',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '轮播类别',
    `type`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '轮播类型',
    `position`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '展示位置',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `view_count`  int                                                           NULL DEFAULT 0 COMMENT '浏览次数',
    `click_count` int                                                           NULL DEFAULT 0 COMMENT '点击次数',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '轮播图'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 代码生成基础表
-- =============================================================================
DROP TABLE IF EXISTS `gen_basic`;
CREATE TABLE `gen_basic` (
    `id`                varchar(32)  NOT NULL COMMENT '主键',
    `db_table`          varchar(255) DEFAULT NULL COMMENT '主表名称',
    `db_table_key`      varchar(255) DEFAULT NULL COMMENT '主表主键',
    `module_name`       varchar(255) DEFAULT NULL COMMENT '模块名',
    `table_prefix`      varchar(255) DEFAULT NULL COMMENT '移除表前缀',
    `generate_type`     varchar(255) DEFAULT NULL COMMENT '生成方式',
    `module`            varchar(255) DEFAULT NULL COMMENT '所属模块',
    `menu_pid`          varchar(255) DEFAULT NULL COMMENT '上级目录',
    `class_name`        varchar(255) DEFAULT NULL COMMENT '类名',
    `form_layout`       varchar(255) DEFAULT NULL COMMENT '表单布局',
    `grid_whether`      varchar(255) DEFAULT NULL COMMENT '使用栅格',
    `package_name`      varchar(255) DEFAULT NULL COMMENT '包名',
    `author_name`       varchar(255) DEFAULT NULL COMMENT '作者',
    `gen_type`          varchar(50)  DEFAULT 'TABLE' COMMENT '生成类型（TABLE/TREE/LEFT_TREE_TABLE/MASTER_DETAIL）',
    `tree_parent_field` varchar(200) DEFAULT NULL COMMENT '树父级字段',
    `tree_name_field`   varchar(200) DEFAULT NULL COMMENT '树显示名称字段',
    `sub_db_table`      varchar(200) DEFAULT NULL COMMENT '子表名称',
    `sub_db_table_key`  varchar(200) DEFAULT NULL COMMENT '子表主键',
    `sub_foreign_key`   varchar(200) DEFAULT NULL COMMENT '子表外键',
    `sub_class_name`    varchar(200) DEFAULT NULL COMMENT '子表类名',
    `sub_function_name` varchar(200) DEFAULT NULL COMMENT '子表功能名',
    `sub_bus_name`      varchar(200) DEFAULT NULL COMMENT '子表业务名',
    `sort_code`         int(11)      DEFAULT NULL COMMENT '排序',
    `is_deleted`        varchar(8)   DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`        datetime     DEFAULT NULL COMMENT '创建时间',
    `created_by`        varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updated_at`        datetime     DEFAULT NULL COMMENT '更新时间',
    `updated_by`        varchar(32)  DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成基础';

-- =============================================================================
-- 代码生成配置表
-- =============================================================================
DROP TABLE IF EXISTS `gen_config`;
CREATE TABLE `gen_config` (
    `id`                  varchar(32)  NOT NULL COMMENT '主键',
    `basic_id`            varchar(32)  DEFAULT NULL COMMENT '基础ID',
    `is_table_key`        varchar(255) DEFAULT 'NO' COMMENT '是否主键',
    `field_name`          varchar(255) DEFAULT NULL COMMENT '字段名',
    `field_remark`        varchar(255) DEFAULT NULL COMMENT '字段注释',
    `field_type`          varchar(255) DEFAULT NULL COMMENT '数据库类型',
    `field_language_type` varchar(255) DEFAULT NULL COMMENT '语言数据类型',
    `effect_type`         varchar(255) DEFAULT NULL COMMENT '作用类型（input/textarea/select/radio/checkbox/datepicker/etc）',
    `dict_type_code`      varchar(255) DEFAULT NULL COMMENT '字典编码',
    `whether_table`       varchar(255) DEFAULT 'YES' COMMENT '列表显示',
    `whether_retract`     varchar(255) DEFAULT 'NO' COMMENT '列省略',
    `whether_add_update`  varchar(255) DEFAULT 'YES' COMMENT '是否增改',
    `whether_required`    varchar(255) DEFAULT 'NO' COMMENT '必填',
    `whether_unique`      varchar(255) DEFAULT 'NO' COMMENT '唯一',
    `query_whether`       varchar(255) DEFAULT 'NO' COMMENT '是否查询',
    `query_type`          varchar(255) DEFAULT NULL COMMENT '查询方式',
    `table_type`          varchar(20)  DEFAULT 'MAIN' COMMENT '所属表类型（MAIN/SUB）',
    `sort_code`           int(11)      DEFAULT NULL COMMENT '排序',
    `is_deleted`          varchar(8)   DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`          datetime     DEFAULT NULL COMMENT '创建时间',
    `created_by`          varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updated_at`          datetime     DEFAULT NULL COMMENT '更新时间',
    `updated_by`          varchar(32)  DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`),
    KEY `idx_basic_id` (`basic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成配置';

-- =============================================================================
-- 用户-角色关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_user_role`;
CREATE TABLE `rel_user_role`
(
    `id`                    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `user_id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `role_id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
    `scope`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据范围覆盖：ALL-全部，CUSTOM-自定义，ORG-本组织，ORG_AND_BELOW-本组织及以下，SELF-本人。为空则继承 rel_role_permission 的配置',
    `custom_scope_group_ids` text                                                        NULL COMMENT '自定义数据范围组ID列表(JSON数组)，scope=CUSTOM时生效',
    `is_deleted`            varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`            datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`) USING BTREE,
    INDEX `idx_role_id` (`role_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户-角色关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 用户-用户组关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_user_group`;
CREATE TABLE `rel_user_group`
(
    `id`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `user_id`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `group_id`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户组ID',
    `is_deleted` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at` datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_group` (`user_id`, `group_id`) USING BTREE,
    INDEX `idx_group_id` (`group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户-用户组关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 用户-权限直关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_user_permission`;
CREATE TABLE `rel_user_permission`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `user_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `permission_code`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限编码',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ALL' COMMENT '数据范围：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组',
    `custom_scope_group_ids` text                                                        NULL COMMENT '自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效',
    `custom_scope_org_ids`   text                                                        NULL COMMENT '自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效',
    `is_deleted`             varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`             datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_permission` (`user_id`, `permission_code`) USING BTREE,
    INDEX `idx_permission_code` (`permission_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户-权限直关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 角色-权限关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_role_permission`;
CREATE TABLE `rel_role_permission`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `role_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
    `permission_code`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限编码',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ALL' COMMENT '数据范围：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组',
    `custom_scope_group_ids` text                                                        NULL COMMENT '自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效',
    `custom_scope_org_ids`   text                                                        NULL COMMENT '自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效',
    `is_deleted`             varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`             datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_role_permission` (`role_id`, `permission_code`) USING BTREE,
    INDEX `idx_permission_code` (`permission_code`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色-权限关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 角色-资源关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_role_resource`;
CREATE TABLE `rel_role_resource`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `role_id`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
    `resource_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源ID',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_role_resource` (`role_id`, `resource_id`) USING BTREE,
    INDEX `idx_resource_id` (`resource_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色-资源关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 用户组-角色关联（已废弃——使用 rel_role_permission 的 GROUP / CUSTOM_GROUP scope）
-- =============================================================================
-- DROP TABLE IF EXISTS `rel_group_role`;

-- =============================================================================
-- 组织-角色关联
-- =============================================================================
DROP TABLE IF EXISTS `rel_org_role`;
CREATE TABLE `rel_org_role`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `org_id`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '组织ID',
    `role_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色ID',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '数据范围覆盖：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组。为空则继承 rel_role_permission 的配置',
    `custom_scope_group_ids` text                                                          NULL COMMENT '自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效',
    `custom_scope_org_ids`   text                                                          NULL COMMENT '自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效',
    `is_deleted`             varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`             datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_org_role` (`org_id`, `role_id`) USING BTREE,
    INDEX `idx_role_id` (`role_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '组织-角色关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 系统配置
-- =============================================================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `config_key`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置键',
    `config_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci    NULL COMMENT '配置值',
    `category`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序码',
    `ext_json`    longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL COMMENT '扩展信息',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '修改时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '修改用户',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统配置'
  ROW_FORMAT = Dynamic;

-- 系统配置种子数据
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `category`, `remark`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('1', 'SYS_DEFAULT_FILE_ENGINE', 'LOCAL', 'SYS_BASE', '默认文件引擎', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('2', 'SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS', 'D:/hei-file-upload', 'FILE_LOCAL', '本地文件存储路径(Windows)', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('3', 'SYS_FILE_LOCAL_FOLDER_FOR_UNIX', '/data/hei-file-upload', 'FILE_LOCAL', '本地文件存储路径(Unix)', 3, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 文件
-- =============================================================================
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`
(
    `id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `engine`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '存储引擎',
    `bucket`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '存储桶',
    `file_key`         varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件Key',
    `name`             text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '文件名称',
    `suffix`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '文件后缀',
    `size_kb`          bigint                                                        NULL DEFAULT NULL COMMENT '文件大小kb',
    `size_info`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '文件大小（格式化后）',
    `obj_name`         text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '文件的对象名（唯一名称）',
    `storage_path`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '文件存储路径',
    `download_path`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '文件下载路径',
    `is_download_auth` tinyint(1)                                                    NULL DEFAULT NULL COMMENT '文件下载是否需要授权',
    `thumbnail`        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL COMMENT '图片缩略图',
    `ext_json`         longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL COMMENT '扩展信息',
    `is_deleted`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`       datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`       datetime                                                      NULL DEFAULT NULL COMMENT '修改时间',
    `updated_by`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '修改用户',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '文件'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 种子数据（按依赖顺序插入）
-- 说明：密码字段存储的是 bcrypt 哈希值，非 SM2 密文。
--       SM2 仅用于前端到后端的传输加密，入库时已解密并 bcrypt 哈希。
--       手机号、邮箱等敏感字段在库中存储为明文（项目当前设计如此）。
-- =============================================================================

-- =============================================================================
-- 组织 sys_org
-- =============================================================================
INSERT INTO `sys_org` (`id`, `code`, `name`, `category`, `parent_id`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('10001', 'HEI', 'Hei集团', 'GROUP', NULL, '集团总部', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10002', 'TECH', '技术部', 'DEPT', '10001', '技术研发部门', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10003', 'MKT', '市场部', 'DEPT', '10001', '市场营销部门', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10004', 'FIN', '财务部', 'DEPT', '10001', '财务管理部门', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10005', 'HR', '人力资源部', 'DEPT', '10001', '人力资源管理部门', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10006', 'SALES', '销售部', 'DEPT', '10001', '销售部门', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10007', 'OPS', '运维部', 'DEPT', '10001', '运维管理部门', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 职位 sys_position
-- =============================================================================
INSERT INTO `sys_position` (`id`, `code`, `name`, `category`, `org_id`, `group_id`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('20001', 'CEO', '总经理', 'MGMT', '10001', NULL, '公司总经理', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20002', 'CTO', '技术总监', 'TECH', '10002', NULL, '技术部门总监', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20003', 'DEV_LEAD', '开发组长', 'TECH', '10002', NULL, '开发团队组长', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20004', 'DEV', '开发工程师', 'TECH', '10002', NULL, '软件开发工程师', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20005', 'TEST', '测试工程师', 'TECH', '10002', NULL, '软件测试工程师', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20006', 'PM', '产品经理', 'MKT', '10003', NULL, '产品经理', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20007', 'MKT_DIR', '市场总监', 'MKT', '10003', NULL, '市场部总监', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20008', 'FIN_DIR', '财务总监', 'FIN', '10004', NULL, '财务部总监', 'ENABLED', 8, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20009', 'HR_DIR', '人力资源总监', 'HR', '10005', NULL, '人力资源部总监', 'ENABLED', 9, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20010', 'SALES', '销售专员', 'SALES', '10006', NULL, '销售专员', 'ENABLED', 10, 'NO', NOW(), '50001', NOW(), '50001'),
       ('20011', 'OPS', '运维工程师', 'OPS', '10007', NULL, '运维工程师', 'ENABLED', 11, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 用户组 sys_group
-- =============================================================================
INSERT INTO `sys_group` (`id`, `code`, `name`, `category`, `parent_id`, `org_id`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('30001', 'ADMIN_GRP', '管理组', 'ADMIN', NULL, '10001', '系统管理组', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('30002', 'DEV_GRP', '研发组', 'TECH', NULL, '10002', '技术研发组', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('30003', 'TEST_GRP', '测试组', 'TECH', NULL, '10002', '软件测试组', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('30004', 'PROD_GRP', '产品组', 'MKT', NULL, '10003', '产品设计组', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('30005', 'MKT_GRP', '市场组', 'MKT', NULL, '10003', '市场推广组', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 角色 sys_role
-- =============================================================================
INSERT INTO `sys_role` (`id`, `code`, `name`, `category`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('40001', 'super_admin', '超级管理员', 'BACKEND', '系统超级管理员，拥有全部权限', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40002', 'admin', '系统管理员', 'BACKEND', '系统管理员', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40003', 'dev', '开发人员', 'BACKEND', '开发人员角色', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40004', 'test', '测试人员', 'BACKEND', '测试人员角色', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40005', 'mkt', '市场人员', 'BACKEND', '市场人员角色', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40006', 'fin', '财务人员', 'BACKEND', '财务人员角色', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
       ('40007', 'hr', '人力资源', 'BACKEND', '人力资源角色', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- B 端用户 sys_user
-- 密码说明：admin 密码为 admin123，其余用户密码均为 123456
-- 密码存储为 bcrypt 哈希（非 SM2 密文）
-- =============================================================================
INSERT INTO `sys_user` (`id`, `account`, `password`, `nickname`, `avatar`, `motto`, `gender`, `birthday`, `email`, `github`, `phone`, `org_id`, `position_id`, `status`, `login_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('50001', 'admin', '$2b$12$5t3Ey0kGLXaWgmUMYHh8aeh9hOTwpIcKI4M.txQi26Sd3jz4aeEm2', '管理员', NULL, '管理一切', 'MALE', '1990-01-01', 'admin@hei.com', NULL, '13800000001', '10001', '20001', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50002', 'zhangsan', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '张三', NULL, '代码改变世界', 'MALE', '1995-05-15', 'zhangsan@hei.com', 'https://github.com/zhangsan', '13800000002', '10002', '20004', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50003', 'lisi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '李四', NULL, '学无止境', 'MALE', '1993-08-20', 'lisi@hei.com', NULL, '13800000003', '10002', '20004', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50004', 'wangwu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '王五', NULL, '追求卓越', 'MALE', '1994-03-10', 'wangwu@hei.com', NULL, '13800000004', '10002', '20003', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50005', 'zhaoliu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '赵六', NULL, '质量第一', 'FEMALE', '1996-11-25', 'zhaoliu@hei.com', NULL, '13800000005', '10002', '20005', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50006', 'sunqi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '孙七', NULL, '用户至上', 'MALE', '1991-07-07', 'sunqi@hei.com', NULL, '13800000006', '10003', '20006', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50007', 'zhouba', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '周八', NULL, '市场就是战场', 'FEMALE', '1992-02-14', 'zhouba@hei.com', NULL, '13800000007', '10003', '20007', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50008', 'wujiu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '吴九', NULL, '精打细算', 'MALE', '1988-09-09', 'wujiu@hei.com', NULL, '13800000008', '10004', '20008', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50009', 'zhengshi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '郑十', NULL, '以人为本', 'FEMALE', '1990-12-01', 'zhengshi@hei.com', NULL, '13800000009', '10005', '20009', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('50010', 'chen十一', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '陈十一', NULL, '稳定压倒一切', 'MALE', '1993-06-18', 'chen11@hei.com', NULL, '13800000010', '10007', '20011', 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- C 端用户 client_user
-- =============================================================================
INSERT INTO `client_user` (`id`, `account`, `password`, `nickname`, `avatar`, `motto`, `gender`, `birthday`, `email`, `github`, `status`, `login_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('60001', 'test01', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '测试用户01', NULL, 'hello world', 'MALE', '1995-01-01', 'test01@example.com', NULL, 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('60002', 'test02', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '测试用户02', NULL, '你好世界', 'FEMALE', '1996-02-02', 'test02@example.com', NULL, 'ACTIVE', 0, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 模块 sys_module
-- =============================================================================
INSERT INTO `sys_module` (`id`, `code`, `name`, `category`, `icon`, `color`, `description`, `is_visible`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('70001', 'sys', '系统管理', 'BACKEND_MENU', 'setting', '#1890FF', '系统管理模块', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('70002', 'content', '内容管理', 'BACKEND_MENU', 'file-text', '#52C41A', '内容管理模块', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('70003', 'dev', '系统工具', 'BACKEND_MENU', 'tool', '#722ED1', '系统工具模块', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('70004', 'monitor', '系统监控', 'BACKEND_MENU', 'dashboard', '#FAAD14', '系统监控模块', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('70005', 'im', '即时通讯', 'BACKEND_MENU', 'message', '#FF4D4F', '即时通讯模块', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 资源 sys_resource（菜单树）
-- 分类：category - BACKEND_MENU（后台菜单）、BACKEND_BUTTON（后台按钮）
-- 类型：type - DIRECTORY（目录）、MENU（菜单）、BUTTON（按钮）
-- =============================================================================
INSERT INTO `sys_resource` (`id`, `code`, `name`, `category`, `type`, `description`, `parent_id`, `route_path`, `component_path`, `icon`, `is_visible`, `is_cache`, `is_affix`, `is_hidden`, `is_breadcrumb`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES
-- 一级目录
('80001', 'sys_admin', '系统管理', 'BACKEND_MENU', 'DIRECTORY', '系统管理目录', NULL, '/sys', NULL, 'setting', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80002', 'content_mgr', '内容管理', 'BACKEND_MENU', 'DIRECTORY', '内容管理目录', NULL, '/content', NULL, 'file-text', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80003', 'dev_tools', '系统工具', 'BACKEND_MENU', 'DIRECTORY', '系统工具目录', NULL, '/dev', NULL, 'tool', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),

-- 系统管理 -> 菜单
('80004', 'sys_user', '用户管理', 'BACKEND_MENU', 'MENU', '用户管理菜单', '80001', '/sys/user', 'sys/user/index', 'user', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80005', 'sys_role', '角色管理', 'BACKEND_MENU', 'MENU', '角色管理菜单', '80001', '/sys/role', 'sys/role/index', 'team', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80006', 'sys_resource_mgr', '资源管理', 'BACKEND_MENU', 'MENU', '资源管理菜单', '80001', '/sys/resource', 'sys/resource/index', 'menu', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80007', 'sys_org', '组织管理', 'BACKEND_MENU', 'MENU', '组织管理菜单', '80001', '/sys/org', 'sys/org/index', 'apartment', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('80008', 'sys_position', '职位管理', 'BACKEND_MENU', 'MENU', '职位管理菜单', '80001', '/sys/org/group/position', 'sys/org/components/group/components/position/index', 'idcard', 'NO', 'NO', 'NO', 'YES', 'YES', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
('80009', 'sys_group', '用户组管理', 'BACKEND_MENU', 'MENU', '用户组管理菜单', '80001', '/sys/org/group', 'sys/org/components/group/index', 'group', 'NO', 'NO', 'NO', 'YES', 'YES', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001'),
('80010', 'sys_dict', '字典管理', 'BACKEND_MENU', 'MENU', '字典管理菜单', '80001', '/sys/dict', 'sys/dict/index', 'book', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), '50001', NOW(), '50001'),
('80011', 'sys_config', '系统配置', 'BACKEND_MENU', 'MENU', '系统配置菜单', '80001', '/sys/config', 'sys/config/index', 'setting', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 9, 'NO', NOW(), '50001', NOW(), '50001'),
('80012', 'sys_notice', '通知管理', 'BACKEND_MENU', 'MENU', '通知管理菜单', '80001', '/sys/notice', 'sys/notice/index', 'notification', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 10, 'NO', NOW(), '50001', NOW(), '50001'),

-- 内容管理 -> 菜单
('80013', 'sys_banner', '轮播图管理', 'BACKEND_MENU', 'MENU', '轮播图管理菜单', '80002', '/sys/banner', 'sys/banner/index', 'picture', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80014', 'sys_file', '文件管理', 'BACKEND_MENU', 'MENU', '文件管理菜单', '80002', '/sys/file', 'sys/file/index', 'file', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),

-- 系统工具 -> 菜单
('80015', 'sys_dev', '代码生成', 'BACKEND_MENU', 'MENU', '代码生成菜单', '80003', '/sys/dev', 'sys/dev/index', 'code', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),

-- ==================== 按钮权限 ====================
-- 用户管理按钮
('80016', 'sys_user_page', '用户查询', 'BACKEND_BUTTON', 'BUTTON', '查询用户列表', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80017', 'sys_user_create', '用户新增', 'BACKEND_BUTTON', 'BUTTON', '新增用户', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80018', 'sys_user_modify', '用户修改', 'BACKEND_BUTTON', 'BUTTON', '修改用户', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80019', 'sys_user_remove', '用户删除', 'BACKEND_BUTTON', 'BUTTON', '删除用户', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80020', 'sys_user_detail', '用户详情', 'BACKEND_BUTTON', 'BUTTON', '查看用户详情', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('80021', 'sys_user_export', '用户导出', 'BACKEND_BUTTON', 'BUTTON', '导出用户数据', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
('80022', 'sys_user_import', '用户导入', 'BACKEND_BUTTON', 'BUTTON', '导入用户数据', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001'),
('80023', 'sys_user_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给用户分配角色', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), '50001', NOW(), '50001'),
('80024', 'sys_user_grant_group', '分配组', 'BACKEND_BUTTON', 'BUTTON', '给用户分配组', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 9, 'NO', NOW(), '50001', NOW(), '50001'),
-- 角色管理按钮
('80025', 'sys_role_page', '角色查询', 'BACKEND_BUTTON', 'BUTTON', '查询角色列表', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80026', 'sys_role_create', '角色新增', 'BACKEND_BUTTON', 'BUTTON', '新增角色', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80027', 'sys_role_modify', '角色修改', 'BACKEND_BUTTON', 'BUTTON', '修改角色', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80028', 'sys_role_remove', '角色删除', 'BACKEND_BUTTON', 'BUTTON', '删除角色', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80029', 'sys_role_detail', '角色详情', 'BACKEND_BUTTON', 'BUTTON', '查看角色详情', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('80030', 'sys_role_export', '角色导出', 'BACKEND_BUTTON', 'BUTTON', '导出角色数据', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
('80031', 'sys_role_grant_perm', '分配权限', 'BACKEND_BUTTON', 'BUTTON', '给角色分配权限', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001'),
('80032', 'sys_role_grant_resource', '分配资源', 'BACKEND_BUTTON', 'BUTTON', '给角色分配资源', '80005', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), '50001', NOW(), '50001'),


-- 资源管理按钮
('80033', 'sys_resource_page', '资源查询', 'BACKEND_BUTTON', 'BUTTON', '查询资源列表', '80006', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80034', 'sys_resource_create', '资源新增', 'BACKEND_BUTTON', 'BUTTON', '新增资源', '80006', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80035', 'sys_resource_modify', '资源修改', 'BACKEND_BUTTON', 'BUTTON', '修改资源', '80006', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80036', 'sys_resource_remove', '资源删除', 'BACKEND_BUTTON', 'BUTTON', '删除资源', '80006', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80037', 'sys_resource_detail', '资源详情', 'BACKEND_BUTTON', 'BUTTON', '查看资源详情', '80006', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 组织管理按钮
('80038', 'sys_org_page', '组织查询', 'BACKEND_BUTTON', 'BUTTON', '查询组织列表', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80039', 'sys_org_create', '组织新增', 'BACKEND_BUTTON', 'BUTTON', '新增组织', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80040', 'sys_org_modify', '组织修改', 'BACKEND_BUTTON', 'BUTTON', '修改组织', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80041', 'sys_org_remove', '组织删除', 'BACKEND_BUTTON', 'BUTTON', '删除组织', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80042', 'sys_org_detail', '组织详情', 'BACKEND_BUTTON', 'BUTTON', '查看组织详情', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('80043', 'sys_org_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给组织分配角色', '80007', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),

-- 职位管理按钮
('80044', 'sys_position_page', '职位查询', 'BACKEND_BUTTON', 'BUTTON', '查询职位列表', '80008', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80045', 'sys_position_create', '职位新增', 'BACKEND_BUTTON', 'BUTTON', '新增职位', '80008', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80046', 'sys_position_modify', '职位修改', 'BACKEND_BUTTON', 'BUTTON', '修改职位', '80008', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80047', 'sys_position_remove', '职位删除', 'BACKEND_BUTTON', 'BUTTON', '删除职位', '80008', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80048', 'sys_position_detail', '职位详情', 'BACKEND_BUTTON', 'BUTTON', '查看职位详情', '80008', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 用户组管理按钮
('80049', 'sys_group_page', '用户组查询', 'BACKEND_BUTTON', 'BUTTON', '查询用户组列表', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80050', 'sys_group_create', '用户组新增', 'BACKEND_BUTTON', 'BUTTON', '新增用户组', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80051', 'sys_group_modify', '用户组修改', 'BACKEND_BUTTON', 'BUTTON', '修改用户组', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80052', 'sys_group_remove', '用户组删除', 'BACKEND_BUTTON', 'BUTTON', '删除用户组', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80053', 'sys_group_detail', '用户组详情', 'BACKEND_BUTTON', 'BUTTON', '查看用户组详情', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('80054', 'sys_group_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给用户组分配角色', '80009', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),

-- 字典管理按钮
('80055', 'sys_dict_page', '字典查询', 'BACKEND_BUTTON', 'BUTTON', '查询字典列表', '80010', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80056', 'sys_dict_create', '字典新增', 'BACKEND_BUTTON', 'BUTTON', '新增字典', '80010', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80057', 'sys_dict_modify', '字典修改', 'BACKEND_BUTTON', 'BUTTON', '修改字典', '80010', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80058', 'sys_dict_remove', '字典删除', 'BACKEND_BUTTON', 'BUTTON', '删除字典', '80010', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80059', 'sys_dict_detail', '字典详情', 'BACKEND_BUTTON', 'BUTTON', '查看字典详情', '80010', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 系统配置按钮
('80060', 'sys_config_page', '配置查询', 'BACKEND_BUTTON', 'BUTTON', '查询配置列表', '80011', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80061', 'sys_config_create', '配置新增', 'BACKEND_BUTTON', 'BUTTON', '新增配置', '80011', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80062', 'sys_config_modify', '配置修改', 'BACKEND_BUTTON', 'BUTTON', '修改配置', '80011', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80063', 'sys_config_remove', '配置删除', 'BACKEND_BUTTON', 'BUTTON', '删除配置', '80011', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80064', 'sys_config_detail', '配置详情', 'BACKEND_BUTTON', 'BUTTON', '查看配置详情', '80011', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 通知管理按钮
('80065', 'sys_notice_page', '通知查询', 'BACKEND_BUTTON', 'BUTTON', '查询通知列表', '80012', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80066', 'sys_notice_create', '通知新增', 'BACKEND_BUTTON', 'BUTTON', '新增通知', '80012', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80067', 'sys_notice_modify', '通知修改', 'BACKEND_BUTTON', 'BUTTON', '修改通知', '80012', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80068', 'sys_notice_remove', '通知删除', 'BACKEND_BUTTON', 'BUTTON', '删除通知', '80012', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80069', 'sys_notice_detail', '通知详情', 'BACKEND_BUTTON', 'BUTTON', '查看通知详情', '80012', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 轮播图管理按钮
('80070', 'sys_banner_page', '轮播查询', 'BACKEND_BUTTON', 'BUTTON', '查询轮播图列表', '80013', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80071', 'sys_banner_create', '轮播新增', 'BACKEND_BUTTON', 'BUTTON', '新增轮播图', '80013', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80072', 'sys_banner_modify', '轮播修改', 'BACKEND_BUTTON', 'BUTTON', '修改轮播图', '80013', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80073', 'sys_banner_remove', '轮播删除', 'BACKEND_BUTTON', 'BUTTON', '删除轮播图', '80013', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('80074', 'sys_banner_detail', '轮播详情', 'BACKEND_BUTTON', 'BUTTON', '查看轮播图详情', '80013', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),

-- 文件管理按钮
('80075', 'sys_file_upload', '文件上传', 'BACKEND_BUTTON', 'BUTTON', '上传文件', '80014', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('80076', 'sys_file_download', '文件下载', 'BACKEND_BUTTON', 'BUTTON', '下载文件', '80014', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('80077', 'sys_file_page', '文件查询', 'BACKEND_BUTTON', 'BUTTON', '查询文件列表', '80014', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('80078', 'sys_file_remove', '文件删除', 'BACKEND_BUTTON', 'BUTTON', '删除文件', '80014', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001');

-- 授权权限按钮（extra 关联 permission_code）
INSERT INTO `sys_resource` (`id`, `code`, `name`, `category`, `type`, `description`, `parent_id`, `route_path`, `component_path`, `icon`, `is_visible`, `is_cache`, `is_affix`, `is_hidden`, `is_breadcrumb`, `status`, `sort_code`, `extra`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('80079', 'sys_user_grant_perm', '授权权限', 'BACKEND_BUTTON', 'BUTTON', '给用户授权颗粒度权限', '80004', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 10, '{"permission_code": "sys:user:grant-permission"}', 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 字典 sys_dict
-- =============================================================================
INSERT INTO `sys_dict` (`id`, `code`, `label`, `value`, `color`, `category`, `parent_id`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES
-- 性别
('90001', 'gender', '性别', NULL, NULL, 'sys_base', NULL, 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90002', 'gender_male', '男', 'MALE', 'blue', 'sys_base', '90001', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90003', 'gender_female', '女', 'FEMALE', 'red', 'sys_base', '90001', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
-- 状态
('90004', 'user_status', '用户状态', NULL, NULL, 'sys_base', NULL, 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('90005', 'user_status_active', '正常', 'ACTIVE', 'green', 'sys_base', '90004', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90006', 'user_status_locked', '锁定', 'LOCKED', 'red', 'sys_base', '90004', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('90007', 'user_status_inactive', '停用', 'INACTIVE', 'orange', 'sys_base', '90004', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
-- 启用/禁用
('90008', 'enabled_status', '启用状态', NULL, NULL, 'sys_base', NULL, 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
('90009', 'enabled_status_yes', '启用', 'ENABLED', 'green', 'sys_base', '90008', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90010', 'enabled_status_no', '禁用', 'DISABLED', 'red', 'sys_base', '90008', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
-- 是否
('90011', 'yes_no', '是否', NULL, NULL, 'sys_base', NULL, 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001'),
('90012', 'yes_no_yes', '是', 'YES', 'green', 'sys_base', '90011', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90013', 'yes_no_no', '否', 'NO', 'red', 'sys_base', '90011', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
-- 通知级别
('90014', 'notice_level', '通知级别', NULL, NULL, 'sys_notice', NULL, 'ENABLED', 5, 'NO', NOW(), '50001', NOW(), '50001'),
('90015', 'notice_level_normal', '普通', 'NORMAL', 'blue', 'sys_notice', '90014', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90016', 'notice_level_important', '重要', 'IMPORTANT', 'orange', 'sys_notice', '90014', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('90017', 'notice_level_urgent', '紧急', 'URGENT', 'red', 'sys_notice', '90014', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
-- 通知类型
('90018', 'notice_type', '通知类型', NULL, NULL, 'sys_notice', NULL, 'ENABLED', 6, 'NO', NOW(), '50001', NOW(), '50001'),
('90019', 'notice_type_system', '系统通知', 'SYSTEM_NOTICE', 'purple', 'sys_notice', '90018', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90020', 'notice_type_business', '业务通知', 'BUSINESS_NOTICE', 'blue', 'sys_notice', '90018', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('90021', 'notice_type_maintenance', '维护公告', 'MAINTENANCE', 'orange', 'sys_notice', '90018', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
-- 资源分类
('90022', 'resource_category', '资源分类', NULL, NULL, 'sys_resource', NULL, 'ENABLED', 7, 'NO', NOW(), '50001', NOW(), '50001'),
('90023', 'res_cat_backend_menu', '后台菜单', 'BACKEND_MENU', 'blue', 'sys_resource', '90022', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90024', 'res_cat_backend_button', '后台按钮', 'BACKEND_BUTTON', 'green', 'sys_resource', '90022', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
-- 资源类型
('90025', 'resource_type', '资源类型', NULL, NULL, 'sys_resource', NULL, 'ENABLED', 8, 'NO', NOW(), '50001', NOW(), '50001'),
('90026', 'resource_type_directory', '目录', 'DIRECTORY', 'blue', 'sys_resource', '90025', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90027', 'resource_type_menu', '菜单', 'MENU', 'green', 'sys_resource', '90025', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
('90028', 'resource_type_button', '按钮', 'BUTTON', 'orange', 'sys_resource', '90025', 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
-- 组织类别
('90029', 'org_category', '组织类别', NULL, NULL, 'sys_org', NULL, 'ENABLED', 9, 'NO', NOW(), '50001', NOW(), '50001'),
('90030', 'org_category_group', '集团', 'GROUP', 'red', 'sys_org', '90029', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
('90031', 'org_category_dept', '部门', 'DEPT', 'blue', 'sys_org', '90029', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 通知 sys_notice
-- =============================================================================
INSERT INTO `sys_notice` (`id`, `title`, `summary`, `content`, `cover`, `category`, `type`, `level`, `view_count`, `is_top`, `position`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('100001', '系统升级维护通知', '系统将于本周六凌晨2:00-6:00进行升级维护', '<h1>系统升级维护</h1><p>为了提供更好的服务，系统将于本周六凌晨2:00-6:00进行升级维护，期间部分功能可能无法正常使用。</p>', NULL, 'PLATFORM', 'MAINTENANCE', 'IMPORTANT', 0, 'YES', 'TOP', 'ENABLED', 1, 'NO', NOW(), '50001', NOW(), '50001'),
       ('100002', '欢迎使用 Hei FastAPI 系统', '欢迎各位同事使用全新开发的后台管理系统', '<p>Hei FastAPI 是一套基于 FastAPI + SQLAlchemy 2.0 的后台管理系统，欢迎大家体验并提供宝贵意见。</p>', NULL, 'PLATFORM', 'SYSTEM_NOTICE', 'NORMAL', 0, 'NO', 'TOP', 'ENABLED', 2, 'NO', NOW(), '50001', NOW(), '50001'),
       ('100003', '第三季度工作总结会议通知', '请各部门负责人准备第三季度工作总结报告', '<p>公司将于下周五召开第三季度工作总结会议，请各部门负责人准备相关材料。</p>', NULL, 'COMPANY', 'BUSINESS_NOTICE', 'IMPORTANT', 0, 'NO', NULL, 'ENABLED', 3, 'NO', NOW(), '50001', NOW(), '50001'),
       ('100004', '关于启用新系统的通知', '即日起正式启用全新后台管理系统', '<p>经过开发团队的不懈努力，全新后台管理系统已于今日正式上线，旧系统将并行运行一个月后下线。</p>', NULL, 'PLATFORM', 'SYSTEM_NOTICE', 'NORMAL', 0, 'NO', NULL, 'ENABLED', 4, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 轮播图 sys_banner
-- =============================================================================
INSERT INTO `sys_banner` (`id`, `title`, `image`, `url`, `link_type`, `summary`, `description`, `category`, `type`, `position`, `sort_code`, `view_count`, `click_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('110001', 'Hei FastAPI 宣传图', 'https://via.placeholder.com/1920x600/1890FF/FFFFFF?text=Hei+FastAPI', 'https://github.com', 'URL', 'Hei FastAPI 框架宣传图', '基于 FastAPI 的企业级后台开发框架', 'INDEX', 'IMAGE', 'INDEX_TOP', 1, 0, 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('110002', '系统使用指南', 'https://via.placeholder.com/1920x600/52C41A/FFFFFF?text=使用指南', NULL, 'URL', '新系统使用指南', '快速上手新系统', 'INDEX', 'IMAGE', 'INDEX_TOP', 2, 0, 0, 'NO', NOW(), '50001', NOW(), '50001'),
       ('110003', '开发团队招募', 'https://via.placeholder.com/1920x600/722ED1/FFFFFF?text=加入我们', NULL, 'URL', '诚聘前后端开发工程师', '如果您对技术充满热情，欢迎加入我们', 'INDEX', 'IMAGE', 'INDEX_TOP', 3, 0, 0, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 系统配置 sys_config（追加）
-- =============================================================================
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `category`, `remark`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('4', 'SYS_SNOWFLAKE_WORKER_ID', '1', 'SYS_BASE', 'Snowflake 工作节点ID', 4, 'NO', NOW(), '50001', NOW(), '50001'),
       ('5', 'SYS_SNOWFLAKE_DATACENTER_ID', '1', 'SYS_BASE', 'Snowflake 数据中心ID', 5, 'NO', NOW(), '50001', NOW(), '50001'),
       ('6', 'SYS_DEFAULT_PASSWORD', '123456', 'SYS_BASE', '默认密码（新增用户时使用）', 6, 'NO', NOW(), '50001', NOW(), '50001'),
       ('7', 'SYS_USER_INIT_PASSWORD', '123456', 'SYS_BASE', '用户初始密码', 7, 'NO', NOW(), '50001', NOW(), '50001'),
       ('8', 'SYS_MAX_LOGIN_RETRIES', '5', 'SYS_SECURITY', '最大登录失败次数', 8, 'NO', NOW(), '50001', NOW(), '50001'),
       ('9', 'SYS_LOGIN_LOCK_MINUTES', '30', 'SYS_SECURITY', '登录锁定时间（分钟）', 9, 'NO', NOW(), '50001', NOW(), '50001'),
       ('10', 'SYS_JWT_TOKEN_EXPIRE', '86400', 'SYS_SECURITY', 'JWT Token 过期时间（秒）', 10, 'NO', NOW(), '50001', NOW(), '50001'),
       ('11', 'SYS_UPLOAD_MAX_SIZE', '10485760', 'SYS_FILE', '文件上传最大大小（字节）', 11, 'NO', NOW(), '50001', NOW(), '50001'),
       ('12', 'SYS_UPLOAD_ALLOWED_EXTENSIONS', 'jpg,jpeg,png,gif,bmp,doc,docx,xls,xlsx,pdf,txt,zip,rar', 'SYS_FILE', '允许上传的文件后缀', 12, 'NO', NOW(), '50001', NOW(), '50001');

-- =============================================================================
-- 关联表
-- =============================================================================

-- 用户-角色关联
INSERT INTO `rel_user_role` (`id`, `user_id`, `role_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
VALUES ('120001', '50001', '40001', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120002', '50002', '40003', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120003', '50003', '40003', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120004', '50004', '40003', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120005', '50005', '40004', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120006', '50006', '40005', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120007', '50007', '40005', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120008', '50008', '40006', 'ALL', NULL, 'NO', NOW(), '50001'),
       ('120009', '50009', '40007', 'ALL', NULL, 'NO', NOW(), '50001');

-- 用户-用户组关联
INSERT INTO `rel_user_group` (`id`, `user_id`, `group_id`, `is_deleted`, `created_at`, `created_by`)
VALUES ('130001', '50001', '30001', 'NO', NOW(), '50001'),
       ('130002', '50002', '30002', 'NO', NOW(), '50001'),
       ('130003', '50003', '30002', 'NO', NOW(), '50001'),
       ('130004', '50004', '30002', 'NO', NOW(), '50001'),
       ('130005', '50005', '30003', 'NO', NOW(), '50001'),
       ('130006', '50006', '30004', 'NO', NOW(), '50001'),
       ('130007', '50007', '30005', 'NO', NOW(), '50001');

-- 角色-权限关联（为超级管理员分配全部已知权限）
-- 权限编码列表参照 @HeiCheckPermission 注解自动扫描结果
SET @rid = 1000000000;
INSERT INTO `rel_role_permission` (`id`, `role_id`, `permission_code`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid := @rid + 1, '40001', v.code, 'ALL', NULL, 'NO', NOW(), '50001'
FROM (VALUES
    ROW('sys:user:page'), ROW('sys:user:create'), ROW('sys:user:modify'), ROW('sys:user:remove'), ROW('sys:user:detail'), ROW('sys:user:export'), ROW('sys:user:import'), ROW('sys:user:grant-role'), ROW('sys:user:grant-group'), ROW('sys:user:grant-permission'), ROW('sys:user:own-permission-detail'), ROW('sys:user:own-roles'), ROW('sys:user:own-groups'),
    ROW('sys:role:page'), ROW('sys:role:create'), ROW('sys:role:modify'), ROW('sys:role:remove'), ROW('sys:role:detail'), ROW('sys:role:export'), ROW('sys:role:grantPermission'), ROW('sys:role:grantResource'), ROW('sys:role:ownPermission'), ROW('sys:role:ownResource'),
    ROW('sys:permission:modules'), ROW('sys:permission:by-module'),
    ROW('sys:resource:page'), ROW('sys:resource:tree'), ROW('sys:resource:create'), ROW('sys:resource:modify'), ROW('sys:resource:remove'), ROW('sys:resource:detail'), ROW('sys:resource:export'),
    ROW('sys:org:page'), ROW('sys:org:tree'), ROW('sys:org:create'), ROW('sys:org:modify'), ROW('sys:org:remove'), ROW('sys:org:detail'), ROW('sys:org:export'), ROW('sys:org:grant-role'), ROW('sys:org:own-roles'),
    ROW('sys:position:page'), ROW('sys:position:create'), ROW('sys:position:modify'), ROW('sys:position:remove'), ROW('sys:position:detail'), ROW('sys:position:export'),
    ROW('sys:group:page'), ROW('sys:group:tree'), ROW('sys:group:create'), ROW('sys:group:modify'), ROW('sys:group:remove'), ROW('sys:group:detail'), ROW('sys:group:export'), ROW('sys:group:grant-role'), ROW('sys:group:own-roles'),
    ROW('sys:dict:page'), ROW('sys:dict:list'), ROW('sys:dict:tree'), ROW('sys:dict:create'), ROW('sys:dict:modify'), ROW('sys:dict:remove'), ROW('sys:dict:detail'), ROW('sys:dict:export'),
    ROW('sys:config:page'), ROW('sys:config:list'), ROW('sys:config:create'), ROW('sys:config:modify'), ROW('sys:config:remove'),
    ROW('sys:notice:page'), ROW('sys:notice:create'), ROW('sys:notice:modify'), ROW('sys:notice:remove'), ROW('sys:notice:detail'),
    ROW('sys:banner:page'), ROW('sys:banner:detail'),
    ROW('sys:file:page'), ROW('sys:file:detail'), ROW('sys:file:upload'), ROW('sys:file:download'),
    ROW('sys:module:page'), ROW('sys:module:detail'),
    ROW('sys:dev:page'), ROW('sys:dev:create'), ROW('sys:dev:modify'), ROW('sys:dev:remove'), ROW('sys:dev:detail'),
    ROW('sys:dev:gen:list'), ROW('sys:dev:gen:create'), ROW('sys:dev:gen:edit'), ROW('sys:dev:gen:detail'), ROW('sys:dev:gen:remove'), ROW('sys:dev:gen:import'), ROW('sys:dev:gen:preview')
) AS v(code);

-- 角色-权限关联（为管理员分配核心管理权限）
INSERT INTO `rel_role_permission` (`id`, `role_id`, `permission_code`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid := @rid + 1, '40002', v.code, 'ALL', NULL, 'NO', NOW(), '50001'
FROM (VALUES
    ROW('sys:user:page'), ROW('sys:user:detail'), ROW('sys:user:create'), ROW('sys:user:modify'), ROW('sys:user:remove'), ROW('sys:user:grant-permission'), ROW('sys:user:own-permission-detail'),
    ROW('sys:role:page'), ROW('sys:role:detail'), ROW('sys:role:create'), ROW('sys:role:modify'), ROW('sys:role:remove'),
    ROW('sys:permission:modules'), ROW('sys:permission:by-module'),
    ROW('sys:org:page'), ROW('sys:org:tree'), ROW('sys:org:detail'), ROW('sys:org:create'), ROW('sys:org:modify'), ROW('sys:org:remove'),
    ROW('sys:position:page'), ROW('sys:position:detail'),
    ROW('sys:group:page'), ROW('sys:group:tree'), ROW('sys:group:detail'), ROW('sys:group:create'), ROW('sys:group:modify'), ROW('sys:group:remove'),
    ROW('sys:resource:page'), ROW('sys:resource:tree'), ROW('sys:resource:detail'), ROW('sys:resource:create'), ROW('sys:resource:modify'), ROW('sys:resource:remove'),
    ROW('sys:dict:page'), ROW('sys:dict:list'), ROW('sys:dict:tree'), ROW('sys:dict:create'), ROW('sys:dict:modify'), ROW('sys:dict:remove'),
    ROW('sys:config:page'), ROW('sys:config:list'), ROW('sys:config:create'), ROW('sys:config:modify'), ROW('sys:config:remove'),
    ROW('sys:notice:page'), ROW('sys:notice:detail'),
    ROW('sys:banner:page'), ROW('sys:banner:detail'),
    ROW('sys:file:page'), ROW('sys:file:detail'), ROW('sys:file:upload'), ROW('sys:file:download'),
    ROW('sys:module:page'), ROW('sys:module:detail')
) AS v(code);

-- 角色-权限关联（为开发人员分配开发相关权限）
INSERT INTO `rel_role_permission` (`id`, `role_id`, `permission_code`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid := @rid + 1, '40003', v.code, 'ALL', NULL, 'NO', NOW(), '50001'
FROM (VALUES
    ROW('sys:dev:page'), ROW('sys:dev:create'), ROW('sys:dev:modify'), ROW('sys:dev:remove'), ROW('sys:dev:detail'),
    ROW('sys:dev:gen:list'), ROW('sys:dev:gen:create'), ROW('sys:dev:gen:edit'), ROW('sys:dev:gen:detail'), ROW('sys:dev:gen:remove'), ROW('sys:dev:gen:import'), ROW('sys:dev:gen:preview'),
    ROW('sys:dict:page'), ROW('sys:dict:list'), ROW('sys:dict:tree'), ROW('sys:config:page'), ROW('sys:config:list')
) AS v(code);

-- 角色-权限关联（为测试人员分配只读+字典+通知等）
INSERT INTO `rel_role_permission` (`id`, `role_id`, `permission_code`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid := @rid + 1, '40004', v.code, 'ALL', NULL, 'NO', NOW(), '50001'
FROM (VALUES
    ROW('sys:user:page'), ROW('sys:user:detail'),
    ROW('sys:role:page'), ROW('sys:role:detail'),
    ROW('sys:org:page'), ROW('sys:org:tree'), ROW('sys:org:detail'),
    ROW('sys:resource:page'), ROW('sys:resource:tree'), ROW('sys:resource:detail'),
    ROW('sys:dict:page'), ROW('sys:dict:list'), ROW('sys:dict:tree'),
    ROW('sys:config:page'), ROW('sys:config:list'),
    ROW('sys:notice:page'), ROW('sys:notice:detail'),
    ROW('sys:banner:page'), ROW('sys:banner:detail'),
    ROW('sys:file:page'), ROW('sys:file:detail'),
    ROW('sys:module:page'), ROW('sys:module:detail'),
    ROW('sys:permission:modules'), ROW('sys:permission:by-module')
) AS v(code);

-- 角色-资源关联（超级管理员分配所有资源）
SET @rid2 = 1500000000;
INSERT INTO `rel_role_resource` (`id`, `role_id`, `resource_id`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid2 := @rid2 + 1, '40001', r.`id`, 'NO', NOW(), '50001'
FROM `sys_resource` r
WHERE r.`is_deleted` = 'NO' AND r.`status` = 'ENABLED';

-- 角色-资源关联（管理员分配菜单资源）
SET @rid3 = 1600000000;
INSERT INTO `rel_role_resource` (`id`, `role_id`, `resource_id`, `is_deleted`, `created_at`, `created_by`)
SELECT @rid3 := @rid3 + 1, '40002', r.`id`, 'NO', NOW(), '50001'
FROM `sys_resource` r
WHERE (r.`type` IN ('DIRECTORY', 'MENU') OR r.`code` IN (
    'sys_user_page', 'sys_user_create', 'sys_user_modify', 'sys_user_remove', 'sys_user_detail', 'sys_user_grant_perm',
    'sys_role_page', 'sys_role_detail',
    'sys_org_page', 'sys_org_create', 'sys_org_modify', 'sys_org_remove', 'sys_org_detail',
    'sys_dict_page', 'sys_dict_create', 'sys_dict_modify', 'sys_dict_remove', 'sys_dict_detail',
    'sys_config_page', 'sys_config_detail',
    'sys_notice_page', 'sys_notice_detail',
    'sys_banner_page', 'sys_banner_detail',
    'sys_file_page', 'sys_file_detail', 'sys_file_upload', 'sys_file_download'
)) AND r.`is_deleted` = 'NO' AND r.`status` = 'ENABLED';

-- 用户组-角色关联（已废弃）

-- 组织-角色关联
INSERT INTO `rel_org_role` (`id`, `org_id`, `role_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
VALUES ('140001', '10002', '40003', NULL, NULL, 'NO', NOW(), '50001'),
       ('140002', '10003', '40005', NULL, NULL, 'NO', NOW(), '50001'),
       ('140003', '10004', '40006', NULL, NULL, 'NO', NOW(), '50001'),
       ('140004', '10005', '40007', NULL, NULL, 'NO', NOW(), '50001');

SET FOREIGN_KEY_CHECKS = 1;
