package io.charlie.app.core.modular.auth.enums;


import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 平台枚举
 */
@Getter
@AllArgsConstructor
public enum PlatformEnum implements ILabelEnum<String> {
    CLIENT("CLIENT", "客户端"),
    ADMIN("ADMIN", "管理端");

    private final String value;
    private final String label;

    public static boolean isValid(String value) {
        for (PlatformEnum item : values()) {
            if (item.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
