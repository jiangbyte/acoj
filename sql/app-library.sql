SET NAMES 'utf8mb4';

CREATE
    DATABASE IF NOT EXISTS astro_code;

use
    astro_code;

-- ----------------------------
-- 题目提交样本库
-- ----------------------------
DROP TABLE IF EXISTS pro_sample_library;
CREATE TABLE pro_sample_library
(
    id           VARCHAR(32) NOT NULL COMMENT '主键',
    user_id      VARCHAR(32) NOT NULL COMMENT '用户ID',
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
    COMMENT '题目提交样本库';

-- ----------------------------
-- 题目检测结果任务库
-- ----------------------------
DROP TABLE IF EXISTS pro_similarity_detail;
CREATE TABLE pro_similarity_detail
(
    id                 VARCHAR(32)             NOT NULL COMMENT '主键',
    -- 基础信息
    task_id            VARCHAR(32)             NOT NULL COMMENT '任务ID',
    task_type          TINYINT(1)                       DEFAULT 0 COMMENT '手动', -- 0:自动,1:手动,2:定时
    problem_id         VARCHAR(32)             NOT NULL COMMENT '题目ID',
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
    COMMENT '题目检测结果任务库';

-- ----------------------------
-- 报告库表
-- ----------------------------
DROP TABLE IF EXISTS pro_similarity_reports;
CREATE TABLE pro_similarity_reports
(
    id                      VARCHAR(32)             NOT NULL COMMENT '主键',
    report_type             INT                     NOT NULL DEFAULT 0 COMMENT '报告类型', -- 0 单次提交报告 1 题目报告，2 题集报告
    task_id                 VARCHAR(32)             NOT NULL COMMENT '任务ID',
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
    COMMENT '题目报告库表';


-- ----------------------------
-- 题目题集提交样本库
-- ----------------------------
DROP TABLE IF EXISTS pro_set_sample_library;
CREATE TABLE pro_set_sample_library
(
    id           VARCHAR(32) NOT NULL COMMENT '主键',
    user_id      VARCHAR(32) NOT NULL COMMENT '用户ID',
    set_id       VARCHAR(32) NOT NULL COMMENT '题集ID',
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
    INDEX (problem_id, user_id, set_id),
    INDEX (user_id, problem_id, set_id),
    INDEX (set_id, problem_id, user_id),
    INDEX (problem_id, set_id, user_id),
    INDEX (submit_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题目题集提交样本库';


-- ----------------------------
-- 题集题目检测结果任务库
-- ----------------------------
DROP TABLE IF EXISTS pro_set_similarity_detail;
CREATE TABLE pro_set_similarity_detail
(
    id                 VARCHAR(32)             NOT NULL COMMENT '主键',
    -- 基础信息
    task_id            VARCHAR(32)             NOT NULL COMMENT '任务ID',
    task_type          TINYINT(1)                       DEFAULT 0 COMMENT '手动', -- 0:自动,1:手动,2:定时
    set_id             VARCHAR(32)             NOT NULL COMMENT '题集ID',
    problem_id         VARCHAR(32)             NOT NULL COMMENT '题目ID',
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
    COMMENT '题集题目检测结果任务库';

-- ----------------------------
-- 报告库表
-- ----------------------------
DROP TABLE IF EXISTS pro_set_similarity_reports;
CREATE TABLE pro_set_similarity_reports
(
    id                     VARCHAR(32)             NOT NULL COMMENT '主键',
    report_type            INT                     NOT NULL DEFAULT 0 COMMENT '报告类型', -- 0 单次提交报告 1 题目报告，2 题集报告
    task_id                VARCHAR(32)             NOT NULL COMMENT '任务ID',
    set_id                 VARCHAR(32)             NOT NULL COMMENT '题集ID',
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
    deleted                TINYINT(1)                       DEFAULT 0 COMMENT '删除状态',
    create_time            DATETIME                         DEFAULT NULL COMMENT '创建时间戳',
    create_user            VARCHAR(32)                      DEFAULT NULL COMMENT '创建者',
    update_time            DATETIME                         DEFAULT NULL COMMENT '更新时间戳',
    update_user            VARCHAR(32)                      DEFAULT NULL COMMENT '更新者',
    -- 添加索引
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '题库题目报告库表';