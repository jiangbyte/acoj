package io.charlie.galaxy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 22/07/2025
 * @description 排序枚举
 */
@Getter
@AllArgsConstructor
public enum ISortOrderEnum implements ILabelEnum<String> {
    ASCEND("ASCEND", "升序"),
    DESCEND("DESCEND", "降序"),
    ;
    private final String value;

    private final String label;

    // 验证是否有效
    public static boolean isValid(String value) {
        for (ISortOrderEnum item : values()) {
            if (item.getValue().equals(value)) {
                return true;
            }
        }

        return false;
    }
}
