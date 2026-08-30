package github.jiangbyte.io.auth.modules.oauth.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发起 OAuth 授权的响应。
 *
 * Author: Charlie
 */
@Schema(description = "发起 OAuth 授权的响应。")
@Data
public class OauthAuthorizeResult {
    @Schema(description = "authorizeUrl")
    private String authorizeUrl;
    @Schema(description = "state")
    private String state;
}
