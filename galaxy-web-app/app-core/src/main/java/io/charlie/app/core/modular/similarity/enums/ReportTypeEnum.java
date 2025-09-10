package io.charlie.app.core.modular.similarity.enums;

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
    SINGLE_SUBMIT(1, "单提交"),
    PROBLEM_SUBMIT(2, "题目提交"),
    ;

    private final Integer value;
    private final String label;
}
