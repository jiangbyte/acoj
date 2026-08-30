package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理端创建账号入参（基础字段、资料与可选登录身份）。
 *
 * Author: Charlie
 */
@Schema(description = "管理端创建账号入参（基础字段、资料与可选登录身份）。")
@Data
public class SysAccountAddParam {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,64}$", message = "账号仅允许字母、数字和下划线，长度 3-64")
    @Schema(description = "登录账号/用户名")
    private String account;

    @Schema(description = "配置了 AUTH_DEFAULT_PASSWORD 时允许为空。")
    /**
     * 配置了 {@code AUTH_DEFAULT_PASSWORD} 时允许为空。
     */
    private String password;
    @Schema(description = "passwordKeyId")

    private String passwordKeyId;

    @NotBlank
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户状态：ACTIVE（正常）/ LOCKED（锁定）/ CANCELLED（已注销）")

    private String accountStatus = "ENABLED";
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "signature")
    private String signature;
    @Schema(description = "phone")
    private String phone;
    @Schema(description = "email")
    private String email;
    @Schema(description = "备注说明")
    private String remark;
}
