SET NAMES 'utf8mb4';

CREATE
    DATABASE IF NOT EXISTS astro_code;

use
    astro_code;

-- ----------------------------
-- 提交表
-- ----------------------------
DROP TABLE IF EXISTS pro_submit;
CREATE TABLE pro_submit
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id  VARCHAR(32) NOT NULL COMMENT '题目ID',
    language    VARCHAR(64) NOT NULL COMMENT '编程语言',
    code        TEXT        NOT NULL COMMENT '源代码',

    submit_type TINYINT(1)           DEFAULT 0 COMMENT '执行类型',

    max_time    INT         NOT NULL DEFAULT 0 COMMENT '最大耗时',
    max_memory  INT         NOT NULL DEFAULT 0 COMMENT '最大内存使用',

    message     TEXT                 DEFAULT NULL COMMENT '执行结果消息',
    test_case  JSON                 DEFAULT NULL COMMENT '测试用例结果',
    status      VARCHAR(32)          DEFAULT NULL COMMENT '执行状态',

    -- 查重结果
    similarity  DECIMAL(10, 2)       DEFAULT 0 COMMENT '相似度',
    task_id     VARCHAR(32)          DEFAULT NULL COMMENT '相似检测任务ID',
    # ------------------------------------------------
    deleted     TINYINT(1)           DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME             DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32)          DEFAULT NULL COMMENT '创建者',
    update_time DATETIME             DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32)          DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id, user_id),
    INDEX (task_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '提交表';

-- ----------------------------
-- 相似度检测任务表
-- ----------------------------
DROP TABLE IF EXISTS pro_similarity_task;
CREATE TABLE pro_similarity_task
(
    id             VARCHAR(32) NOT NULL COMMENT '主键',
    user_id        VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID',

    status         VARCHAR(32)    DEFAULT '' COMMENT '任务状态',
    compare_range  VARCHAR(32)    DEFAULT 'RECENT' COMMENT '比较范围', -- RECENT:近期,ALL:全部
    days_before    INT            DEFAULT 30 COMMENT '近期天数',

    total_compared INT            DEFAULT 0 COMMENT '比较提交数',
    max_similarity DECIMAL(10, 2) DEFAULT 0 COMMENT '最大相似度',

    is_manual      TINYINT(1)     DEFAULT 0 COMMENT '手动任务',
    # ------------------------------------------------
    deleted        TINYINT(1)     DEFAULT 0 COMMENT '删除状态',
    create_time    DATETIME       DEFAULT NULL COMMENT '创建时间戳',
    create_user    VARCHAR(32)    DEFAULT NULL COMMENT '创建者',
    update_time    DATETIME       DEFAULT NULL COMMENT '更新时间戳',
    update_user    VARCHAR(32)    DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id),
    INDEX (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '代码相似度检测任务表';

-- ----------------------------
-- 相似度检测结果表(存储每次检测的详细结果)
-- ----------------------------
DROP TABLE IF EXISTS pro_similarity_result;
CREATE TABLE pro_similarity_result
(
    id                 VARCHAR(32)    NOT NULL COMMENT '主键',
    task_id            VARCHAR(32)    NOT NULL COMMENT '关联的任务ID',

    origin_submit_id   VARCHAR(32)    NOT NULL COMMENT '提交ID',
    compared_submit_id VARCHAR(32)    NOT NULL COMMENT '被比较的提交ID',

    similarity         DECIMAL(10, 2) NOT NULL COMMENT '相似度值',
    details            JSON COMMENT '详细比对结果',
    match_details      TEXT COMMENT '匹配部分详情',
    threshold          DECIMAL(10, 2) NOT NULL COMMENT '相似度阈值',
    # ------------------------------------------------
    deleted            TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time        DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user        VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time        DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user        VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '代码相似度检测结果详情表';

-- ----------------------------
-- oj题目表
-- ----------------------------
DROP TABLE IF EXISTS pro_problem;
CREATE TABLE pro_problem
(
    id                VARCHAR(32)  NOT NULL COMMENT '主键',
    category_id       VARCHAR(32) DEFAULT 0 COMMENT '分类',

    title             VARCHAR(255) NOT NULL COMMENT '标题',
    source            VARCHAR(255) COMMENT '来源',
    url               VARCHAR(255) COMMENT '链接',
    max_time          INT         DEFAULT 0 COMMENT '时间限制',
    max_memory        INT         DEFAULT 0 COMMENT '内存限制',

    description       TEXT COMMENT '描述',

    test_case         JSON COMMENT '用例',
    allowed_languages JSON COMMENT '开放语言',

    difficulty        INT         DEFAULT 1 COMMENT '难度',

    use_template      TINYINT(1)  DEFAULT 0 COMMENT '使用模板',
    code_template     JSON COMMENT '模板代码',

    -- 统计
    solved            BIGINT      DEFAULT 0 COMMENT '解决',
    # ------------------------------------------------
    deleted           TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time       DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user       VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time       DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user       VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题目表';

-- ----------------------------
-- 分类表
-- ----------------------------
DROP TABLE IF EXISTS pro_category;
CREATE TABLE pro_category
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

INSERT INTO pro_category (id, name)
VALUES (1, '分类1'),
       (2, '分类2'),
       (3, '分类3');

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS pro_tag;
CREATE TABLE pro_tag
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

INSERT INTO pro_tag (id, name)
VALUES (1, '标签1'),
       (2, '标签2'),
       (3, '标签3'),
       (4, '标签4'),
       (5, '标签5'),
       (6, '标签6'),
       (7, '标签7'),
       (8, '标签8');

-- ----------------------------
-- 题目标签关联表 1-N
-- ----------------------------
DROP TABLE IF EXISTS pro_problem_tag;
CREATE TABLE pro_problem_tag
(
    problem_id VARCHAR(32) NOT NULL COMMENT '题目ID',
    tag_id     VARCHAR(32) NOT NULL COMMENT '标签ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题目标签关联表';

-- ----------------------------
-- 用户解决记录表
-- ----------------------------
DROP TABLE IF EXISTS pro_solved;
CREATE TABLE pro_solved
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id  VARCHAR(32) NOT NULL COMMENT '题目ID',
    submit_id   VARCHAR(32) NOT NULL COMMENT '提交ID',
    solved      TINYINT(1)  DEFAULT 0 COMMENT '是否解决',
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
    COMMENT '用户解决表';

-- ----------------------------
-- 题集
-- ----------------------------
DROP TABLE IF EXISTS pro_set;
CREATE TABLE pro_set
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    set_type    INT          NOT NULL DEFAULT 1 COMMENT '题集类型',
    -- 1:常规题集, 2:限时题集, 3:自定义题集
    title       VARCHAR(255) NOT NULL COMMENT '标题',
    cover       VARCHAR(255) COMMENT '封面',
    description TEXT COMMENT '描述',
    category_id VARCHAR(32)           DEFAULT 0 COMMENT '分类',
    difficulty  INT                   DEFAULT 1 COMMENT '难度',
    start_time  DATETIME              DEFAULT NULL COMMENT '开始时间',
    end_time    DATETIME              DEFAULT NULL COMMENT '结束时间',
    config      JSON                  DEFAULT NULL COMMENT '配置信息',
    # ------------------------------------------------
    deleted     TINYINT(1)            DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME              DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32)           DEFAULT NULL COMMENT '创建者',
    update_time DATETIME              DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32)           DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题集';

-- ----------------------------
-- 题集进度表
-- ----------------------------
DROP TABLE IF EXISTS pro_set_progress;
CREATE TABLE pro_set_progress
(
    id             VARCHAR(32) NOT NULL COMMENT '主键',
    user_id        VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_set_id VARCHAR(32) NOT NULL COMMENT '题单ID',
    progress       JSON        NOT NULL COMMENT '进度信息',
    completed      TINYINT(1)  DEFAULT 0 COMMENT '是否完成',
    completed_at   DATETIME    DEFAULT NULL COMMENT '完成时间',
    # ------------------------------------------------
    deleted        TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time    DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user    VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time    DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user    VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题集进度表';

-- ----------------------------
-- 用户题集解决记录表
-- ----------------------------
DROP TABLE IF EXISTS pro_set_solved;
CREATE TABLE pro_set_solved
(
    id             VARCHAR(32) NOT NULL COMMENT '主键',
    user_id        VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID',
    problem_set_id VARCHAR(32) NOT NULL COMMENT '题单ID',
    submit_id      VARCHAR(32) NOT NULL COMMENT '提交ID',
    solved         TINYINT(1)  DEFAULT 0 COMMENT '是否解决',
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
    COMMENT '用户题集解决记录表';

-- ----------------------------
-- 题集 - 题目关联表 1-N
-- ----------------------------
DROP TABLE IF EXISTS pro_set_problem;
CREATE TABLE pro_set_problem
(
    problem_set_id VARCHAR(32) NOT NULL COMMENT '题单ID',
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题集题目';

-- ----------------------------
-- 题单提交表
-- ----------------------------
DROP TABLE IF EXISTS pro_set_submit;
CREATE TABLE pro_set_submit
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id  VARCHAR(32) NOT NULL COMMENT '题目ID',
    set_id    VARCHAR(32) NULL COMMENT '题集ID',
    language    VARCHAR(64) NOT NULL COMMENT '编程语言',
    code        TEXT        NOT NULL COMMENT '源代码',

    submit_type TINYINT(1)           DEFAULT 0 COMMENT '执行类型',

    max_time    INT         NOT NULL DEFAULT 0 COMMENT '最大耗时',
    max_memory  INT         NOT NULL DEFAULT 0 COMMENT '最大内存使用',

    message     TEXT                 DEFAULT NULL COMMENT '执行结果消息',
    test_cases  JSON                 DEFAULT NULL COMMENT '测试用例结果',
    status      VARCHAR(32)          DEFAULT NULL COMMENT '执行状态',

    -- 查重结果
    similarity  DECIMAL(10, 2)       DEFAULT 0 COMMENT '相似度',
    task_id     VARCHAR(32)          DEFAULT NULL COMMENT '相似检测任务ID',
    # ------------------------------------------------
    deleted     TINYINT(1)           DEFAULT 0 COMMENT '删除状态',
    create_time DATETIME             DEFAULT NULL COMMENT '创建时间戳',
    create_user VARCHAR(32)          DEFAULT NULL COMMENT '创建者',
    update_time DATETIME             DEFAULT NULL COMMENT '更新时间戳',
    update_user VARCHAR(32)          DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id, user_id),
    INDEX (task_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题单提交表';

-- ----------------------------
-- 题单相似度检测任务表
-- ----------------------------
DROP TABLE IF EXISTS pro_set_similarity_task;
CREATE TABLE pro_set_similarity_task
(
    id             VARCHAR(32) NOT NULL COMMENT '主键',
    user_id        VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID',
    set_id    VARCHAR(32) NULL COMMENT '题集ID',

    status         VARCHAR(32)    DEFAULT '' COMMENT '任务状态',
    compare_range  VARCHAR(32)    DEFAULT 'RECENT' COMMENT '比较范围', -- RECENT:近期,ALL:全部
    days_before    INT            DEFAULT 30 COMMENT '近期天数',

    total_compared INT            DEFAULT 0 COMMENT '比较提交数',
    max_similarity DECIMAL(10, 2) DEFAULT 0 COMMENT '最大相似度',

    is_manual      TINYINT(1)     DEFAULT 0 COMMENT '手动任务',
    # ------------------------------------------------
    deleted        TINYINT(1)     DEFAULT 0 COMMENT '删除状态',
    create_time    DATETIME       DEFAULT NULL COMMENT '创建时间戳',
    create_user    VARCHAR(32)    DEFAULT NULL COMMENT '创建者',
    update_time    DATETIME       DEFAULT NULL COMMENT '更新时间戳',
    update_user    VARCHAR(32)    DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id),
    INDEX (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题单代码相似度检测任务表';

-- ----------------------------
-- 题单相似度检测结果表(存储每次检测的详细结果)
-- ----------------------------
DROP TABLE IF EXISTS pro_set_similarity_result;
CREATE TABLE pro_set_similarity_result
(
    id                 VARCHAR(32)    NOT NULL COMMENT '主键',
    task_id            VARCHAR(32)    NOT NULL COMMENT '关联的任务ID',

    origin_submit_id   VARCHAR(32)    NOT NULL COMMENT '提交ID',
    compared_submit_id VARCHAR(32)    NOT NULL COMMENT '被比较的提交ID',

    similarity         DECIMAL(10, 2) NOT NULL COMMENT '相似度值',
    details            JSON COMMENT '详细比对结果',
    match_details      TEXT COMMENT '匹配部分详情',
    threshold          DECIMAL(10, 2) NOT NULL COMMENT '相似度阈值',
    # ------------------------------------------------
    deleted            TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time        DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user        VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time        DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user        VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题单代码相似度检测结果详情表';




















-- ----------------------------
-- 排行榜配置表
-- ----------------------------
DROP TABLE IF EXISTS rank_config;
CREATE TABLE rank_config
(
    id           VARCHAR(32)  NOT NULL COMMENT '主键',
    rank_type    VARCHAR(64)  NOT NULL COMMENT '排行榜类型',
    name         VARCHAR(255) NOT NULL COMMENT '排行榜名称',
    description  TEXT COMMENT '描述',
    scope        VARCHAR(32)  NOT NULL COMMENT '范围(total/problem_set)',
    refresh_rate INT          NOT NULL DEFAULT 3600 COMMENT '刷新频率(秒)',
    config       JSON COMMENT '配置信息',
    # ------------------------------------------------
    status       TINYINT(1)            DEFAULT 1 COMMENT '状态',
    create_at    DATETIME              DEFAULT NULL COMMENT '创建时间戳',
    created_by   VARCHAR(32)           DEFAULT NULL COMMENT '创建者',
    update_at    DATETIME              DEFAULT NULL COMMENT '更新时间戳',
    updated_by   VARCHAR(32)           DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '排行榜配置表';

-- ----------------------------
-- 排行榜快照表
-- ----------------------------
DROP TABLE IF EXISTS rank_snapshot;
CREATE TABLE rank_snapshot
(
    id          VARCHAR(32)  NOT NULL COMMENT '主键',
    rank_type   VARCHAR(64)  NOT NULL COMMENT '排行榜类型',
    name        VARCHAR(255) NOT NULL COMMENT '排行榜名称',
    scope       VARCHAR(32)  NOT NULL COMMENT '范围(total/problem_set)',
    scope_id    VARCHAR(32)  NULL COMMENT '范围ID(题集ID等)',
    snapshot    JSON         NOT NULL COMMENT '排行榜快照数据',
    snapshot_at DATETIME     NOT NULL COMMENT '快照时间',
    # ------------------------------------------------
    status      TINYINT(1)  DEFAULT 1 COMMENT '状态',
    create_at   DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    created_by  VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_at   DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    updated_by  VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '排行榜快照表';

-- ----------------------------
-- 题解表
-- ----------------------------
DROP TABLE IF EXISTS pro_solution;
CREATE TABLE pro_solution
(
    id         VARCHAR(32)  NOT NULL COMMENT '主键',
    title      VARCHAR(255) NOT NULL COMMENT '标题',
    summary    TEXT         NOT NULL COMMENT '摘要',
    cover      VARCHAR(255) COMMENT '封面',
    user_id    VARCHAR(32)  NOT NULL COMMENT '用户ID',
    problem_id VARCHAR(32)  NOT NULL COMMENT '题目ID',
    content    TEXT         NOT NULL COMMENT '内容',
    likes      VARCHAR(32)  NULL COMMENT '点赞数',
    favorite   VARCHAR(32)  NULL COMMENT '收藏数',
    views      VARCHAR(32)  NULL COMMENT '浏览数',
    comments   VARCHAR(32)  NULL COMMENT '评论数',
    # ------------------------------------------------
    status     TINYINT(1)  DEFAULT 1 COMMENT '状态',
    create_at  DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    created_by VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_at  DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    updated_by VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题解表';

-- ----------------------------
-- 点赞表
-- ----------------------------
DROP TABLE IF EXISTS pro_like;
DROP TABLE IF EXISTS pro_like;
CREATE TABLE pro_like
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    solution_id VARCHAR(32) NOT NULL COMMENT '题解ID',
    # ------------------------------------------------
    status      TINYINT(1)  DEFAULT 1 COMMENT '状态',
    create_at   DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    created_by  VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_at   DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    updated_by  VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '点赞表';

-- ----------------------------
-- 收藏表
-- ----------------------------
DROP TABLE IF EXISTS pro_collection;
CREATE TABLE pro_collection
(
    id         VARCHAR(32) NOT NULL COMMENT '主键',
    user_id    VARCHAR(32) NOT NULL COMMENT '用户ID',
    problem_id VARCHAR(32) NOT NULL COMMENT '题目ID',
    # ------------------------------------------------
    status     TINYINT(1)  DEFAULT 1 COMMENT '状态',
    create_at  DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    created_by VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_at  DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    updated_by VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '收藏表';

-- ----------------------------
-- 浏览记录表
-- ----------------------------
DROP TABLE IF EXISTS pro_view;
CREATE TABLE pro_view
(
    `id`          VARCHAR(32) NOT NULL COMMENT '主键',
    `user_id`     VARCHAR(32) COMMENT '用户ID',
    `problem_id`  VARCHAR(32) NOT NULL COMMENT '题目ID',
    `view_ip`     VARCHAR(255) COMMENT 'IP',
    `view_device` VARCHAR(255) COMMENT '设备',
    `view_os`     VARCHAR(255) COMMENT '操作系统',
    # ------------------------------------------------
    status        TINYINT(1)  DEFAULT 1 COMMENT '状态',
    create_at     DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    created_by    VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_at     DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    updated_by    VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '题目浏览记录表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS pro_comment;
CREATE TABLE pro_comment
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    content     TEXT        NOT NULL COMMENT '内容',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    solution_id VARCHAR(32) NOT NULL COMMENT '题解ID',
    likes       VARCHAR(32) NOT NULL DEFAULT 0 COMMENT '点赞数',
    replies     VARCHAR(32) NOT NULL DEFAULT 0 COMMENT '回复数',
    parent_id   VARCHAR(32) NULL COMMENT '父级',
    # ------------------------------------------------
    status      TINYINT(1)           DEFAULT 1 COMMENT '状态',
    create_at   DATETIME             DEFAULT NULL COMMENT '创建时间戳',
    created_by  VARCHAR(32)          DEFAULT NULL COMMENT '创建者',
    update_at   DATETIME             DEFAULT NULL COMMENT '更新时间戳',
    updated_by  VARCHAR(32)          DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '评论表';
