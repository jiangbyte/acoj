package github.jiangbyte.io.auth.modules.oauth.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 当前用户三方绑定列表项。
 *
 * Author: Charlie
 */
@Schema(description = "当前用户三方绑定列表项。")
@Data
public class OauthBindingResult {
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "label")
    private String label;
    @Schema(description = "openIdMasked")
    private String openIdMasked;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "boundAt")
    private OffsetDateTime boundAt;
}
