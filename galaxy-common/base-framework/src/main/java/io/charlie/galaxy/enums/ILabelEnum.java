package io.charlie.galaxy.enums;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 基础枚举
 */
public interface ILabelEnum<T> {

    T getValue(); // 获取枚举值

    String getLabel(); // 获取枚举标签

}
