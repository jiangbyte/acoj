package github.jiangbyte.io.auth.modules.session.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单个 Token 会话详情：登录时间、过期时间、客户端 IP 与设备信息。
 *
 * Author: Charlie
 */
@Schema(description = "单个 Token 会话详情：登录时间、过期时间、客户端 IP 与设备信息。")
@Data
public class SessionTokenResult {
    @Schema(description = "token")
    private String token;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "loginAt")
    private String loginAt;
    @Schema(description = "lastActiveAt")
    private String lastActiveAt;
    @Schema(description = "expiresAt")
    private String expiresAt;
    @Schema(description = "clientIp")
    private String clientIp;
    @Schema(description = "deviceLabel")
    private String deviceLabel;
    @Schema(description = "客户端 User-Agent")
    private String userAgent;
    @Schema(description = "rememberMe")
    private Boolean rememberMe;
}
