package io.charlie.web.oj.modular.task.similarity.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 05/09/2025
 * @description 相似等级
 */
@AllArgsConstructor
@Getter
public enum ReportTypeEnum implements ILabelEnum<Integer> {
    PROBLEM_SINGLE_SUBMIT(1, "题目单提交"),
    SET_SINGLE_SUBMIT(2, "题集单提交"),
    PROBLEM_SUBMIT(3, "题目提交"),
    SET_SUBMIT(4, "题集提交"),
    ;

    private final Integer value;
    private final String label;
}
