package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import lombok.Data;

/**
 * 门户注册成功响应：新账号 ID、登录名与账号类型。
 *
 * Author: Charlie
 */
@Schema(description = "门户注册成功响应：新账号 ID、登录名与账号类型。")
@Data
public class RegisterResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private AccountType accountType;
}
