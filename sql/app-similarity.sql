SET NAMES 'utf8mb4';

CREATE
    DATABASE IF NOT EXISTS astro_code;

use
    astro_code;

-- ----------------------------
-- 报告库表
-- ----------------------------
DROP TABLE IF EXISTS data_similar_reports;
CREATE TABLE data_similar_reports
(
    id                     VARCHAR(32)             NOT NULL COMMENT '主键',
    report_type            INT                     NOT NULL DEFAULT 0 COMMENT '报告类型', -- 0 单次提交报告 1 题目报告，2 题集报告
    task_id                VARCHAR(32)             NOT NULL COMMENT '任务ID',
    set_id                 VARCHAR(32)             NOT NULL COMMENT '题集ID',
    problem_id             VARCHAR(32)             NOT NULL COMMENT '题目ID',
    sample_count           INT                     NOT NULL DEFAULT 0 COMMENT '样例数量',
    similarity_group_count INT                     NOT NULL DEFAULT 0 COMMENT '相似组数量',
    max_similarity         DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0.0 COMMENT '最大相似度',
    threshold              DECIMAL(10, 2) UNSIGNED NULL     DEFAULT 0.5 COMMENT '检测阈值',
    check_mode             INT                     NOT NULL DEFAULT 1 COMMENT '检测模式',
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
    COMMENT '报告库表';
