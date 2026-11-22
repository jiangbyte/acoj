package io.charlie.web.modular.task.similarity.enums;

import lombok.Getter;

/**
 * 筛查任务执行状态（持久化到 task_reports.exec_status）。
 */
@Getter
public enum SimilarityExecStatus {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String value;

    SimilarityExecStatus(String value) {
        this.value = value;
    }
}
