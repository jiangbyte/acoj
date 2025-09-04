SET NAMES 'utf8mb4';

CREATE
    DATABASE IF NOT EXISTS astro_code;

use
    astro_code;

-- ----------------------------
-- 用户组表
-- ----------------------------
DROP TABLE IF EXISTS sys_group;
CREATE TABLE sys_group
(
    id          VARCHAR(32)  NOT NULL COMMENT '用户组',
    parent_id   VARCHAR(32) COMMENT '父级用户组',
    name        VARCHAR(100) NOT NULL COMMENT '名称',
    code        VARCHAR(50)  NOT NULL COMMENT '编码',
    description VARCHAR(255) COMMENT '描述',
    sort        TINYINT     DEFAULT 99 COMMENT '排序',
    admin_id    VARCHAR(32) COMMENT '负责人',
    group_type  TINYINT(1)  DEFAULT 0 COMMENT '系统组',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX idx_group_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户组表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    id             VARCHAR(32)  NOT NULL COMMENT '主键',
    group_id       VARCHAR(32) COMMENT '用户组',
    username       VARCHAR(64)  NOT NULL COMMENT '用户名',
    password       VARCHAR(100) NOT NULL COMMENT '密码',
    nickname       VARCHAR(128) NOT NULL COMMENT '昵称',
    avatar         VARCHAR(255) NULL COMMENT '头像',
    background     VARCHAR(255) NULL COMMENT '背景图片',
    quote          VARCHAR(255) NULL COMMENT '签名',
    gender         TINYINT(1)  DEFAULT 0 COMMENT '性别',
    email          VARCHAR(128) NOT NULL COMMENT '邮箱',
    student_number VARCHAR(20)  NULL COMMENT '学号',
    telephone      VARCHAR(20)  NULL COMMENT '电话',
    -- 登录时间
    login_time     DATETIME    DEFAULT NULL COMMENT '登录时间',
    # ------------------------------------------------
    deleted        TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time    DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user    VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time    DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user    VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_telephone (telephone),
    INDEX idx_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT
        '用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    name        VARCHAR(255) DEFAULT NULL COMMENT '名称',
    code        VARCHAR(50)  DEFAULT NULL COMMENT '编码',
    description VARCHAR(255) COMMENT '描述',
    level       TINYINT      DEFAULT 5 COMMENT '角色层级',
    -- 1顶级角色（超级管理员） 2普通管理员 3用户组管理员以及子组管理员 4用户组管理员 5普通用户
    # ------------------------------------------------
    deleted     TINYINT(1)   DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32)  DEFAULT NULL COMMENT '创建者',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32)  DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX idx_role_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT
        '角色表';

-- ----------------------------
-- 用户-角色 关联表(1-N)
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT
        '用户-角色 关联表(1-N)';

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    title       VARCHAR(64)  NOT NULL COMMENT '标题',
    cover       VARCHAR(255) NULL COMMENT '封面',
    url         VARCHAR(255) NULL COMMENT '链接',
    sort        TINYINT     DEFAULT 99 COMMENT '排序',
    content     TEXT         NULL COMMENT '内容',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '公告表';

-- ----------------------------
-- 横幅表
-- ----------------------------
DROP TABLE IF EXISTS sys_banner;
CREATE TABLE sys_banner
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    title       VARCHAR(255) NOT NULL COMMENT '标题',
    banner      VARCHAR(255) NULL COMMENT '横幅',
    button_text VARCHAR(255) NULL COMMENT '按钮文字',
    to_url      VARCHAR(255) NULL COMMENT '链接',
    sort        TINYINT     DEFAULT 99 COMMENT '排序',
    subtitle    VARCHAR(255) NOT NULL COMMENT '子标题',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '横幅表';


-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict
(
    id          varchar(32)  NOT NULL COMMENT '主键ID',
    -- 字典类型
    dict_type   varchar(64)  NOT NULL COMMENT '字典类型',
    type_label  varchar(64)  NULL COMMENT '类型名称',
    -- 字典数据
    dict_value  varchar(255) NOT NULL COMMENT '字典值',
    dict_label  varchar(255) NOT NULL COMMENT '字典标签',
    sort_order  int         DEFAULT '0' COMMENT '排序',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_code (dict_type, dict_value) COMMENT '类型和编码唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统字典表';


-- 插入性别字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('1', 'sys_gender', '性别', '0', '未知', 0, NOW(), '1'),
       ('2', 'sys_gender', '性别', '1', '男', 1, NOW(), '1'),
       ('3', 'sys_gender', '性别', '2', '女', 2, NOW(), '1');

-- 插入用户状态字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('4', 'user_status', '用户状态', '0', '禁用', 0, NOW(), '1'),
       ('5', 'user_status', '用户状态', '1', '正常', 1, NOW(), '1'),
       ('6', 'user_status', '用户状态', '2', '锁定', 2, NOW(), '1'),
       ('7', 'user_status', '用户状态', '3', '过期', 3, NOW(), '1');

-- 插入菜单类型字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('8', 'menu_type', '菜单类型', '0', '目录', 0, NOW(), '1'),
       ('9', 'menu_type', '菜单类型', '1', '菜单', 1, NOW(), '1'),
       ('10', 'menu_type', '菜单类型', '2', '按钮', 2, NOW(), '1');

-- 插入系统是否字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('11', 'sys_yes_no', '是否', '0', '否', 0, NOW(), '1'),
       ('12', 'sys_yes_no', '是否', '1', '是', 1, NOW(), '1');

-- 插入日志类型字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('13', 'log_type', '日志类型', '1', '登录日志', 1, NOW(), '1'),
       ('14', 'log_type', '日志类型', '2', '操作日志', 2, NOW(), '1'),
       ('15', 'log_type', '日志类型', '3', '异常日志', 3, NOW(), '1');

-- 插入省份地区字典数据(示例)
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('16', 'sys_province', '省份', '11', '北京市', 1, NOW(), '1'),
       ('17', 'sys_province', '省份', '12', '天津市', 2, NOW(), '1'),
       ('18', 'sys_province', '省份', '13', '河北省', 3, NOW(), '1'),
       ('19', 'sys_province', '省份', '14', '山西省', 4, NOW(), '1');

-- 插入文件类型字典数据
INSERT INTO sys_dict (id, dict_type, type_label, dict_value, dict_label, sort_order, create_time, create_user)
VALUES ('20', 'file_type', '文件类型', 'image', '图片', 1, NOW(), '1'),
       ('21', 'file_type', '文件类型', 'video', '视频', 2, NOW(), '1'),
       ('22', 'file_type', '文件类型', 'audio', '音频', 3, NOW(), '1'),
       ('23', 'file_type', '文件类型', 'document', '文档', 4, NOW(), '1'),
       ('24', 'file_type', '文件类型', 'archive', '压缩包', 5, NOW(), '1');

-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    type_name   VARCHAR(100) NOT NULL COMMENT '名称',
    type_code   VARCHAR(100) NOT NULL COMMENT '编码',
    is_system   TINYINT(1)  DEFAULT 0 COMMENT '内置',
    parent_id   VARCHAR(32) COMMENT '父级类型',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX idx_type_code (type_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典类型表';

-- ----------------------------
-- 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data
(
    id          VARCHAR(32) COMMENT '主键',
    type_id     VARCHAR(32)  NOT NULL COMMENT '类型',
    dict_label  VARCHAR(100) NOT NULL COMMENT '标签',
    dict_value  VARCHAR(100) NOT NULL COMMENT '值',
    sort        INT         DEFAULT 0 COMMENT '排序',
    is_default  TINYINT(1)  DEFAULT 0 COMMENT '默认状态',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX idx_dict_value (dict_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典数据表';

-- ----------------------------
-- 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config
(
    id             VARCHAR(32)  NOT NULL COMMENT '主键',
    name           VARCHAR(255) NOT NULL COMMENT '名称',
    code           VARCHAR(255) NOT NULL COMMENT '编码',
    value          VARCHAR(255) NOT NULL COMMENT '值',
    component_type VARCHAR(255) COMMENT '组件类型', -- 默认输入框
    description    VARCHAR(255) COMMENT '描述',
    # ------------------------------------------------
    deleted        TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time    DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user    VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time    DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user    VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT
        '系统配置表';

-- 登录页面背景图片
INSERT INTO sys_config (id, code, name, value, description)
VALUES (1, 'APP_ADMIN_LOGIN_BACKGROUND', '登录页面背景图片', 'https://img.shetu66.com/2023/06/25/1687662757639430.png', '登录页面背景图片');
-- 应用logo
INSERT INTO sys_config (id, code, name, value, description)
VALUES (2, 'APP_LOGO', '应用logo', 'https://cdn.jsdelivr.net/gh/yupi/pku-oj-img/logo.png', '应用logo');
-- 应用名称
INSERT INTO sys_config (id, code, name, value, description)
VALUES (3, 'APP_NAME', '应用名称', 'Astro Code', '应用名称');
-- 管理页面显示名称还是Logo
INSERT INTO sys_config (id, code, name, value, description)
VALUES (4, 'APP_ADMIN_SHOW_APP_NAME', '管理页面显示名称还是Logo', 'true', '管理页面显示名称还是Logo');

-- ----------------------------
-- 系统文章表（关于，协议，帮助等）
-- ----------------------------
CREATE TABLE sys_article
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    title       VARCHAR(255) NOT NULL COMMENT '标题',
    subtitle    VARCHAR(255) NOT NULL COMMENT '子标题',
    cover       VARCHAR(255) NULL COMMENT '封面',
    author      VARCHAR(255) NULL COMMENT '作者',
    summary     VARCHAR(255) NULL COMMENT '摘要',
    sort        TINYINT     DEFAULT 99 COMMENT '排序',
    to_url      VARCHAR(255) NULL COMMENT '链接',
    parent_id   VARCHAR(32) DEFAULT '0' COMMENT '父级',
    type        VARCHAR(32) DEFAULT 0 COMMENT '类型',
    category    VARCHAR(32) DEFAULT 0 COMMENT '分类',
    content     TEXT         NOT NULL COMMENT '内容',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '系统文章表';


-- 系统核心角色
INSERT INTO sys_role(id, name, code, description, level)
VALUES (1, '超级管理员', 'super', '超级管理员', 1),
       (2, '普通管理员', 'admin', '管理员', 2),
       (3, '子组管理员', 'admin_group', '用户组管理员以及子组管理员', 3),
       (4, '用户组管理员', 'group', '用户组管理员', 4),
       (5, '普通用户', 'user', '普通用户', 5);

-- 初始化用户组
INSERT INTO sys_group(id, code, name, description, parent_id, create_time, create_user, update_time, update_user)
VALUES (1, 'SUPER_GROUP', '超级管理员组', '超级管理员组', 0, NOW(), 1, NOW(), 1);
INSERT INTO sys_group(id, code, name, description, parent_id, create_time, create_user, update_time, update_user)
VALUES (2, 'ADMIN_GROUP', '管理员组', '管理员组', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_group(id, code, name, description, parent_id, create_time, create_user, update_time, update_user)
VALUES (0, 'USER_GROUP', '默认用户组', '默认用户组', 2, NOW(), 1, NOW(), 1);

-- 初始化用户数据
INSERT INTO sys_user(id, group_id, username, password, nickname, email, telephone, create_time, create_user, update_time, update_user)
VALUES (1, 0, 'super', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '超级管理员', 'super@example.com', '13800000000', NOW(), 1, NOW(), 1),
       (2, 0, 'admin', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '管理员', 'admin@example.com', '13800000001', NOW(), 1, NOW(), 1);

INSERT INTO sys_user_role(user_id, role_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 2),
       (2, 3),
       (3, 3);
# -- 其他用户统一分配普通用户角色
# INSERT INTO sys_user_role(user_id, role_id)
# SELECT id, 3
# FROM sys_user
# WHERE id NOT IN (1, 2, 3);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (1, '系统状态', 'sys_status', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, is_default, create_time, create_user, update_time, update_user)
VALUES (1, 1, '启用', '1', 1, 1, NOW(), 1, NOW(), 1),
       (2, 1, '禁用', '0', 2, 0, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (2, '性别类型', 'gender_type', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, is_default, create_time, create_user, update_time, update_user)
VALUES (3, 2, '保密', '0', 1, 1, NOW(), 1, NOW(), 1),
       (4, 2, '男', '1', 2, 0, NOW(), 1, NOW(), 1),
       (5, 2, '女', '2', 3, 0, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (3, '菜单类型', 'menu_type', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, create_time, create_user, update_time, update_user)
VALUES (6, 3, '目录', '0', 1, NOW(), 1, NOW(), 1),
       (7, 3, '菜单', '1', 2, NOW(), 1, NOW(), 1),
       (8, 3, '按钮', '2', 3, NOW(), 1, NOW(), 1),
       (9, 3, '外链', '3', 4, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (4, '角色层级', 'role_level', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, create_time, create_user, update_time, update_user)
VALUES (10, 4, '超级管理员', '1', 1, NOW(), 1, NOW(), 1),
       (11, 4, '普通管理员', '2', 2, NOW(), 1, NOW(), 1),
       (12, 4, '用户组管理员以及子组管理员', '3', 3, NOW(), 1, NOW(), 1),
       (13, 4, '用户组管理员', '4', 4, NOW(), 1, NOW(), 1),
       (14, 4, '普通用户', '5', 5, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (5, '置顶状态', 'is_top', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, create_time, create_user, update_time, update_user)
VALUES (15, 5, '不置顶', '0', 1, NOW(), 1, NOW(), 1),
       (16, 5, '置顶', '1', 2, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (7, '配置类型', 'config_type', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, create_time, create_user, update_time, update_user)
VALUES (22, 7, '文本', '1', 1, NOW(), 1, NOW(), 1),
       (23, 7, '富文本', '2', 2, NOW(), 1, NOW(), 1),
       (24, 7, '数值', '3', 3, NOW(), 1, NOW(), 1),
       (25, 7, '布尔值', '4', 4, NOW(), 1, NOW(), 1),
       (26, 7, 'JSON', '5', 5, NOW(), 1, NOW(), 1),
       (27, 7, '图片URL', '6', 6, NOW(), 1, NOW(), 1),
       (28, 7, '文件URL', '7', 7, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (8, '是否系统内置', 'is_system', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_data(id, type_id, dict_label, dict_value, sort, create_time, create_user, update_time, update_user)
VALUES (31, 8, '否', '0', 1, NOW(), 1, NOW(), 1),
       (32, 8, '是', '1', 2, NOW(), 1, NOW(), 1);

INSERT INTO sys_dict_type(id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (78, '测试类型', 'config_type1', 1, NOW(), 1, NOW(), 1);
INSERT INTO sys_dict_type(id, parent_id, type_name, type_code, is_system, create_time, create_user, update_time, update_user)
VALUES (79, 78, '测试类型', 'config_type2', 1, NOW(), 1, NOW(), 1);



-- ----------------------------
-- 分类表
-- ----------------------------
DROP TABLE IF EXISTS sys_category;
CREATE TABLE sys_category
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    name        VARCHAR(255) NOT NULL COMMENT '名称',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '分类表';

INSERT INTO sys_category (id, name)
VALUES (1, '分类1'),
       (2, '分类2'),
       (3, '分类3');

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS sys_tag;
CREATE TABLE sys_tag
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    name        VARCHAR(255) NOT NULL COMMENT '名称',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '标签表';

INSERT INTO sys_tag (id, name)
VALUES (1, '标签1'),
       (2, '标签2'),
       (3, '标签3'),
       (4, '标签4'),
       (5, '标签5'),
       (6, '标签6'),
       (7, '标签7'),
       (8, '标签8');