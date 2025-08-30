

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