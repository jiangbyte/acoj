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
    code_length INT         NOT NULL DEFAULT 0 COMMENT '源代码长度',

    submit_type TINYINT(1)           DEFAULT 0 COMMENT '执行类型',

    max_time    INT         NOT NULL DEFAULT 0 COMMENT '最大耗时',
    max_memory  INT         NOT NULL DEFAULT 0 COMMENT '最大内存使用',


    message     TEXT                 DEFAULT NULL COMMENT '执行结果消息',
    test_case   JSON                 DEFAULT NULL COMMENT '测试用例结果',
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
    id                VARCHAR(32)             NOT NULL COMMENT '主键',
    display_id        VARCHAR(32) COMMENT '展示ID',
    category_id       VARCHAR(32)                  DEFAULT 0 COMMENT '分类',

    title             VARCHAR(255)            NOT NULL COMMENT '标题',
    source            VARCHAR(255) COMMENT '来源',
    url               VARCHAR(255) COMMENT '链接',
    max_time          INT                          DEFAULT 0 COMMENT '时间限制',
    max_memory        INT                          DEFAULT 0 COMMENT '内存限制',

    description       TEXT COMMENT '描述',

    test_case         JSON COMMENT '用例',
    allowed_languages JSON COMMENT '开放语言',

    difficulty        INT                          DEFAULT 1 COMMENT '难度',

    threshold         DECIMAL(10, 2) UNSIGNED NULL DEFAULT 0.5 COMMENT '阈值',

    use_template      TINYINT(1)                   DEFAULT 0 COMMENT '使用模板',
    code_template     JSON COMMENT '模板代码',

    is_public         TINYINT(1)                   DEFAULT 0 COMMENT '是否公开',

    -- 统计
    solved            BIGINT                       DEFAULT 0 COMMENT '解决',
    # ------------------------------------------------
    deleted           TINYINT(1)                   DEFAULT 0 COMMENT '删除状态',
    create_time       DATETIME                     DEFAULT NULL COMMENT '创建时间戳',
    create_user       VARCHAR(32)                  DEFAULT NULL COMMENT '创建者',
    update_time       DATETIME                     DEFAULT NULL COMMENT '更新时间戳',
    update_user       VARCHAR(32)                  DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题目表';

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
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID',
    status         VARCHAR(32) NULL COMMENT '状态',
    extra_json     JSON        NULL COMMENT '额外信息',
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
    problem_id     VARCHAR(32) NOT NULL COMMENT '题目ID',
    sort           INT         NOT NULL DEFAULT 0 COMMENT '排序'
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
    set_id      VARCHAR(32) NULL COMMENT '题集ID',
    language    VARCHAR(64) NOT NULL COMMENT '编程语言',
    code        TEXT        NOT NULL COMMENT '源代码',
    code_length INT         NOT NULL DEFAULT 0 COMMENT '源代码长度',

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
    set_id         VARCHAR(32) NULL COMMENT '题集ID',

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






