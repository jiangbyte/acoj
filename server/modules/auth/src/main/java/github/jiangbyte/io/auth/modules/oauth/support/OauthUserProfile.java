package github.jiangbyte.io.auth.modules.oauth.support;

import lombok.Data;

/**
 * 统一的三方用户资料。
 *
 * Author: Charlie
 */
@Data
public class OauthUserProfile {
    private String provider;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
    private String rawProfileJson;
}
