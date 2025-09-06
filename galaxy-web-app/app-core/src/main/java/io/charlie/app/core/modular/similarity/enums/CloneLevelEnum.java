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
public enum CloneLevelEnum implements ILabelEnum<String> {
    HIGHLY_SUSPECTED("HIGHLY_SUSPECTED", "高度可疑"),
    MEDIUM_SUSPECTED("MEDIUM_SUSPECTED", "中度可疑"),
    LOW_SUSPECTED("LOW_SUSPECTED", "轻度可疑"),
    NOT_REACHED("NOT_REACHED", "未达阈值"),
    NOT_DETECTED("NOT_DETECTED", "未检测"),
    ;

    private final String value;
    private final String label;
}
