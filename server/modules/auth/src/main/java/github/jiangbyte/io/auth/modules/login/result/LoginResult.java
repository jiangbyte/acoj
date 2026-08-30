package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import lombok.Data;

/**
 * 登录/刷新会话成功后的响应：Token、账号信息、密码过期与 TTL。
 *
 * Author: Charlie
 */
@Schema(description = "登录/刷新会话成功后的响应：Token、账号信息、密码过期与 TTL。")
@Data
public class LoginResult {
    @Schema(description = "token")
    private String token;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private AccountType accountType;
    @Schema(description = "passwordExpired")
    private Boolean passwordExpired = false;
    @Schema(description = "是否需强制绑定邮箱（硬拦截）")
    /** 是否需强制绑定邮箱（硬拦截） */
    private Boolean forceBindEmail = false;
    @Schema(description = "是否需强制绑定手机（硬拦截）")
    /** 是否需强制绑定手机（硬拦截） */
    private Boolean forceBindPhone = false;
    @Schema(description = "处于告警窗口时的剩余天数；不适用时为 null。")
    /**
     * 处于告警窗口时的剩余天数；不适用时为 null。
     */
    private Integer passwordExpiryWarningDays;
    @Schema(description = "登录/刷新后的绝对 token TTL（秒）；使用 Sa-Token 默认时为 null。")
    /** 登录/刷新后的绝对 token TTL（秒）；使用 Sa-Token 默认时为 null。 */
    private Long expiresIn;
}
