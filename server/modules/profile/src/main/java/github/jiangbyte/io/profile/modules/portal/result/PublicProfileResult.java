package github.jiangbyte.io.profile.modules.portal.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门户公开资料响应：空间详情可见的账号展示字段。
 *
 * Author: Charlie
 */
@Schema(description = "门户公开资料响应：空间详情可见的账号展示字段。")
@Data
public class PublicProfileResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "对外展示昵称（不含登录账号兜底）。")
    /** 对外展示昵称（不含登录账号兜底）。 */
    private String nickname;
    @Schema(description = "登录账号（资料页单独展示，如 @account）。")
    /** 登录账号（资料页单独展示，如 @account）。 */
    private String account;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "signature")
    private String signature;
}
