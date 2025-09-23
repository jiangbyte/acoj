SET NAMES 'utf8mb4';

CREATE
    DATABASE IF NOT EXISTS astro_code;

use
    astro_code;

-- ----------------------------
-- 提交表
-- ----------------------------
DROP TABLE IF EXISTS data_submit;
CREATE TABLE data_submit
(
    id                  VARCHAR(32) NOT NULL COMMENT '主键',
    user_id             VARCHAR(32) NOT NULL COMMENT '用户ID',
    set_id              VARCHAR(32)          DEFAULT NULL COMMENT '题集ID',
    judge_task_id             VARCHAR(50)          DEFAULT NULL COMMENT '相似检测任务ID',
    is_set              TINYINT(1)           DEFAULT 0 COMMENT '是否是题集提交',
    problem_id          VARCHAR(32) NOT NULL COMMENT '题目ID',
    language            VARCHAR(64) NOT NULL COMMENT '编程语言',
    code                TEXT        NOT NULL COMMENT '源代码',
    code_length         INT         NOT NULL DEFAULT 0 COMMENT '源代码长度',

    submit_type         TINYINT(1)           DEFAULT 0 COMMENT '执行类型',

    max_time            INT         NOT NULL DEFAULT 0 COMMENT '最大耗时',
    max_memory          INT         NOT NULL DEFAULT 0 COMMENT '最大内存使用',

    message             TEXT                 DEFAULT NULL COMMENT '执行结果消息',
    test_case           JSON                 DEFAULT NULL COMMENT '测试用例结果',
    status              VARCHAR(32)          DEFAULT NULL COMMENT '执行状态',

    is_finish           TINYINT(1)           DEFAULT 0 COMMENT '流程流转是否完成',

    -- 查重结果
    similarity          DECIMAL(10, 2)       DEFAULT 0 COMMENT '相似度',
    task_id             VARCHAR(32)          DEFAULT NULL COMMENT '相似检测任务ID',
    report_id           VARCHAR(32)          DEFAULT NULL COMMENT '报告ID',
    similarity_category VARCHAR(32)          DEFAULT NULL COMMENT '相似分级',

    # ------------------------------------------------
    deleted             TINYINT(1)           DEFAULT 0 COMMENT '删除状态',
    create_time         DATETIME             DEFAULT NULL COMMENT '创建时间戳',
    create_user         VARCHAR(32)          DEFAULT NULL COMMENT '创建者',
    update_time         DATETIME             DEFAULT NULL COMMENT '更新时间戳',
    update_user         VARCHAR(32)          DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id, user_id),
    INDEX (task_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '提交表';

-- ----------------------------
-- oj题目表
-- ----------------------------
DROP TABLE IF EXISTS data_problem;
CREATE TABLE data_problem
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
    is_visible        TINYINT(1)                   DEFAULT 1 COMMENT '是否可见',
    use_ai            TINYINT(1)                   DEFAULT 0 COMMENT '是否使用AI',

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
DROP TABLE IF EXISTS data_problem_tag;
CREATE TABLE data_problem_tag
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
DROP TABLE IF EXISTS data_solved;
CREATE TABLE data_solved
(
    id          VARCHAR(32) NOT NULL COMMENT '主键',
    set_id      VARCHAR(32) DEFAULT NULL COMMENT '题集ID',
    is_set      TINYINT(1)  DEFAULT 0 COMMENT '是否是题集提交',
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
DROP TABLE IF EXISTS data_set;
CREATE TABLE data_set
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
    is_visible        TINYINT(1)                   DEFAULT 1 COMMENT '是否可见',
    use_ai            TINYINT(1)                   DEFAULT 0 COMMENT '是否使用AI',

    ex_json     JSON                  DEFAULT NULL COMMENT '额外的信息',
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
-- 题集 - 题目关联表 1-N
-- ----------------------------
DROP TABLE IF EXISTS data_set_problem;
CREATE TABLE data_set_problem
(
    set_id     VARCHAR(32) NOT NULL COMMENT '题集ID',
    problem_id VARCHAR(32) NOT NULL COMMENT '题目ID',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题集题目';

-- ----------------------------
-- 题集进度表
-- ----------------------------
DROP TABLE IF EXISTS data_progress;
CREATE TABLE data_progress
(
    id           VARCHAR(32) NOT NULL COMMENT '主键',
    user_id      VARCHAR(32) NOT NULL COMMENT '用户ID',
    set_id       VARCHAR(32) NOT NULL COMMENT '题集ID',
    problem_id   VARCHAR(32) NOT NULL COMMENT '题目ID',
    status       VARCHAR(32) NULL COMMENT '状态',
    extra_json   JSON        NULL COMMENT '额外信息',
    completed    TINYINT(1)  DEFAULT 0 COMMENT '是否完成',
    completed_at DATETIME    DEFAULT NULL COMMENT '完成时间',
    # ------------------------------------------------
    deleted      TINYINT(1)  DEFAULT 0 COMMENT '删除状态',
    create_time  DATETIME    DEFAULT NULL COMMENT '创建时间戳',
    create_user  VARCHAR(32) DEFAULT NULL COMMENT '创建者',
    update_time  DATETIME    DEFAULT NULL COMMENT '更新时间戳',
    update_user  VARCHAR(32) DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题集进度表';



-- ----------------------------
-- 题目提交样本库
-- ----------------------------
DROP TABLE IF EXISTS data_library;
CREATE TABLE data_library
(
    id           VARCHAR(32) NOT NULL COMMENT '主键',
    user_id      VARCHAR(32) NOT NULL COMMENT '用户ID',
    set_id       VARCHAR(32) NOT NULL COMMENT '题集ID',
    is_set       TINYINT(1)           DEFAULT 0 COMMENT '是否是题集提交',
    problem_id   VARCHAR(32) NOT NULL COMMENT '题目ID',
    submit_id    VARCHAR(32) NULL COMMENT '提交ID',
    submit_time  DATETIME    NULL COMMENT '提交时间',
    language     VARCHAR(64) NOT NULL COMMENT '编程语言',
    code         TEXT        NOT NULL COMMENT '源代码',
    code_length  INT         NOT NULL DEFAULT 0 COMMENT '源代码长度',
    access_count INT                  DEFAULT 0 COMMENT '访问次数',
    # ------------------------------------------------
    deleted      TINYINT(1)           DEFAULT 0 COMMENT '删除状态',
    create_time  DATETIME             DEFAULT NULL COMMENT '创建时间戳',
    create_user  VARCHAR(32)          DEFAULT NULL COMMENT '创建者',
    update_time  DATETIME             DEFAULT NULL COMMENT '更新时间戳',
    update_user  VARCHAR(32)          DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id),
    INDEX (problem_id, user_id),
    INDEX (user_id, problem_id),
    INDEX (submit_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '提交样本库';

-- ----------------------------
-- 检测结果任务库
-- ----------------------------
DROP TABLE IF EXISTS task_similarity;
CREATE TABLE task_similarity
(
    id                 VARCHAR(32)             NOT NULL COMMENT '主键',
    -- 基础信息
    task_id            VARCHAR(32)             NOT NULL COMMENT '任务ID',
    task_type          TINYINT(1)                       DEFAULT 0 COMMENT '手动', -- 0:自动,1:手动,2:定时
    problem_id         VARCHAR(32)             NOT NULL COMMENT '题目ID',
    set_id             VARCHAR(32)             NOT NULL COMMENT '题集ID',
    is_set             TINYINT(1)                       DEFAULT 0 COMMENT '是否是题集提交',
    language           VARCHAR(64)             NOT NULL COMMENT '编程语言',
    similarity         DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0 COMMENT '相似度',

    -- 提交用户
    submit_user        VARCHAR(32)             NOT NULL COMMENT '提交用户',
    submit_code        TEXT                    NOT NULL COMMENT '源代码',
    submit_code_length INT                     NOT NULL DEFAULT 0 COMMENT '源代码长度',
    submit_id          VARCHAR(32)             NULL COMMENT '提交ID',
    submit_time        DATETIME                NULL COMMENT '提交时间',
    submit_token_name  JSON                    NULL COMMENT '提交用户Token名称',
    submit_token_texts JSON                    NULL COMMENT '提交用户Token内容',

    -- 样本用户
    origin_user        VARCHAR(32)             NOT NULL COMMENT '样本用户',
    origin_code        TEXT                    NOT NULL COMMENT '样本源代码',
    origin_code_length INT                     NOT NULL DEFAULT 0 COMMENT '样本源代码长度',
    origin_id          VARCHAR(32)             NULL COMMENT '样本提交ID',
    origin_time        DATETIME                NULL COMMENT '样本提交时间',
    origin_token_name  JSON                    NULL COMMENT '样本用户Token名称',
    origin_token_texts JSON                    NULL COMMENT '样本用户Token内容',

    # ------------------------------------------------
    deleted            TINYINT(1)                       DEFAULT 0 COMMENT '删除状态',
    create_time        DATETIME                         DEFAULT NULL COMMENT '创建时间戳',
    create_user        VARCHAR(32)                      DEFAULT NULL COMMENT '创建者',
    update_time        DATETIME                         DEFAULT NULL COMMENT '更新时间戳',
    update_user        VARCHAR(32)                      DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '检测结果任务库';

-- ----------------------------
-- 报告库表
-- ----------------------------
DROP TABLE IF EXISTS task_reports;
CREATE TABLE task_reports
(
    id                      VARCHAR(32)             NOT NULL COMMENT '主键',
    report_type             INT                     NOT NULL DEFAULT 0 COMMENT '报告类型', -- 0 单次提交报告 1 题目报告，2 题集报告
    task_id                 VARCHAR(32)             NOT NULL COMMENT '任务ID',
    set_id                  VARCHAR(32)             NOT NULL COMMENT '题集ID',
    is_set                  TINYINT(1)                       DEFAULT 0 COMMENT '是否是题集提交',
    problem_id              VARCHAR(32)             NOT NULL COMMENT '题目ID',
    sample_count            INT                     NOT NULL DEFAULT 0 COMMENT '样例数量',
    similarity_group_count  INT                     NOT NULL DEFAULT 0 COMMENT '相似组数量',
    avg_similarity          DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0.0 COMMENT '平均相似度',
    max_similarity          DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0.0 COMMENT '最大相似度',
    threshold               DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0.5 COMMENT '检测阈值',
    similarity_distribution JSON                    NULL COMMENT '相似度分布',
    degree_statistics       JSON                    NULL COMMENT '程度统计',
    check_mode              INT                     NOT NULL DEFAULT 1 COMMENT '检测模式',
    # ------------------------------------------------
    deleted                 TINYINT(1)                       DEFAULT 0 COMMENT '删除状态',
    create_time             DATETIME                         DEFAULT NULL COMMENT '创建时间戳',
    create_user             VARCHAR(32)                      DEFAULT NULL COMMENT '创建者',
    update_time             DATETIME                         DEFAULT NULL COMMENT '更新时间戳',
    update_user             VARCHAR(32)                      DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '报告库表';

