package github.jiangbyte.io.auth.modules.oauth.support;

import java.util.Locale;
import java.util.Set;

/**
 * 支持的三方登录提供商。
 *
 * Author: Charlie
 */
public enum OauthProvider {
    GITHUB("GitHub", true),
    GITEE("Gitee", true),
    QQ("QQ", true),
    WECHAT_OPEN("微信", true),
    WECHAT_MP("微信小程序", false);

    private final String label;
    /** 是否走网页 OAuth 授权码流程（小程序为 false）。 */
    private final boolean webOAuth;

    OauthProvider(String label, boolean webOAuth) {
        this.label = label;
        this.webOAuth = webOAuth;
    }

    public String getLabel() {
        return label;
    }

    public boolean isWebOAuth() {
        return webOAuth;
    }

    public static OauthProvider from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("provider required");
        }
        return OauthProvider.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    public static final Set<String> WECHAT_FAMILY = Set.of(WECHAT_OPEN.name(), WECHAT_MP.name());
}
