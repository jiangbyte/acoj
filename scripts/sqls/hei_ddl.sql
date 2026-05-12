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
-- 资源-权限关联
-- =============================================================================
DROP TABLE IF EXISTS `ral_resource_permission`;
CREATE TABLE `ral_resource_permission`
(
    `id`            varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `resource_id`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '资源ID',
    `permission_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '权限ID',
    `is_deleted`    varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_resource_permission` (`resource_id`, `permission_id`) USING BTREE,
    INDEX `idx_permission_id` (`permission_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '资源-权限关联';

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
-- 权限
-- =============================================================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `code`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限编码（如 sys/user/page）',
    `name`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称',
    `module`      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属模块（如 sys/user）',
    `category`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'BACKEND' COMMENT '权限分类：BACKEND-后端权限，FRONTEND-前端权限',
    `status`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'ENABLED' COMMENT '状态',
    `sort_code`   int                                                           NULL DEFAULT 0 COMMENT '排序',
    `is_deleted`  varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '创建用户',
    `updated_at`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `updated_by`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code` (`code`) USING BTREE,
    INDEX `idx_module` (`module`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '权限'
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
DROP TABLE IF EXISTS `ral_user_role`;
CREATE TABLE `ral_user_role`
(
    `id`                    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `user_id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `role_id`               varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
    `scope`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据范围覆盖：ALL-全部，CUSTOM-自定义，ORG-本组织，ORG_AND_BELOW-本组织及以下，SELF-本人。为空则继承 ral_role_permission 的配置',
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
DROP TABLE IF EXISTS `ral_user_group`;
CREATE TABLE `ral_user_group`
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
DROP TABLE IF EXISTS `ral_user_permission`;
CREATE TABLE `ral_user_permission`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `user_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `permission_id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限ID',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ALL' COMMENT '数据范围：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组',
    `custom_scope_group_ids` text                                                        NULL COMMENT '自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效',
    `custom_scope_org_ids`   text                                                        NULL COMMENT '自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效',
    `is_deleted`             varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`             datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_permission` (`user_id`, `permission_id`) USING BTREE,
    INDEX `idx_permission_id` (`permission_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户-权限直关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 角色-权限关联
-- =============================================================================
DROP TABLE IF EXISTS `ral_role_permission`;
CREATE TABLE `ral_role_permission`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
    `role_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
    `permission_id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限ID',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ALL' COMMENT '数据范围：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组',
    `custom_scope_group_ids` text                                                        NULL COMMENT '自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效',
    `custom_scope_org_ids`   text                                                        NULL COMMENT '自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效',
    `is_deleted`             varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'NO' COMMENT '逻辑删除',
    `created_at`             datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `created_by`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_role_permission` (`role_id`, `permission_id`) USING BTREE,
    INDEX `idx_permission_id` (`permission_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色-权限关联'
  ROW_FORMAT = Dynamic;

-- =============================================================================
-- 角色-资源关联
-- =============================================================================
DROP TABLE IF EXISTS `ral_role_resource`;
CREATE TABLE `ral_role_resource`
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
-- 用户组-角色关联（已废弃——使用 ral_role_permission 的 GROUP / CUSTOM_GROUP scope）
-- =============================================================================
-- DROP TABLE IF EXISTS `ral_group_role`;

-- =============================================================================
-- 组织-角色关联
-- =============================================================================
DROP TABLE IF EXISTS `ral_org_role`;
CREATE TABLE `ral_org_role`
(
    `id`                     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `org_id`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '组织ID',
    `role_id`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色ID',
    `scope`                  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '数据范围覆盖：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组。为空则继承 ral_role_permission 的配置',
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
VALUES ('1', 'SYS_DEFAULT_FILE_ENGINE', 'LOCAL', 'SYS_BASE', '默认文件引擎', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('2', 'SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS', 'D:/hei-file-upload', 'FILE_LOCAL', '本地文件存储路径(Windows)', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('3', 'SYS_FILE_LOCAL_FOLDER_FOR_UNIX', '/data/hei-file-upload', 'FILE_LOCAL', '本地文件存储路径(Unix)', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

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
VALUES ('org_root', 'HEI', 'Hei集团', 'GROUP', NULL, '集团总部', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_tech', 'TECH', '技术部', 'DEPT', 'org_root', '技术研发部门', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_mkt', 'MKT', '市场部', 'DEPT', 'org_root', '市场营销部门', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_fin', 'FIN', '财务部', 'DEPT', 'org_root', '财务管理部门', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_hr', 'HR', '人力资源部', 'DEPT', 'org_root', '人力资源管理部门', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_sales', 'SALES', '销售部', 'DEPT', 'org_root', '销售部门', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('org_ops', 'OPS', '运维部', 'DEPT', 'org_root', '运维管理部门', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 职位 sys_position
-- =============================================================================
INSERT INTO `sys_position` (`id`, `code`, `name`, `category`, `org_id`, `group_id`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('pos_ceo', 'CEO', '总经理', 'MGMT', 'org_root', NULL, '公司总经理', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_cto', 'CTO', '技术总监', 'TECH', 'org_tech', NULL, '技术部门总监', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_dev_leader', 'DEV_LEAD', '开发组长', 'TECH', 'org_tech', NULL, '开发团队组长', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_dev', 'DEV', '开发工程师', 'TECH', 'org_tech', NULL, '软件开发工程师', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_test', 'TEST', '测试工程师', 'TECH', 'org_tech', NULL, '软件测试工程师', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_pm', 'PM', '产品经理', 'MKT', 'org_mkt', NULL, '产品经理', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_mkt_dir', 'MKT_DIR', '市场总监', 'MKT', 'org_mkt', NULL, '市场部总监', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_fin_dir', 'FIN_DIR', '财务总监', 'FIN', 'org_fin', NULL, '财务部总监', 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_hr_dir', 'HR_DIR', '人力资源总监', 'HR', 'org_hr', NULL, '人力资源部总监', 'ENABLED', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_sales', 'SALES', '销售专员', 'SALES', 'org_sales', NULL, '销售专员', 'ENABLED', 10, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('pos_ops', 'OPS', '运维工程师', 'OPS', 'org_ops', NULL, '运维工程师', 'ENABLED', 11, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 用户组 sys_group
-- =============================================================================
INSERT INTO `sys_group` (`id`, `code`, `name`, `category`, `parent_id`, `org_id`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('grp_admin', 'ADMIN_GRP', '管理组', 'ADMIN', NULL, 'org_root', '系统管理组', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('grp_dev', 'DEV_GRP', '研发组', 'TECH', NULL, 'org_tech', '技术研发组', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('grp_test', 'TEST_GRP', '测试组', 'TECH', NULL, 'org_tech', '软件测试组', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('grp_product', 'PROD_GRP', '产品组', 'MKT', NULL, 'org_mkt', '产品设计组', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('grp_market', 'MKT_GRP', '市场组', 'MKT', NULL, 'org_mkt', '市场推广组', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 角色 sys_role
-- =============================================================================
INSERT INTO `sys_role` (`id`, `code`, `name`, `category`, `description`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('role_super_admin', 'super_admin', '超级管理员', 'BACKEND', '系统超级管理员，拥有全部权限', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_admin', 'admin', '系统管理员', 'BACKEND', '系统管理员', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_dev', 'dev', '开发人员', 'BACKEND', '开发人员角色', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_test', 'test', '测试人员', 'BACKEND', '测试人员角色', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_mkt', 'mkt', '市场人员', 'BACKEND', '市场人员角色', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_fin', 'fin', '财务人员', 'BACKEND', '财务人员角色', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('role_hr', 'hr', '人力资源', 'BACKEND', '人力资源角色', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- B 端用户 sys_user
-- 密码说明：admin 密码为 admin123，其余用户密码均为 123456
-- 密码存储为 bcrypt 哈希（非 SM2 密文）
-- =============================================================================
INSERT INTO `sys_user` (`id`, `account`, `password`, `nickname`, `avatar`, `motto`, `gender`, `birthday`, `email`, `github`, `phone`, `org_id`, `position_id`, `status`, `login_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('user_admin', 'admin', '$2b$12$5t3Ey0kGLXaWgmUMYHh8aeh9hOTwpIcKI4M.txQi26Sd3jz4aeEm2', '管理员', NULL, '管理一切', 'MALE', '1990-01-01', 'admin@hei.com', NULL, '13800000001', 'org_root', 'pos_ceo', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_dev1', 'zhangsan', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '张三', NULL, '代码改变世界', 'MALE', '1995-05-15', 'zhangsan@hei.com', 'https://github.com/zhangsan', '13800000002', 'org_tech', 'pos_dev', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_dev2', 'lisi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '李四', NULL, '学无止境', 'MALE', '1993-08-20', 'lisi@hei.com', NULL, '13800000003', 'org_tech', 'pos_dev', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_dev3', 'wangwu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '王五', NULL, '追求卓越', 'MALE', '1994-03-10', 'wangwu@hei.com', NULL, '13800000004', 'org_tech', 'pos_dev_leader', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_test1', 'zhaoliu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '赵六', NULL, '质量第一', 'FEMALE', '1996-11-25', 'zhaoliu@hei.com', NULL, '13800000005', 'org_tech', 'pos_test', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_pm1', 'sunqi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '孙七', NULL, '用户至上', 'MALE', '1991-07-07', 'sunqi@hei.com', NULL, '13800000006', 'org_mkt', 'pos_pm', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_mkt1', 'zhouba', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '周八', NULL, '市场就是战场', 'FEMALE', '1992-02-14', 'zhouba@hei.com', NULL, '13800000007', 'org_mkt', 'pos_mkt_dir', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_fin1', 'wujiu', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '吴九', NULL, '精打细算', 'MALE', '1988-09-09', 'wujiu@hei.com', NULL, '13800000008', 'org_fin', 'pos_fin_dir', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_hr1', 'zhengshi', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '郑十', NULL, '以人为本', 'FEMALE', '1990-12-01', 'zhengshi@hei.com', NULL, '13800000009', 'org_hr', 'pos_hr_dir', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('user_ops1', 'chen十一', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '陈十一', NULL, '稳定压倒一切', 'MALE', '1993-06-18', 'chen11@hei.com', NULL, '13800000010', 'org_ops', 'pos_ops', 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- C 端用户 client_user
-- =============================================================================
INSERT INTO `client_user` (`id`, `account`, `password`, `nickname`, `avatar`, `motto`, `gender`, `birthday`, `email`, `github`, `status`, `login_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('cuser_1', 'test01', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '测试用户01', NULL, 'hello world', 'MALE', '1995-01-01', 'test01@example.com', NULL, 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('cuser_2', 'test02', '$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC', '测试用户02', NULL, '你好世界', 'FEMALE', '1996-02-02', 'test02@example.com', NULL, 'ACTIVE', 0, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 模块 sys_module
-- =============================================================================
INSERT INTO `sys_module` (`id`, `code`, `name`, `category`, `icon`, `color`, `description`, `is_visible`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('mod_sys', 'sys', '系统管理', 'BACKEND_MENU', 'setting', '#1890FF', '系统管理模块', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('mod_content', 'content', '内容管理', 'BACKEND_MENU', 'file-text', '#52C41A', '内容管理模块', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('mod_dev', 'dev', '系统工具', 'BACKEND_MENU', 'tool', '#722ED1', '系统工具模块', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('mod_monitor', 'monitor', '系统监控', 'BACKEND_MENU', 'dashboard', '#FAAD14', '系统监控模块', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('mod_im', 'im', '即时通讯', 'BACKEND_MENU', 'message', '#FF4D4F', '即时通讯模块', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 资源 sys_resource（菜单树）
-- 分类：category - BACKEND_MENU（后台菜单）、BACKEND_BUTTON（后台按钮）
-- 类型：type - DIRECTORY（目录）、MENU（菜单）、BUTTON（按钮）
-- =============================================================================
INSERT INTO `sys_resource` (`id`, `code`, `name`, `category`, `type`, `description`, `parent_id`, `route_path`, `component_path`, `icon`, `is_visible`, `is_cache`, `is_affix`, `is_hidden`, `is_breadcrumb`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES
-- 一级目录
('res_sys_admin', 'sys_admin', '系统管理', 'BACKEND_MENU', 'DIRECTORY', '系统管理目录', NULL, '/sys', NULL, 'setting', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_content', 'content_mgr', '内容管理', 'BACKEND_MENU', 'DIRECTORY', '内容管理目录', NULL, '/content', NULL, 'file-text', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_dev_tools', 'dev_tools', '系统工具', 'BACKEND_MENU', 'DIRECTORY', '系统工具目录', NULL, '/dev', NULL, 'tool', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 系统管理 -> 菜单
('res_sys_user_menu', 'sys_user', '用户管理', 'BACKEND_MENU', 'MENU', '用户管理菜单', 'res_sys_admin', '/sys/user', 'sys/user/index', 'user', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_role_menu', 'sys_role', '角色管理', 'BACKEND_MENU', 'MENU', '角色管理菜单', 'res_sys_admin', '/sys/role', 'sys/role/index', 'team', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_permission_menu', 'sys_permission', '权限管理', 'BACKEND_MENU', 'MENU', '权限管理菜单', 'res_sys_admin', '/sys/permission', 'sys/permission/index', 'safety', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_resource_menu', 'sys_resource_mgr', '资源管理', 'BACKEND_MENU', 'MENU', '资源管理菜单', 'res_sys_admin', '/sys/resource', 'sys/resource/index', 'menu', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_org_menu', 'sys_org', '组织管理', 'BACKEND_MENU', 'MENU', '组织管理菜单', 'res_sys_admin', '/sys/org', 'sys/org/index', 'apartment', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_position_menu', 'sys_position', '职位管理', 'BACKEND_MENU', 'MENU', '职位管理菜单', 'res_sys_admin', '/sys/position', 'sys/position/index', 'idcard', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_group_menu', 'sys_group', '用户组管理', 'BACKEND_MENU', 'MENU', '用户组管理菜单', 'res_sys_admin', '/sys/group', 'sys/group/index', 'group', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_dict_menu', 'sys_dict', '字典管理', 'BACKEND_MENU', 'MENU', '字典管理菜单', 'res_sys_admin', '/sys/dict', 'sys/dict/index', 'book', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_config_menu', 'sys_config', '系统配置', 'BACKEND_MENU', 'MENU', '系统配置菜单', 'res_sys_admin', '/sys/config', 'sys/config/index', 'setting', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_notice_menu', 'sys_notice', '通知管理', 'BACKEND_MENU', 'MENU', '通知管理菜单', 'res_sys_admin', '/sys/notice', 'sys/notice/index', 'notification', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 10, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 内容管理 -> 菜单
('res_sys_banner_menu', 'sys_banner', '轮播图管理', 'BACKEND_MENU', 'MENU', '轮播图管理菜单', 'res_content', '/sys/banner', 'sys/banner/index', 'picture', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('res_sys_file_menu', 'sys_file', '文件管理', 'BACKEND_MENU', 'MENU', '文件管理菜单', 'res_content', '/sys/file', 'sys/file/index', 'file', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 系统工具 -> 菜单
('res_sys_dev_menu', 'sys_dev', '代码生成', 'BACKEND_MENU', 'MENU', '代码生成菜单', 'res_dev_tools', '/sys/dev', 'sys/dev/index', 'code', 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- ==================== 按钮权限 ====================
-- 用户管理按钮
('btn_sys_user_page', 'sys_user_page', '用户查询', 'BACKEND_BUTTON', 'BUTTON', '查询用户列表', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_create', 'sys_user_create', '用户新增', 'BACKEND_BUTTON', 'BUTTON', '新增用户', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_modify', 'sys_user_modify', '用户修改', 'BACKEND_BUTTON', 'BUTTON', '修改用户', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_remove', 'sys_user_remove', '用户删除', 'BACKEND_BUTTON', 'BUTTON', '删除用户', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_detail', 'sys_user_detail', '用户详情', 'BACKEND_BUTTON', 'BUTTON', '查看用户详情', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_export', 'sys_user_export', '用户导出', 'BACKEND_BUTTON', 'BUTTON', '导出用户数据', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_import', 'sys_user_import', '用户导入', 'BACKEND_BUTTON', 'BUTTON', '导入用户数据', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_grant_role', 'sys_user_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给用户分配角色', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_user_grant_group', 'sys_user_grant_group', '分配组', 'BACKEND_BUTTON', 'BUTTON', '给用户分配组', 'res_sys_user_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 角色管理按钮
('btn_sys_role_page', 'sys_role_page', '角色查询', 'BACKEND_BUTTON', 'BUTTON', '查询角色列表', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_create', 'sys_role_create', '角色新增', 'BACKEND_BUTTON', 'BUTTON', '新增角色', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_modify', 'sys_role_modify', '角色修改', 'BACKEND_BUTTON', 'BUTTON', '修改角色', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_remove', 'sys_role_remove', '角色删除', 'BACKEND_BUTTON', 'BUTTON', '删除角色', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_detail', 'sys_role_detail', '角色详情', 'BACKEND_BUTTON', 'BUTTON', '查看角色详情', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_export', 'sys_role_export', '角色导出', 'BACKEND_BUTTON', 'BUTTON', '导出角色数据', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_grant_perm', 'sys_role_grant_perm', '分配权限', 'BACKEND_BUTTON', 'BUTTON', '给角色分配权限', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_role_grant_resource', 'sys_role_grant_resource', '分配资源', 'BACKEND_BUTTON', 'BUTTON', '给角色分配资源', 'res_sys_role_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 权限管理按钮
('btn_sys_permission_page', 'sys_permission_page', '权限查询', 'BACKEND_BUTTON', 'BUTTON', '查询权限列表', 'res_sys_permission_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_permission_create', 'sys_permission_create', '权限新增', 'BACKEND_BUTTON', 'BUTTON', '新增权限', 'res_sys_permission_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_permission_modify', 'sys_permission_modify', '权限修改', 'BACKEND_BUTTON', 'BUTTON', '修改权限', 'res_sys_permission_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_permission_remove', 'sys_permission_remove', '权限删除', 'BACKEND_BUTTON', 'BUTTON', '删除权限', 'res_sys_permission_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_permission_detail', 'sys_permission_detail', '权限详情', 'BACKEND_BUTTON', 'BUTTON', '查看权限详情', 'res_sys_permission_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 资源管理按钮
('btn_sys_resource_page', 'sys_resource_page', '资源查询', 'BACKEND_BUTTON', 'BUTTON', '查询资源列表', 'res_sys_resource_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_resource_create', 'sys_resource_create', '资源新增', 'BACKEND_BUTTON', 'BUTTON', '新增资源', 'res_sys_resource_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_resource_modify', 'sys_resource_modify', '资源修改', 'BACKEND_BUTTON', 'BUTTON', '修改资源', 'res_sys_resource_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_resource_remove', 'sys_resource_remove', '资源删除', 'BACKEND_BUTTON', 'BUTTON', '删除资源', 'res_sys_resource_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_resource_detail', 'sys_resource_detail', '资源详情', 'BACKEND_BUTTON', 'BUTTON', '查看资源详情', 'res_sys_resource_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 组织管理按钮
('btn_sys_org_page', 'sys_org_page', '组织查询', 'BACKEND_BUTTON', 'BUTTON', '查询组织列表', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_org_create', 'sys_org_create', '组织新增', 'BACKEND_BUTTON', 'BUTTON', '新增组织', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_org_modify', 'sys_org_modify', '组织修改', 'BACKEND_BUTTON', 'BUTTON', '修改组织', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_org_remove', 'sys_org_remove', '组织删除', 'BACKEND_BUTTON', 'BUTTON', '删除组织', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_org_detail', 'sys_org_detail', '组织详情', 'BACKEND_BUTTON', 'BUTTON', '查看组织详情', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_org_grant_role', 'sys_org_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给组织分配角色', 'res_sys_org_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 职位管理按钮
('btn_sys_position_page', 'sys_position_page', '职位查询', 'BACKEND_BUTTON', 'BUTTON', '查询职位列表', 'res_sys_position_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_position_create', 'sys_position_create', '职位新增', 'BACKEND_BUTTON', 'BUTTON', '新增职位', 'res_sys_position_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_position_modify', 'sys_position_modify', '职位修改', 'BACKEND_BUTTON', 'BUTTON', '修改职位', 'res_sys_position_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_position_remove', 'sys_position_remove', '职位删除', 'BACKEND_BUTTON', 'BUTTON', '删除职位', 'res_sys_position_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_position_detail', 'sys_position_detail', '职位详情', 'BACKEND_BUTTON', 'BUTTON', '查看职位详情', 'res_sys_position_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 用户组管理按钮
('btn_sys_group_page', 'sys_group_page', '用户组查询', 'BACKEND_BUTTON', 'BUTTON', '查询用户组列表', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_group_create', 'sys_group_create', '用户组新增', 'BACKEND_BUTTON', 'BUTTON', '新增用户组', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_group_modify', 'sys_group_modify', '用户组修改', 'BACKEND_BUTTON', 'BUTTON', '修改用户组', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_group_remove', 'sys_group_remove', '用户组删除', 'BACKEND_BUTTON', 'BUTTON', '删除用户组', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_group_detail', 'sys_group_detail', '用户组详情', 'BACKEND_BUTTON', 'BUTTON', '查看用户组详情', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_group_grant_role', 'sys_group_grant_role', '分配角色', 'BACKEND_BUTTON', 'BUTTON', '给用户组分配角色', 'res_sys_group_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 字典管理按钮
('btn_sys_dict_page', 'sys_dict_page', '字典查询', 'BACKEND_BUTTON', 'BUTTON', '查询字典列表', 'res_sys_dict_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_dict_create', 'sys_dict_create', '字典新增', 'BACKEND_BUTTON', 'BUTTON', '新增字典', 'res_sys_dict_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_dict_modify', 'sys_dict_modify', '字典修改', 'BACKEND_BUTTON', 'BUTTON', '修改字典', 'res_sys_dict_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_dict_remove', 'sys_dict_remove', '字典删除', 'BACKEND_BUTTON', 'BUTTON', '删除字典', 'res_sys_dict_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_dict_detail', 'sys_dict_detail', '字典详情', 'BACKEND_BUTTON', 'BUTTON', '查看字典详情', 'res_sys_dict_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 系统配置按钮
('btn_sys_config_page', 'sys_config_page', '配置查询', 'BACKEND_BUTTON', 'BUTTON', '查询配置列表', 'res_sys_config_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_config_create', 'sys_config_create', '配置新增', 'BACKEND_BUTTON', 'BUTTON', '新增配置', 'res_sys_config_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_config_modify', 'sys_config_modify', '配置修改', 'BACKEND_BUTTON', 'BUTTON', '修改配置', 'res_sys_config_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_config_remove', 'sys_config_remove', '配置删除', 'BACKEND_BUTTON', 'BUTTON', '删除配置', 'res_sys_config_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_config_detail', 'sys_config_detail', '配置详情', 'BACKEND_BUTTON', 'BUTTON', '查看配置详情', 'res_sys_config_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 通知管理按钮
('btn_sys_notice_page', 'sys_notice_page', '通知查询', 'BACKEND_BUTTON', 'BUTTON', '查询通知列表', 'res_sys_notice_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_notice_create', 'sys_notice_create', '通知新增', 'BACKEND_BUTTON', 'BUTTON', '新增通知', 'res_sys_notice_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_notice_modify', 'sys_notice_modify', '通知修改', 'BACKEND_BUTTON', 'BUTTON', '修改通知', 'res_sys_notice_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_notice_remove', 'sys_notice_remove', '通知删除', 'BACKEND_BUTTON', 'BUTTON', '删除通知', 'res_sys_notice_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_notice_detail', 'sys_notice_detail', '通知详情', 'BACKEND_BUTTON', 'BUTTON', '查看通知详情', 'res_sys_notice_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 轮播图管理按钮
('btn_sys_banner_page', 'sys_banner_page', '轮播查询', 'BACKEND_BUTTON', 'BUTTON', '查询轮播图列表', 'res_sys_banner_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_banner_create', 'sys_banner_create', '轮播新增', 'BACKEND_BUTTON', 'BUTTON', '新增轮播图', 'res_sys_banner_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_banner_modify', 'sys_banner_modify', '轮播修改', 'BACKEND_BUTTON', 'BUTTON', '修改轮播图', 'res_sys_banner_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_banner_remove', 'sys_banner_remove', '轮播删除', 'BACKEND_BUTTON', 'BUTTON', '删除轮播图', 'res_sys_banner_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_banner_detail', 'sys_banner_detail', '轮播详情', 'BACKEND_BUTTON', 'BUTTON', '查看轮播图详情', 'res_sys_banner_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 文件管理按钮
('btn_sys_file_upload', 'sys_file_upload', '文件上传', 'BACKEND_BUTTON', 'BUTTON', '上传文件', 'res_sys_file_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_file_download', 'sys_file_download', '文件下载', 'BACKEND_BUTTON', 'BUTTON', '下载文件', 'res_sys_file_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_file_page', 'sys_file_page', '文件查询', 'BACKEND_BUTTON', 'BUTTON', '查询文件列表', 'res_sys_file_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('btn_sys_file_remove', 'sys_file_remove', '文件删除', 'BACKEND_BUTTON', 'BUTTON', '删除文件', 'res_sys_file_menu', NULL, NULL, NULL, 'YES', 'NO', 'NO', 'NO', 'YES', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 权限 sys_permission（与 @HeiCheckPermission 注解一一对应）
-- =============================================================================
INSERT INTO `sys_permission` (`id`, `code`, `name`, `module`, `category`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES
-- 用户管理
('p_sys_user_page', 'sys:user:page', '用户分页查询', 'sys/user', 'BACKEND', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_create', 'sys:user:create', '用户新增', 'sys/user', 'BACKEND', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_modify', 'sys:user:modify', '用户修改', 'sys/user', 'BACKEND', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_remove', 'sys:user:remove', '用户删除', 'sys/user', 'BACKEND', 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_detail', 'sys:user:detail', '用户详情', 'sys/user', 'BACKEND', 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_export', 'sys:user:export', '用户导出', 'sys/user', 'BACKEND', 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_import', 'sys:user:import', '用户导入', 'sys/user', 'BACKEND', 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_grant_role', 'sys:user:grant-role', '用户分配角色', 'sys/user', 'BACKEND', 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_grant_group', 'sys:user:grant-group', '用户分配组', 'sys/user', 'BACKEND', 'ENABLED', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_own_roles', 'sys:user:own-roles', '用户拥有角色', 'sys/user', 'BACKEND', 'ENABLED', 10, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_user_own_groups', 'sys:user:own-groups', '用户拥有组', 'sys/user', 'BACKEND', 'ENABLED', 11, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 角色管理
('p_sys_role_page', 'sys:role:page', '角色分页查询', 'sys/role', 'BACKEND', 'ENABLED', 12, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_create', 'sys:role:create', '角色新增', 'sys/role', 'BACKEND', 'ENABLED', 13, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_modify', 'sys:role:modify', '角色修改', 'sys/role', 'BACKEND', 'ENABLED', 14, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_remove', 'sys:role:remove', '角色删除', 'sys/role', 'BACKEND', 'ENABLED', 15, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_detail', 'sys:role:detail', '角色详情', 'sys/role', 'BACKEND', 'ENABLED', 16, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_export', 'sys:role:export', '角色导出', 'sys/role', 'BACKEND', 'ENABLED', 17, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_grant_perm', 'sys:role:grantPermission', '角色分配权限', 'sys/role', 'BACKEND', 'ENABLED', 18, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_grant_resource', 'sys:role:grantResource', '角色分配资源', 'sys/role', 'BACKEND', 'ENABLED', 19, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_own_perm', 'sys:role:ownPermission', '角色拥有的权限', 'sys/role', 'BACKEND', 'ENABLED', 20, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_role_own_resource', 'sys:role:ownResource', '角色拥有的资源', 'sys/role', 'BACKEND', 'ENABLED', 21, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 权限管理
('p_sys_permission_page', 'sys:permission:page', '权限分页查询', 'sys/permission', 'BACKEND', 'ENABLED', 22, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_create', 'sys:permission:create', '权限新增', 'sys/permission', 'BACKEND', 'ENABLED', 23, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_modify', 'sys:permission:modify', '权限修改', 'sys/permission', 'BACKEND', 'ENABLED', 24, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_remove', 'sys:permission:remove', '权限删除', 'sys/permission', 'BACKEND', 'ENABLED', 25, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_detail', 'sys:permission:detail', '权限详情', 'sys/permission', 'BACKEND', 'ENABLED', 26, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_export', 'sys:permission:export', '权限导出', 'sys/permission', 'BACKEND', 'ENABLED', 27, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_modules', 'sys:permission:modules', '权限模块列表', 'sys/permission', 'BACKEND', 'ENABLED', 28, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_permission_by_module', 'sys:permission:by-module', '按模块查权限', 'sys/permission', 'BACKEND', 'ENABLED', 29, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 资源管理
('p_sys_resource_page', 'sys:resource:page', '资源分页查询', 'sys/resource', 'BACKEND', 'ENABLED', 30, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_tree', 'sys:resource:tree', '资源分页查询', 'sys/resource', 'BACKEND', 'ENABLED', 30, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_create', 'sys:resource:create', '资源新增', 'sys/resource', 'BACKEND', 'ENABLED', 31, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_modify', 'sys:resource:modify', '资源修改', 'sys/resource', 'BACKEND', 'ENABLED', 32, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_remove', 'sys:resource:remove', '资源删除', 'sys/resource', 'BACKEND', 'ENABLED', 33, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_detail', 'sys:resource:detail', '资源详情', 'sys/resource', 'BACKEND', 'ENABLED', 34, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_resource_export', 'sys:resource:export', '资源导出', 'sys/resource', 'BACKEND', 'ENABLED', 35, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 组织管理
('p_sys_org_page', 'sys:org:page', '组织分页查询', 'sys/org', 'BACKEND', 'ENABLED', 36, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_tree', 'sys:org:tree', '组织树查询', 'sys/org', 'BACKEND', 'ENABLED', 36, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_create', 'sys:org:create', '组织新增', 'sys/org', 'BACKEND', 'ENABLED', 37, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_modify', 'sys:org:modify', '组织修改', 'sys/org', 'BACKEND', 'ENABLED', 38, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_remove', 'sys:org:remove', '组织删除', 'sys/org', 'BACKEND', 'ENABLED', 39, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_detail', 'sys:org:detail', '组织详情', 'sys/org', 'BACKEND', 'ENABLED', 40, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_export', 'sys:org:export', '组织导出', 'sys/org', 'BACKEND', 'ENABLED', 41, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_grant_role', 'sys:org:grant-role', '组织分配角色', 'sys/org', 'BACKEND', 'ENABLED', 42, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_org_own_roles', 'sys:org:own-roles', '组织拥有角色', 'sys/org', 'BACKEND', 'ENABLED', 43, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 职位管理
('p_sys_position_page', 'sys:position:page', '职位分页查询', 'sys/position', 'BACKEND', 'ENABLED', 44, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_position_create', 'sys:position:create', '职位新增', 'sys/position', 'BACKEND', 'ENABLED', 45, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_position_modify', 'sys:position:modify', '职位修改', 'sys/position', 'BACKEND', 'ENABLED', 46, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_position_remove', 'sys:position:remove', '职位删除', 'sys/position', 'BACKEND', 'ENABLED', 47, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_position_detail', 'sys:position:detail', '职位详情', 'sys/position', 'BACKEND', 'ENABLED', 48, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_position_export', 'sys:position:export', '职位导出', 'sys/position', 'BACKEND', 'ENABLED', 49, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 用户组管理
('p_sys_group_page', 'sys:group:page', '用户组分页查询', 'sys/group', 'BACKEND', 'ENABLED', 50, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_tree', 'sys:group:tree', '用户组树查询', 'sys/group', 'BACKEND', 'ENABLED', 50, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_create', 'sys:group:create', '用户组新增', 'sys/group', 'BACKEND', 'ENABLED', 51, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_modify', 'sys:group:modify', '用户组修改', 'sys/group', 'BACKEND', 'ENABLED', 52, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_remove', 'sys:group:remove', '用户组删除', 'sys/group', 'BACKEND', 'ENABLED', 53, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_detail', 'sys:group:detail', '用户组详情', 'sys/group', 'BACKEND', 'ENABLED', 54, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_group_export', 'sys:group:export', '用户组导出', 'sys/group', 'BACKEND', 'ENABLED', 55, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- ('p_sys_group_grant_role', 'sys:group:grant-role', '用户组分配角色', 'sys/group', 'BACKEND', 'ENABLED', 56, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- ('p_sys_group_own_roles', 'sys:group:own-roles', '用户组拥有角色', 'sys/group', 'BACKEND', 'ENABLED', 57, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 字典管理
('p_sys_dict_page', 'sys:dict:page', '字典分页查询', 'sys/dict', 'BACKEND', 'ENABLED', 58, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_list', 'sys:dict:list', '字典列表', 'sys/dict', 'BACKEND', 'ENABLED', 59, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_tree', 'sys:dict:tree', '字典树查询', 'sys/dict', 'BACKEND', 'ENABLED', 60, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_create', 'sys:dict:create', '字典新增', 'sys/dict', 'BACKEND', 'ENABLED', 61, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_modify', 'sys:dict:modify', '字典修改', 'sys/dict', 'BACKEND', 'ENABLED', 62, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_remove', 'sys:dict:remove', '字典删除', 'sys/dict', 'BACKEND', 'ENABLED', 63, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_detail', 'sys:dict:detail', '字典详情', 'sys/dict', 'BACKEND', 'ENABLED', 64, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_get_label', 'sys:dict:get-label', '字典标签查询', 'sys/dict', 'BACKEND', 'ENABLED', 65, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_get_children', 'sys:dict:get-children', '字典子项查询', 'sys/dict', 'BACKEND', 'ENABLED', 66, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dict_export', 'sys:dict:export', '字典导出', 'sys/dict', 'BACKEND', 'ENABLED', 67, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 系统配置
('p_sys_config_page', 'sys:config:page', '配置分页查询', 'sys/config', 'BACKEND', 'ENABLED', 68, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_list', 'sys:config:list', '配置列表', 'sys/config', 'BACKEND', 'ENABLED', 69, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_create', 'sys:config:create', '配置新增', 'sys/config', 'BACKEND', 'ENABLED', 70, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_modify', 'sys:config:modify', '配置修改', 'sys/config', 'BACKEND', 'ENABLED', 71, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_remove', 'sys:config:remove', '配置删除', 'sys/config', 'BACKEND', 'ENABLED', 72, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_detail', 'sys:config:detail', '配置详情', 'sys/config', 'BACKEND', 'ENABLED', 73, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_config_edit', 'sys:config:edit', '配置编辑', 'sys/config', 'BACKEND', 'ENABLED', 74, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 通知管理
('p_sys_notice_page', 'sys:notice:page', '通知分页查询', 'sys/notice', 'BACKEND', 'ENABLED', 75, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_notice_create', 'sys:notice:create', '通知新增', 'sys/notice', 'BACKEND', 'ENABLED', 76, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_notice_modify', 'sys:notice:modify', '通知修改', 'sys/notice', 'BACKEND', 'ENABLED', 77, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_notice_remove', 'sys:notice:remove', '通知删除', 'sys/notice', 'BACKEND', 'ENABLED', 78, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_notice_detail', 'sys:notice:detail', '通知详情', 'sys/notice', 'BACKEND', 'ENABLED', 79, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_notice_export', 'sys:notice:export', '通知导出', 'sys/notice', 'BACKEND', 'ENABLED', 80, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 轮播图管理
('p_sys_banner_page', 'sys:banner:page', '轮播分页查询', 'sys/banner', 'BACKEND', 'ENABLED', 81, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_banner_create', 'sys:banner:create', '轮播新增', 'sys/banner', 'BACKEND', 'ENABLED', 82, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_banner_modify', 'sys:banner:modify', '轮播修改', 'sys/banner', 'BACKEND', 'ENABLED', 83, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_banner_remove', 'sys:banner:remove', '轮播删除', 'sys/banner', 'BACKEND', 'ENABLED', 84, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_banner_detail', 'sys:banner:detail', '轮播详情', 'sys/banner', 'BACKEND', 'ENABLED', 85, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_banner_export', 'sys:banner:export', '轮播导出', 'sys/banner', 'BACKEND', 'ENABLED', 86, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 文件管理
('p_sys_file_upload', 'sys:file:upload', '文件上传', 'sys/file', 'BACKEND', 'ENABLED', 87, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_file_download', 'sys:file:download', '文件下载', 'sys/file', 'BACKEND', 'ENABLED', 88, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_file_page', 'sys:file:page', '文件分页查询', 'sys/file', 'BACKEND', 'ENABLED', 89, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_file_detail', 'sys:file:detail', '文件详情', 'sys/file', 'BACKEND', 'ENABLED', 90, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_file_remove', 'sys:file:remove', '文件删除', 'sys/file', 'BACKEND', 'ENABLED', 91, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 代码生成
('p_sys_dev_gen_basic_page', 'sys:dev:gen-basic-page', '生成基础分页', 'sys/dev', 'BACKEND', 'ENABLED', 92, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_basic_create', 'sys:dev:gen-basic-create', '生成基础新增', 'sys/dev', 'BACKEND', 'ENABLED', 93, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_basic_modify', 'sys:dev:gen-basic-modify', '生成基础修改', 'sys/dev', 'BACKEND', 'ENABLED', 94, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_basic_delete', 'sys:dev:gen-basic-delete', '生成基础删除', 'sys/dev', 'BACKEND', 'ENABLED', 95, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_basic_detail', 'sys:dev:gen-basic-detail', '生成基础详情', 'sys/dev', 'BACKEND', 'ENABLED', 96, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_config_list', 'sys:dev:gen-config-list', '生成配置列表', 'sys/dev', 'BACKEND', 'ENABLED', 97, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_config_modify', 'sys:dev:gen-config-modify', '生成配置修改', 'sys/dev', 'BACKEND', 'ENABLED', 98, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_gen_config_detail', 'sys:dev:gen-config-detail', '生成配置详情', 'sys/dev', 'BACKEND', 'ENABLED', 99, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_dev_cfg_mod_batch', 'sys:dev:gen-config-modify-batch', '生成配置批量修改', 'sys/dev', 'BACKEND', 'ENABLED', 100, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_generate', 'sys:dev:gen-basic-exec-gen-pro', '代码生成执行', 'sys/dev', 'BACKEND', 'ENABLED', 101, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_dev_preview', 'sys:dev:gen-basic-preview-gen', '代码生成预览', 'sys/dev', 'BACKEND', 'ENABLED', 102, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),

-- 模块管理
('p_sys_module_page', 'sys:module:page', '模块分页查询', 'sys/module', 'BACKEND', 'ENABLED', 103, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_module_create', 'sys:module:create', '模块新增', 'sys/module', 'BACKEND', 'ENABLED', 104, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_module_modify', 'sys:module:modify', '模块修改', 'sys/module', 'BACKEND', 'ENABLED', 105, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_module_remove', 'sys:module:remove', '模块删除', 'sys/module', 'BACKEND', 'ENABLED', 106, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('p_sys_module_detail', 'sys:module:detail', '模块详情', 'sys/module', 'BACKEND', 'ENABLED', 107, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 字典 sys_dict
-- =============================================================================
INSERT INTO `sys_dict` (`id`, `code`, `label`, `value`, `color`, `category`, `parent_id`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES
-- 性别
('dict_gender', 'gender', '性别', NULL, NULL, 'sys_base', NULL, 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_gender_male', 'gender_male', '男', 'MALE', 'blue', 'sys_base', 'dict_gender', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_gender_female', 'gender_female', '女', 'FEMALE', 'red', 'sys_base', 'dict_gender', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 状态
('dict_status', 'user_status', '用户状态', NULL, NULL, 'sys_base', NULL, 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_status_active', 'user_status_active', '正常', 'ACTIVE', 'green', 'sys_base', 'dict_status', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_status_locked', 'user_status_locked', '锁定', 'LOCKED', 'red', 'sys_base', 'dict_status', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_status_inactive', 'user_status_inactive', '停用', 'INACTIVE', 'orange', 'sys_base', 'dict_status', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 启用/禁用
('dict_enabled', 'enabled_status', '启用状态', NULL, NULL, 'sys_base', NULL, 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_enabled_yes', 'enabled_status_yes', '启用', 'ENABLED', 'green', 'sys_base', 'dict_enabled', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_enabled_no', 'enabled_status_no', '禁用', 'DISABLED', 'red', 'sys_base', 'dict_enabled', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 是否
('dict_yesno', 'yes_no', '是否', NULL, NULL, 'sys_base', NULL, 'ENABLED', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_yesno_yes', 'yes_no_yes', '是', 'YES', 'green', 'sys_base', 'dict_yesno', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_yesno_no', 'yes_no_no', '否', 'NO', 'red', 'sys_base', 'dict_yesno', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 通知级别
('dict_notice_level', 'notice_level', '通知级别', NULL, NULL, 'sys_notice', NULL, 'ENABLED', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_level_normal', 'notice_level_normal', '普通', 'NORMAL', 'blue', 'sys_notice', 'dict_notice_level', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_level_important', 'notice_level_important', '重要', 'IMPORTANT', 'orange', 'sys_notice', 'dict_notice_level', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_level_urgent', 'notice_level_urgent', '紧急', 'URGENT', 'red', 'sys_notice', 'dict_notice_level', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 通知类型
('dict_notice_type', 'notice_type', '通知类型', NULL, NULL, 'sys_notice', NULL, 'ENABLED', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_type_system', 'notice_type_system', '系统通知', 'SYSTEM_NOTICE', 'purple', 'sys_notice', 'dict_notice_type', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_type_business', 'notice_type_business', '业务通知', 'BUSINESS_NOTICE', 'blue', 'sys_notice', 'dict_notice_type', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_notice_type_maintenance', 'notice_type_maintenance', '维护公告', 'MAINTENANCE', 'orange', 'sys_notice', 'dict_notice_type', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 资源分类
('dict_resource_category', 'resource_category', '资源分类', NULL, NULL, 'sys_resource', NULL, 'ENABLED', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_res_cat_menu', 'res_cat_backend_menu', '后台菜单', 'BACKEND_MENU', 'blue', 'sys_resource', 'dict_resource_category', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_res_cat_button', 'res_cat_backend_button', '后台按钮', 'BACKEND_BUTTON', 'green', 'sys_resource', 'dict_resource_category', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 资源类型
('dict_resource_type', 'resource_type', '资源类型', NULL, NULL, 'sys_resource', NULL, 'ENABLED', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_resource_type_directory', 'resource_type_directory', '目录', 'DIRECTORY', 'blue', 'sys_resource', 'dict_resource_type', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_resource_type_menu', 'resource_type_menu', '菜单', 'MENU', 'green', 'sys_resource', 'dict_resource_type', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_resource_type_button', 'resource_type_button', '按钮', 'BUTTON', 'orange', 'sys_resource', 'dict_resource_type', 'ENABLED', 3, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
-- 组织类别
('dict_org_category', 'org_category', '组织类别', NULL, NULL, 'sys_org', NULL, 'ENABLED', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_org_category_group', 'org_category_group', '集团', 'GROUP', 'red', 'sys_org', 'dict_org_category', 'ENABLED', 1, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
('dict_org_category_dept', 'org_category_dept', '部门', 'DEPT', 'blue', 'sys_org', 'dict_org_category', 'ENABLED', 2, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 通知 sys_notice
-- =============================================================================
INSERT INTO `sys_notice` (`id`, `title`, `summary`, `content`, `cover`, `category`, `type`, `level`, `view_count`, `is_top`, `position`, `status`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('notice_1', '系统升级维护通知', '系统将于本周六凌晨2:00-6:00进行升级维护', '<h1>系统升级维护</h1><p>为了提供更好的服务，系统将于本周六凌晨2:00-6:00进行升级维护，期间部分功能可能无法正常使用。</p>', NULL, 'PLATFORM', 'MAINTENANCE', 'IMPORTANT', 0, 'YES', 'TOP', 'ENABLED', 1, 'NO', NOW(), 'user_admin', NOW(), 'user_admin'),
       ('notice_2', '欢迎使用 Hei FastAPI 系统', '欢迎各位同事使用全新开发的后台管理系统', '<p>Hei FastAPI 是一套基于 FastAPI + SQLAlchemy 2.0 的后台管理系统，欢迎大家体验并提供宝贵意见。</p>', NULL, 'PLATFORM', 'SYSTEM_NOTICE', 'NORMAL', 0, 'NO', 'TOP', 'ENABLED', 2, 'NO', NOW(), 'user_admin', NOW(), 'user_admin'),
       ('notice_3', '第三季度工作总结会议通知', '请各部门负责人准备第三季度工作总结报告', '<p>公司将于下周五召开第三季度工作总结会议，请各部门负责人准备相关材料。</p>', NULL, 'COMPANY', 'BUSINESS_NOTICE', 'IMPORTANT', 0, 'NO', NULL, 'ENABLED', 3, 'NO', NOW(), 'user_admin', NOW(), 'user_admin'),
       ('notice_4', '关于启用新系统的通知', '即日起正式启用全新后台管理系统', '<p>经过开发团队的不懈努力，全新后台管理系统已于今日正式上线，旧系统将并行运行一个月后下线。</p>', NULL, 'PLATFORM', 'SYSTEM_NOTICE', 'NORMAL', 0, 'NO', NULL, 'ENABLED', 4, 'NO', NOW(), 'user_admin', NOW(), 'user_admin');

-- =============================================================================
-- 轮播图 sys_banner
-- =============================================================================
INSERT INTO `sys_banner` (`id`, `title`, `image`, `url`, `link_type`, `summary`, `description`, `category`, `type`, `position`, `sort_code`, `view_count`, `click_count`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('banner_1', 'Hei FastAPI 宣传图', 'https://via.placeholder.com/1920x600/1890FF/FFFFFF?text=Hei+FastAPI', 'https://github.com', 'URL', 'Hei FastAPI 框架宣传图', '基于 FastAPI 的企业级后台开发框架', 'INDEX', 'IMAGE', 'INDEX_TOP', 1, 0, 0, 'NO', NOW(), 'user_admin', NOW(), 'user_admin'),
       ('banner_2', '系统使用指南', 'https://via.placeholder.com/1920x600/52C41A/FFFFFF?text=使用指南', NULL, 'URL', '新系统使用指南', '快速上手新系统', 'INDEX', 'IMAGE', 'INDEX_TOP', 2, 0, 0, 'NO', NOW(), 'user_admin', NOW(), 'user_admin'),
       ('banner_3', '开发团队招募', 'https://via.placeholder.com/1920x600/722ED1/FFFFFF?text=加入我们', NULL, 'URL', '诚聘前后端开发工程师', '如果您对技术充满热情，欢迎加入我们', 'INDEX', 'IMAGE', 'INDEX_TOP', 3, 0, 0, 'NO', NOW(), 'user_admin', NOW(), 'user_admin');

-- =============================================================================
-- 系统配置 sys_config（追加）
-- =============================================================================
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `category`, `remark`, `sort_code`, `is_deleted`, `created_at`, `created_by`, `updated_at`, `updated_by`)
VALUES ('4', 'SYS_SNOWFLAKE_WORKER_ID', '1', 'SYS_BASE', 'Snowflake 工作节点ID', 4, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('5', 'SYS_SNOWFLAKE_DATACENTER_ID', '1', 'SYS_BASE', 'Snowflake 数据中心ID', 5, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('6', 'SYS_DEFAULT_PASSWORD', '123456', 'SYS_BASE', '默认密码（新增用户时使用）', 6, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('7', 'SYS_USER_INIT_PASSWORD', '123456', 'SYS_BASE', '用户初始密码', 7, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('8', 'SYS_MAX_LOGIN_RETRIES', '5', 'SYS_SECURITY', '最大登录失败次数', 8, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('9', 'SYS_LOGIN_LOCK_MINUTES', '30', 'SYS_SECURITY', '登录锁定时间（分钟）', 9, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('10', 'SYS_JWT_TOKEN_EXPIRE', '86400', 'SYS_SECURITY', 'JWT Token 过期时间（秒）', 10, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('11', 'SYS_UPLOAD_MAX_SIZE', '10485760', 'SYS_FILE', '文件上传最大大小（字节）', 11, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN'),
       ('12', 'SYS_UPLOAD_ALLOWED_EXTENSIONS', 'jpg,jpeg,png,gif,bmp,doc,docx,xls,xlsx,pdf,txt,zip,rar', 'SYS_FILE', '允许上传的文件后缀', 12, 'NO', NOW(), 'ADMIN', NOW(), 'ADMIN');

-- =============================================================================
-- 关联表
-- =============================================================================

-- 用户-角色关联
INSERT INTO `ral_user_role` (`id`, `user_id`, `role_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
VALUES ('ur_admin_sa', 'user_admin', 'role_super_admin', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_dev1_dev', 'user_dev1', 'role_dev', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_dev2_dev', 'user_dev2', 'role_dev', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_dev3_dev', 'user_dev3', 'role_dev', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_test1_test', 'user_test1', 'role_test', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_pm1_mkt', 'user_pm1', 'role_mkt', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_mkt1_mkt', 'user_mkt1', 'role_mkt', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_fin1_fin', 'user_fin1', 'role_fin', 'ALL', NULL, 'NO', NOW(), 'ADMIN'),
       ('ur_hr1_hr', 'user_hr1', 'role_hr', 'ALL', NULL, 'NO', NOW(), 'ADMIN');

-- 用户-用户组关联
INSERT INTO `ral_user_group` (`id`, `user_id`, `group_id`, `is_deleted`, `created_at`, `created_by`)
VALUES ('ug_admin', 'user_admin', 'grp_admin', 'NO', NOW(), 'ADMIN'),
       ('ug_dev1', 'user_dev1', 'grp_dev', 'NO', NOW(), 'ADMIN'),
       ('ug_dev2', 'user_dev2', 'grp_dev', 'NO', NOW(), 'ADMIN'),
       ('ug_dev3', 'user_dev3', 'grp_dev', 'NO', NOW(), 'ADMIN'),
       ('ug_test1', 'user_test1', 'grp_test', 'NO', NOW(), 'ADMIN'),
       ('ug_pm1', 'user_pm1', 'grp_product', 'NO', NOW(), 'ADMIN'),
       ('ug_mkt1', 'user_mkt1', 'grp_market', 'NO', NOW(), 'ADMIN');

-- 角色-权限关联（为超级管理员分配全部权限）
INSERT INTO `ral_role_permission` (`id`, `role_id`, `permission_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rp_s_', `id`), 'role_super_admin', `id`, 'ALL', NULL, 'NO', NOW(), 'ADMIN'
FROM `sys_permission`
WHERE `is_deleted` = 'NO' AND `status` = 'ENABLED';

-- 角色-权限关联（为管理员分配核心管理权限）
INSERT INTO `ral_role_permission` (`id`, `role_id`, `permission_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rp_a_', p.`id`), 'role_admin', p.`id`, 'ALL', NULL, 'NO', NOW(), 'ADMIN'
FROM `sys_permission` p
WHERE p.`code` IN (
    'sys:user:page', 'sys:user:detail', 'sys:user:create', 'sys:user:modify', 'sys:user:remove',
    'sys:role:page', 'sys:role:detail', 'sys:role:create', 'sys:role:modify', 'sys:role:remove',
    'sys:permission:page', 'sys:permission:detail',
    'sys:org:page', 'sys:org:tree', 'sys:org:detail', 'sys:org:create', 'sys:org:modify', 'sys:org:remove',
    'sys:position:page', 'sys:position:detail',
     'sys:group:page', 'sys:group:tree', 'sys:group:detail', 'sys:group:create', 'sys:group:modify', 'sys:group:remove',
     'sys:resource:page', 'sys:resource:tree', 'sys:resource:detail', 'sys:resource:create', 'sys:resource:modify', 'sys:resource:remove',
    'sys:dict:page', 'sys:dict:list', 'sys:dict:tree', 'sys:dict:create', 'sys:dict:modify', 'sys:dict:remove',
    'sys:config:page', 'sys:config:list', 'sys:config:create', 'sys:config:modify', 'sys:config:remove',
    'sys:notice:page', 'sys:notice:detail',
    'sys:banner:page', 'sys:banner:detail',
    'sys:file:page', 'sys:file:detail', 'sys:file:upload', 'sys:file:download',
    'sys:module:page', 'sys:module:detail'
);

-- 角色-权限关联（为开发人员分配开发相关权限）
INSERT INTO `ral_role_permission` (`id`, `role_id`, `permission_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rp_d_', p.`id`), 'role_dev', p.`id`, 'ALL', NULL, 'NO', NOW(), 'ADMIN'
FROM `sys_permission` p
WHERE p.`code` LIKE 'sys:dev:%'
   OR p.`code` IN ('sys:dict:page', 'sys:dict:list', 'sys:dict:tree', 'sys:config:page', 'sys:config:list');

-- 角色-权限关联（为测试人员分配只读+字典+通知等）
INSERT INTO `ral_role_permission` (`id`, `role_id`, `permission_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rp_t_', p.`id`), 'role_test', p.`id`, 'ALL', NULL, 'NO', NOW(), 'ADMIN'
FROM `sys_permission` p
WHERE p.`code` LIKE '%.page' OR p.`code` LIKE '%.detail' OR p.`code` LIKE '%.list';

-- 角色-资源关联（超级管理员分配所有资源）
INSERT INTO `ral_role_resource` (`id`, `role_id`, `resource_id`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rrs_', r.`id`), 'role_super_admin', r.`id`, 'NO', NOW(), 'ADMIN'
FROM `sys_resource` r
WHERE r.`is_deleted` = 'NO' AND r.`status` = 'ENABLED';

-- 角色-资源关联（管理员分配菜单资源）
INSERT INTO `ral_role_resource` (`id`, `role_id`, `resource_id`, `is_deleted`, `created_at`, `created_by`)
SELECT CONCAT('rra_', r.`id`), 'role_admin', r.`id`, 'NO', NOW(), 'ADMIN'
FROM `sys_resource` r
WHERE (r.`type` IN ('DIRECTORY', 'MENU') OR r.`code` IN (
    'sys_user_page', 'sys_user_create', 'sys_user_modify', 'sys_user_remove', 'sys_user_detail',
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
INSERT INTO `ral_org_role` (`id`, `org_id`, `role_id`, `scope`, `custom_scope_group_ids`, `is_deleted`, `created_at`, `created_by`)
VALUES ('or_tech_dev', 'org_tech', 'role_dev', NULL, NULL, 'NO', NOW(), 'ADMIN'),
       ('or_mkt_mkt', 'org_mkt', 'role_mkt', NULL, NULL, 'NO', NOW(), 'ADMIN'),
       ('or_fin_fin', 'org_fin', 'role_fin', NULL, NULL, 'NO', NOW(), 'ADMIN'),
       ('or_hr_hr', 'org_hr', 'role_hr', NULL, NULL, 'NO', NOW(), 'ADMIN');

SET FOREIGN_KEY_CHECKS = 1;
