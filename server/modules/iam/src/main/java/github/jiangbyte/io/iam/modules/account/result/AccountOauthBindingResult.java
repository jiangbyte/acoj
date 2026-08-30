package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号详情中的三方绑定行。
 *
 * Author: Charlie
 */
@Schema(description = "账号详情中的三方绑定行。")
@Data
public class AccountOauthBindingResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "openId")
    private String openId;
    @Schema(description = "unionId")
    private String unionId;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "boundAt")
    private OffsetDateTime boundAt;
}
