package io.charlie.app.core.modular.group.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 默认组枚举
 */
@Getter
@AllArgsConstructor
public enum SysGroupEnums implements ILabelEnum<String> {
    DEFAULT_GROUP("1", "默认用户组"),
    ;

    private final String value;
    private final String label;
}
