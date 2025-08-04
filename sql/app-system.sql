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
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    group_id    VARCHAR(32) COMMENT '用户组',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    nickname    VARCHAR(128) NOT NULL COMMENT '昵称',
    avatar      VARCHAR(255) NULL COMMENT '头像',
    background  VARCHAR(255) NULL COMMENT '背景图片',
    quote       VARCHAR(255) NULL COMMENT '签名',
    gender      TINYINT(1)  DEFAULT 0 COMMENT '性别',
    email       VARCHAR(128) NOT NULL COMMENT '邮箱',
    telephone   VARCHAR(20)  NULL COMMENT '电话',
    -- 登录时间
    login_time  DATETIME    DEFAULT NULL COMMENT '登录时间',
    # ------------------------------------------------
    deleted     TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32) DEFAULT NULL COMMENT '更新者',
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


-- 初始化用户数据
INSERT INTO sys_user(id, username, password, nickname, email, telephone, create_time, create_user, update_time, update_user)
VALUES (1, 'super', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '超级管理员', 'super@example.com', '13800000000', NOW(), 1, NOW(), 1),
       (2, 'admin', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '管理员', 'admin@example.com', '13800000001', NOW(), 1, NOW(), 1);

# -- 插入异世界风格用户数据
# INSERT INTO sys_user(id, group_id, username, password, nickname, avatar, background, quote, gender, email, telephone, create_time, create_user, update_time, update_user)
# VALUES
# -- 星界法师议会成员
# (3, 5, 'astralion', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '星辰之眼', '/avatars/mage_stars.jpg', '/backgrounds/cosmic.jpg', '群星指引我的道路', 1, 'astralion@astral.academy',
#  '13811110001', NOW(), 1, NOW(), 1),
# (4, 5, 'lunara', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '月影先知', '/avatars/mage_moon.jpg', '/backgrounds/lunar.jpg', '月光揭示万物真理', 0, 'lunara@astral.academy',
#  '13811110002', NOW(), 1, NOW(), 1),
#
# -- 钢铁矮人工程学院师生
# (5, 6, 'ironbeard', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '铁须大师', '/avatars/dwarf_blacksmith.jpg', '/backgrounds/forge.jpg', '最好的武器来自最热的熔炉', 1,
#  'ironbeard@irondwarf.edu', '13822220001', NOW(), 1, NOW(), 1),
# (6, 6, 'gemcutter', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '宝石切割者', '/avatars/dwarf_engineer.jpg', '/backgrounds/crystal.jpg', '精确到毫米的完美', 1,
#  'gemcutter@irondwarf.edu', '13822220002', NOW(), 1, NOW(), 1),
#
# -- 元素使联盟成员
# (7, 7, 'flameheart', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '烈焰之心', '/avatars/fire_mage.jpg', '/backgrounds/volcano.jpg', '火焰净化一切', 1, 'flameheart@element.org',
#  '13833330001', NOW(), 1, NOW(), 1),
# (8, 7, 'tidecaller', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '潮汐召唤者', '/avatars/water_mage.jpg', '/backgrounds/ocean.jpg', '水流永不停息', 0, 'tidecaller@element.org',
#  '13833330002', NOW(), 1, NOW(), 1),
#
# -- 流浪商人联合会成员
# (9, 8, 'silvertongue', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '银舌商人', '/avatars/merchant.jpg', '/backgrounds/caravan.jpg', '利润是最好的魔法', 1, 'silvertongue@nomad.org',
#  '13844440001', NOW(), 1, NOW(), 1),
# (10, 8, 'spicequeen', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '香料女王', '/avatars/merchant_female.jpg', '/backgrounds/bazaar.jpg', '东方最稀有的香料在我手中', 0,
#  'spicequeen@nomad.org', '13844440002', NOW(), 1, NOW(), 1),
#
# -- 精灵歌咏者学院师生
# (11, 9, 'melodewhisper', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '旋律低语者', '/avatars/elf_bard.jpg', '/backgrounds/forest.jpg', '每片树叶都在歌唱', 0,
#  'melodewhisper@elven.edu', '13855550001', NOW(), 1, NOW(), 1),
# (12, 9, 'harpweaver', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '竖琴编织者', '/avatars/elf_musician.jpg', '/backgrounds/concert.jpg', '用琴弦编织梦境', 1,
#  'harpweaver@elven.edu', '13855550002', NOW(), 1, NOW(), 1),
#
# -- 普通冒险者
# (13, 4, 'swordwind', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '剑风', '/avatars/adventurer.jpg', '/backgrounds/tavern.jpg', '我的剑就是我的道路', 1, 'swordwind@tavern.com',
#  '13866660001', NOW(), 1, NOW(), 1),
# (14, 4, 'shadowstep', '$2a$10$TePd3nIT3FJguI2Vp4BdfuqWONoa6xnIyK5QnHak.s3DT/8N9CXSS', '影步', '/avatars/rogue.jpg', '/backgrounds/alley.jpg', '黑暗是我的盟友', 0, 'shadowstep@tavern.com',
#  '13866660002', NOW(), 1, NOW(), 1);
#
#
# INSERT INTO sys_user_role(user_id, role_id)
# VALUES (1, 1),
#        (1, 2),
#        (1, 3),
#        (2, 2),
#        (2, 3),
#        (3, 3);
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

# -- 系统更新公告
# INSERT INTO sys_notice (id, user_id, title, cover, url, sort, is_top, content, status, create_time, create_user, update_time, update_user)
# VALUES (1, 1, 'OJ系统重大更新 v2.0.0', '/covers/system_update.jpg', NULL, 1, 1,
#         '# OJ系统版本2.0.0更新内容
#
#         ## 🚀 新功能
#         - **Type-2检测系统**：新增更严格的代码相似度检测机制，有效识别高级抄袭行为
#         - **AI教练功能增强**：现在支持多编程语言的优化提示
#
#         ## 🛠 改进
#         - 判题引擎性能提升30%
#         - 用户界面全面优化
#         - 题库搜索功能增强
#
#         更新于：2025-07-01',
#         1, NOW(), 1, NOW(), 1),
#
# -- Type-2检测支持公告
#        (2, 1, 'Type-2检测系统上线通知', '/covers/anti_cheat.jpg', '/help/anti-cheat', 2, 0,
#         '# Type-2代码相似度检测系统正式上线
#
#         我们很高兴地宣布，OJ平台现已支持**Type-2级别**的代码相似度检测！
#
#         ## 检测能力
#         - 识别变量重命名、控制流修改等高级抄袭手段
#
#         ## 使用须知
#         1. 所有新提交的代码将自动进行Type-2检测
#         2. 用户可以在提交界面查看检测结果
#         3. 检测结果将作为学术诚信评估的重要参考
#
#         如有疑问，请联系管理员或查看[帮助文档](/help/anti-cheat)',
#         1, NOW(), 1, NOW(), 1),
#
# -- AI教练功能公告
#        (3, 1, 'AI编程教练功能升级', '/covers/ai_coach.jpg', '/ai-coach', 3, 1,
#         '# AI编程教练功能重大升级
#
#         ## 使用方式
#         1. 在代码编辑器中点击"AI助手"按钮
#         2. 提交代码后查看AI分析报告
#         3. 在个人中心查看学习建议
#
#         > 注意：AI建议仅供参考，最终代码责任由用户承担',
#         1, NOW(), 1, NOW(), 1),
#
# -- 维护公告
#        (4, 1, '系统维护通知', '/covers/maintenance.jpg', NULL, 4, 0,
#         '# 计划内系统维护通知
#
#         ⏰ **维护时间**：
#         2025-07-01 02:00 - 05:00 (UTC+8)
#
#         ## 影响范围
#         - 在此期间无法提交代码
#         - 排行榜数据更新延迟
#         - 部分API接口可能不可用
#
#         ## 更新内容
#         - 数据库服务器升级
#         - 负载均衡优化
#         - 安全补丁应用
#
#         我们对造成的不便深表歉意，感谢您的理解与支持！',
#         1, NOW(), 1, NOW(), 1);
#
#
# -- 推荐题单横幅
# INSERT INTO sys_banner (id, title, banner, button_text, to_url, sort, subtitle, status, create_time, create_user, update_time, update_user)
# VALUES (1, '新手入门题单推荐', '/banners/beginner_problems.jpg', '开始挑战', '/problem-lists/beginner', 1, '精选50道适合编程新手的经典题目', 1, NOW(), 1, NOW(), 1),
#        (2, 'Type-2检测系统详解', '/banners/type2_detection.jpg', '了解详情', '/features/type2-detection', 3, '新一代代码相似度分析技术', 1, NOW(), 1, NOW(), 1),
#        (3, 'AI编程教练体验', '/banners/ai_coach_banner.jpg', '立即体验', '/ai-coach', 4, '智能分析你的代码，提供专业建议', 1, NOW(), 1, NOW(), 1);