package github.jiangbyte.io.common.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 账号类型枚举：管理端（ADMIN）与门户端（PORTAL）。
 *
 * Author: Charlie
 */
@Schema(description = "账号类型枚举：管理端（ADMIN）与门户端（PORTAL）。")
public enum AccountType {
    ADMIN,
    PORTAL
}
