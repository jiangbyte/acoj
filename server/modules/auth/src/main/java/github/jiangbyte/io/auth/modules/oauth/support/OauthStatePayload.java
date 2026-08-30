package github.jiangbyte.io.auth.modules.oauth.support;

import lombok.Data;

/**
 * OAuth state 载荷（存 Redis）。
 *
 * Author: Charlie
 */
@Data
public class OauthStatePayload {
    private String accountType;
    private String intent;
    private String accountId;
    private String provider;
    private String redirect;
}
