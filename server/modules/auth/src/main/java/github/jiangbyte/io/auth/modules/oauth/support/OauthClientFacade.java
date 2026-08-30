package github.jiangbyte.io.auth.modules.oauth.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.sys.config.ConfigApi;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JustAuth / 微信小程序客户端工厂。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class OauthClientFacade {

    private final ConfigApi configApi;
    private final ObjectMapper objectMapper;

    public String buildAuthorizeUrl(AccountType accountType, OauthProvider provider, String state) {
        if (!provider.isWebOAuth()) {
            throw new BizException("该提供商不支持网页授权");
        }
        AuthRequest request = createAuthRequest(accountType, provider);
        return request.authorize(state);
    }

    public OauthUserProfile loginByCode(AccountType accountType, OauthProvider provider, String code, String state) {
        if (!provider.isWebOAuth()) {
            throw new BizException("该提供商不支持网页授权回调");
        }
        AuthRequest request = createAuthRequest(accountType, provider);
        AuthCallback callback = AuthCallback.builder().code(code).state(state).build();
        AuthResponse<?> response;
        try {
            response = request.login(callback);
        } catch (AuthException ex) {
            throw new BizException("三方登录失败: " + ex.getMessage());
        }
        if (response == null || !response.ok() || !(response.getData() instanceof AuthUser user)) {
            String msg = response == null ? "无响应" : String.valueOf(response.getMsg());
            throw new BizException("三方登录失败: " + msg);
        }
        return toProfile(provider, user);
    }

    public OauthUserProfile loginWechatMp(AccountType accountType, String jsCode) {
        ensureEnabled(accountType, OauthProvider.WECHAT_MP);
        String appId = configValue(accountType, OauthProvider.WECHAT_MP, "APP_ID");
        String secret = configValue(accountType, OauthProvider.WECHAT_MP, "APP_SECRET");
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(secret)) {
            throw new BizException("微信小程序未配置 AppId/AppSecret");
        }
        if (!StringUtils.hasText(jsCode)) {
            throw new BizException("缺少 code");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + enc(appId)
                + "&secret=" + enc(secret)
                + "&js_code=" + enc(jsCode.trim())
                + "&grant_type=authorization_code";
        String body = RestClient.create().get().uri(url).retrieve().body(String.class);
        JsonNode json;
        try {
            json = objectMapper.readTree(body == null ? "{}" : body);
        } catch (Exception e) {
            throw new BizException("微信小程序登录失败: 响应解析错误");
        }
        if (json.has("errcode") && json.get("errcode").asInt() != 0) {
            throw new BizException("微信小程序登录失败: " + json.path("errmsg").asText());
        }
        String openId = json.path("openid").asText(null);
        if (!StringUtils.hasText(openId)) {
            throw new BizException("微信小程序登录失败: 未返回 openid");
        }
        OauthUserProfile profile = new OauthUserProfile();
        profile.setProvider(OauthProvider.WECHAT_MP.name());
        profile.setOpenId(openId);
        String unionId = json.path("unionid").asText(null);
        profile.setUnionId(StringUtils.hasText(unionId) ? unionId : null);
        profile.setRawProfileJson(body);
        return profile;
    }

    public void ensureEnabled(AccountType accountType, OauthProvider provider) {
        AccountType type = accountType == null ? AccountType.PORTAL : accountType;
        if (!configApi.getBoolean(configKey(type, provider, "ENABLED"), false)) {
            throw new BizException(provider.getLabel() + " 登录未启用");
        }
    }

    public String resolveRedirectUri(AccountType accountType, OauthProvider provider) {
        String configured = configValue(accountType, provider, "REDIRECT_URI");
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        throw new BizException("请配置 " + configKey(accountType, provider, "REDIRECT_URI"));
    }

    private AuthRequest createAuthRequest(AccountType accountType, OauthProvider provider) {
        ensureEnabled(accountType, provider);
        String clientId = firstNonBlank(
                configValue(accountType, provider, "CLIENT_ID"),
                configValue(accountType, provider, "APP_ID"));
        String clientSecret = firstNonBlank(
                configValue(accountType, provider, "CLIENT_SECRET"),
                configValue(accountType, provider, "APP_SECRET"));
        String redirectUri = resolveRedirectUri(accountType, provider);
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new BizException(provider.getLabel() + " 未配置 ClientId/Secret");
        }
        AuthConfig config = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .ignoreCheckState(true)
                .build();
        return switch (provider) {
            case GITHUB -> new AuthGithubRequest(config);
            case GITEE -> new AuthGiteeRequest(config);
            case QQ -> new AuthQqRequest(config);
            case WECHAT_OPEN -> new AuthWeChatOpenRequest(config);
            case WECHAT_MP -> throw new BizException("小程序请使用 code 登录接口");
        };
    }

    private OauthUserProfile toProfile(OauthProvider provider, AuthUser user) {
        OauthUserProfile profile = new OauthUserProfile();
        profile.setProvider(provider.name());
        profile.setOpenId(user.getUuid());
        profile.setUnionId(extractUnionId(user));
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        ObjectNode raw = objectMapper.createObjectNode();
        raw.put("uuid", user.getUuid());
        raw.put("username", user.getUsername());
        raw.put("nickname", user.getNickname());
        raw.put("avatar", user.getAvatar());
        raw.put("email", user.getEmail());
        raw.put("source", user.getSource());
        if (user.getRawUserInfo() != null) {
            raw.set("rawUserInfo", objectMapper.valueToTree(user.getRawUserInfo()));
        }
        try {
            profile.setRawProfileJson(objectMapper.writeValueAsString(raw));
        } catch (Exception e) {
            profile.setRawProfileJson("{}");
        }
        return profile;
    }

    @SuppressWarnings("unchecked")
    private static String extractUnionId(AuthUser user) {
        Object raw = user.getRawUserInfo();
        if (raw == null) {
            return null;
        }
        if (raw instanceof Map<?, ?> map) {
            Object unionId = map.get("unionid");
            if (unionId == null) {
                unionId = map.get("unionId");
            }
            return unionId == null ? null : String.valueOf(unionId);
        }
        try {
            Object unionId = raw.getClass().getMethod("get", Object.class).invoke(raw, "unionid");
            if (unionId == null) {
                unionId = raw.getClass().getMethod("get", Object.class).invoke(raw, "unionId");
            }
            return unionId == null ? null : String.valueOf(unionId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String configValue(AccountType accountType, OauthProvider provider, String field) {
        return configApi.getValue(configKey(accountType, provider, field), "").trim();
    }

    public static String configKey(AccountType accountType, OauthProvider provider, String field) {
        AccountType type = accountType == null ? AccountType.PORTAL : accountType;
        return "AUTH_OAUTH_" + type.name() + "_" + provider.name() + "_" + field;
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a.trim();
        }
        return StringUtils.hasText(b) ? b.trim() : "";
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
