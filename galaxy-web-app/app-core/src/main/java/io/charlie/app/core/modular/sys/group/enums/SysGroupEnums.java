package io.charlie.app.core.modular.sys.group.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 系统组枚举
 */
@Getter
@AllArgsConstructor
public enum SysGroupEnums implements ILabelEnum<String> {
    DEFAULT_GROUP("0", "默认组"),
    ;

    private final String value;

    private final String label;
}
