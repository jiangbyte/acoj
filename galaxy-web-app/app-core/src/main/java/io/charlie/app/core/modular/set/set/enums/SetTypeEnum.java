package io.charlie.app.core.modular.set.set.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 09/08/2025
 * @description 题集类型枚举
 */
@AllArgsConstructor
@Getter
public enum SetTypeEnum implements ILabelEnum<Integer> {
    NORMAL_SET(1, "常规题集"),
    LIMIT_TIME_SET(2, "限时题集");

    private final Integer value;
    private final String label;
}