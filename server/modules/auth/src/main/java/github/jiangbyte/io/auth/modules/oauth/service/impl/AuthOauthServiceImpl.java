package github.jiangbyte.io.auth.modules.oauth.service.impl;

import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.service.AuthService;
import github.jiangbyte.io.auth.modules.oauth.result.OauthAuthorizeResult;
import github.jiangbyte.io.auth.modules.oauth.result.OauthBindingResult;
import github.jiangbyte.io.auth.modules.oauth.service.AuthOauthService;
import github.jiangbyte.io.auth.modules.oauth.support.OauthClientFacade;
import github.jiangbyte.io.auth.modules.oauth.support.OauthExchangeStore;
import github.jiangbyte.io.auth.modules.oauth.support.OauthProvider;
import github.jiangbyte.io.auth.modules.oauth.support.OauthStatePayload;
import github.jiangbyte.io.auth.modules.oauth.support.OauthStateStore;
import github.jiangbyte.io.auth.modules.oauth.support.OauthUserProfile;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.account.AccountApi;
import github.jiangbyte.io.iam.account.AccountInfo;
import github.jiangbyte.io.iam.account.AccountOauthApi;
import github.jiangbyte.io.iam.account.AccountOauthBindingInfo;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link AuthOauthService} 实现。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AuthOauthServiceImpl implements AuthOauthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OauthClientFacade oauthClientFacade;
    private final OauthStateStore oauthStateStore;
    private final OauthExchangeStore oauthExchangeStore;
    private final AccountOauthApi accountOauthApi;
    private final AccountApi accountApi;
    private final AuthService authService;
    private final ConfigApi configApi;
    private final ProfileUserPortalApi portalUserProfileApi;

    @Value("${hei.app.frontend-base-url:http://localhost:5173}")
    private String adminFrontendBaseUrl;

    @Value("${hei.app.portal-frontend-base-url:http://localhost:5174}")
    private String portalFrontendBaseUrl;

    @Override
    public OauthAuthorizeResult authorize(AccountType accountType, String provider, String intent, String redirect) {
        AccountType type = accountType == null ? AccountType.PORTAL : accountType;
        OauthProvider oauthProvider = OauthProvider.from(provider);
        if (!oauthProvider.isWebOAuth()) {
            throw new BizException("请使用小程序登录接口");
        }
        oauthClientFacade.ensureEnabled(type, oauthProvider);

        String normalizedIntent = StringUtils.hasText(intent) ? intent.trim().toUpperCase(Locale.ROOT) : "LOGIN";
        if (!"LOGIN".equals(normalizedIntent) && !"BIND".equals(normalizedIntent)) {
            throw new BizException("不支持的 OAuth intent");
        }

        OauthStatePayload payload = new OauthStatePayload();
        payload.setAccountType(type.name());
        payload.setIntent(normalizedIntent);
        payload.setProvider(oauthProvider.name());
        payload.setRedirect(redirect);
        if ("BIND".equals(normalizedIntent)) {
            LoginUser user = LoginHelper.requireUser();
            if (user.getAccountType() != type) {
                throw new BizException("账号类型不匹配");
            }
            payload.setAccountId(user.getAccountId());
        }

        String state = oauthStateStore.save(payload);
        String authorizeUrl = oauthClientFacade.buildAuthorizeUrl(type, oauthProvider, state);
        OauthAuthorizeResult result = new OauthAuthorizeResult();
        result.setAuthorizeUrl(authorizeUrl);
        result.setState(state);
        return result;
    }

    @Override
    @Transactional
    public String handleCallback(AccountType accountType, String provider, String code, String state) {
        AccountType type = accountType == null ? AccountType.PORTAL : accountType;
        OauthProvider oauthProvider = OauthProvider.from(provider);
        OauthStatePayload payload = oauthStateStore.consume(state);
        String frontend = frontendCallback(type);
        if (payload == null) {
            return failRedirect(frontend, "授权已过期，请重试");
        }
        if (!type.name().equalsIgnoreCase(payload.getAccountType())
                || !oauthProvider.name().equalsIgnoreCase(payload.getProvider())) {
            return failRedirect(frontend, "授权状态不匹配");
        }
        try {
            OauthUserProfile profile = oauthClientFacade.loginByCode(type, oauthProvider, code, state);
            if ("BIND".equalsIgnoreCase(payload.getIntent())) {
                bindProfile(payload.getAccountId(), profile);
                return successRedirect(frontend, null, payload.getRedirect(), "bound");
            }
            LoginResult login = loginOrCreate(type, profile);
            return successRedirect(frontend, login, payload.getRedirect(), "login");
        } catch (BizException ex) {
            return failRedirect(frontend, ex.getMessage());
        } catch (Exception ex) {
            return failRedirect(frontend, "三方登录失败");
        }
    }

    @Override
    public LoginResult exchange(String code) {
        return oauthExchangeStore.consume(code);
    }

    @Override
    @Transactional
    public LoginResult loginWechatMp(AccountType accountType, String code) {
        AccountType type = accountType == null ? AccountType.PORTAL : accountType;
        if (type != AccountType.PORTAL) {
            throw new BizException("管理端暂不支持小程序登录");
        }
        OauthUserProfile profile = oauthClientFacade.loginWechatMp(type, code);
        String label = StringUtils.hasText(profile.getNickname())
                ? profile.getNickname()
                : maskOpenId(profile.getOpenId());
        AuditSnapshots.subject(label);
        AuditSnapshots.after(Map.of(
                "提供商", "微信小程序",
                "OpenID", maskOpenId(profile.getOpenId())));
        return loginOrCreate(type, profile);
    }

    @Override
    public List<OauthBindingResult> listCurrentBindings() {
        LoginUser user = LoginHelper.requireUser();
        return toResults(accountOauthApi.listByAccount(user.getAccountId()));
    }

    @Override
    public OauthAuthorizeResult bindAuthorize(AccountType accountType, String provider) {
        LoginUser user = LoginHelper.requireUser();
        OauthProvider oauthProvider = OauthProvider.from(provider);
        String accountLabel = StringUtils.hasText(user.getAccount()) ? user.getAccount() : user.getAccountId();
        AuditSnapshots.subject(accountLabel);
        AuditSnapshots.after(Map.of("提供商", oauthProvider.getLabel()));
        return authorize(accountType, provider, "BIND", null);
    }

    @Override
    @Transactional
    public void unbind(String provider) {
        LoginUser user = LoginHelper.requireUser();
        OauthProvider oauthProvider = OauthProvider.from(provider);
        assertCanUnbind(user.getAccountId(), oauthProvider.name());
        AccountOauthBindingInfo binding = accountOauthApi.listByAccount(user.getAccountId()).stream()
                .filter(item -> oauthProvider.name().equalsIgnoreCase(item.getProvider()))
                .findFirst()
                .orElse(null);
        if (binding != null) {
            AuditSnapshots.deleted(binding);
        }
        AuditSnapshots.subject(user.getAccountId());
        accountOauthApi.unbind(user.getAccountId(), oauthProvider.name());
    }

    @Override
    @Transactional
    public void adminUnbind(String accountId, String provider) {
        if (!StringUtils.hasText(accountId)) {
            throw new BizException("账号 ID 不能为空");
        }
        OauthProvider oauthProvider = OauthProvider.from(provider);
        assertCanUnbind(accountId, oauthProvider.name());
        AccountOauthBindingInfo binding = accountOauthApi.listByAccount(accountId).stream()
                .filter(item -> oauthProvider.name().equalsIgnoreCase(item.getProvider()))
                .findFirst()
                .orElse(null);
        if (binding != null) {
            AuditSnapshots.deleted(binding);
        }
        AuditSnapshots.subject(accountId);
        accountOauthApi.unbind(accountId, oauthProvider.name());
    }

    private LoginResult loginOrCreate(AccountType accountType, OauthUserProfile profile) {
        AccountOauthBindingInfo binding = resolveBinding(profile);
        if (binding != null) {
            AccountInfo account = accountApi.getById(binding.getAccountId());
            if (account == null) {
                throw new BizException("绑定账号不存在");
            }
            // 刷新资料快照
            accountOauthApi.upsertBinding(
                    account.getId(),
                    profile.getProvider(),
                    profile.getOpenId(),
                    profile.getUnionId(),
                    profile.getNickname(),
                    profile.getAvatar(),
                    profile.getRawProfileJson());
            String label = StringUtils.hasText(profile.getNickname())
                    ? profile.getNickname()
                    : accountApi.findIdentifier(account.getId(), "ACCOUNT");
            return authService.issueLoginForAccount(account, accountType, label);
        }

        if (accountType == AccountType.ADMIN) {
            throw new BizException("请先使用账号密码登录后再绑定该三方账号");
        }

        // 门户自动建号
        String accountName = allocateOauthAccountName(profile);
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String rawPassword = "oauth:" + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        AccountInfo created = accountApi.createPortalAccount(
                accountName, null, accountApi.encodePassword(rawPassword));
        accountApi.recordPasswordHistory(created.getId(), rawPassword, created.getId(), "oauth_register");

        String nickname = StringUtils.hasText(profile.getNickname())
                ? profile.getNickname()
                : "user-" + created.getId().substring(Math.max(0, created.getId().length() - 8));
        portalUserProfileApi.createProfile(created.getId(), nickname, null);
        if (StringUtils.hasText(profile.getAvatar())) {
            // 档案头像若需对象存储，此处仅存 URL 字符串由前端展示；createProfile 无 avatar 参数时跳过
        }

        String roleId = configApi.getValue("AUTH_REGISTER_PORTAL_DEFAULT_ROLE_ID", "").trim();
        if (StringUtils.hasText(roleId)) {
            accountApi.assignRole(created.getId(), roleId);
        }
        String deptId = configApi.getValue("AUTH_REGISTER_PORTAL_DEFAULT_DEPT_ID", "").trim();
        if (StringUtils.hasText(deptId)) {
            accountApi.assignPrimaryDept(created.getId(), deptId);
        }

        accountOauthApi.upsertBinding(
                created.getId(),
                profile.getProvider(),
                profile.getOpenId(),
                profile.getUnionId(),
                profile.getNickname(),
                profile.getAvatar(),
                profile.getRawProfileJson());

        return authService.issueLoginForAccount(created, AccountType.PORTAL, accountName);
    }

    private void bindProfile(String accountId, OauthUserProfile profile) {
        if (!StringUtils.hasText(accountId)) {
            throw new BizException("绑定失败：未登录");
        }
        AccountOauthBindingInfo existing = resolveBinding(profile);
        if (existing != null && !accountId.equals(existing.getAccountId())) {
            throw new BizException("该三方账号已绑定其他用户");
        }
        AccountOauthBindingInfo sameProvider = accountOauthApi.listByAccount(accountId).stream()
                .filter(item -> profile.getProvider().equalsIgnoreCase(item.getProvider()))
                .findFirst()
                .orElse(null);
        if (sameProvider != null && !sameProvider.getOpenId().equals(profile.getOpenId())) {
            throw new BizException("已绑定其他 " + profile.getProvider() + " 账号，请先解绑");
        }
        accountOauthApi.upsertBinding(
                accountId,
                profile.getProvider(),
                profile.getOpenId(),
                profile.getUnionId(),
                profile.getNickname(),
                profile.getAvatar(),
                profile.getRawProfileJson());
    }

    private AccountOauthBindingInfo resolveBinding(OauthUserProfile profile) {
        if (OauthProvider.WECHAT_FAMILY.contains(profile.getProvider())
                && StringUtils.hasText(profile.getUnionId())) {
            AccountOauthBindingInfo byUnion = accountOauthApi.findByWechatUnionId(profile.getUnionId());
            if (byUnion != null) {
                return byUnion;
            }
        }
        return accountOauthApi.findByProviderOpenId(profile.getProvider(), profile.getOpenId());
    }

    private void assertCanUnbind(String accountId, String provider) {
        AccountOauthBindingInfo target = accountOauthApi.listByAccount(accountId).stream()
                .filter(item -> provider.equalsIgnoreCase(item.getProvider()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return;
        }
        int oauthCount = accountOauthApi.countByAccount(accountId);
        boolean hasAccount = StringUtils.hasText(accountApi.findIdentifier(accountId, "ACCOUNT"));
        boolean hasEmail = StringUtils.hasText(accountApi.findIdentifier(accountId, "EMAIL"));
        boolean hasPhone = StringUtils.hasText(accountApi.findIdentifier(accountId, "PHONE"));
        int otherLoginWays = (hasAccount ? 1 : 0) + (hasEmail ? 1 : 0) + (hasPhone ? 1 : 0) + Math.max(0, oauthCount - 1);
        // OAuth 自动建号账号通常只有 ACCOUNT + 当前三方；允许保留 ACCOUNT 作为登录手段
        if (otherLoginWays <= 0) {
            throw new BizException("无法解绑：请至少保留一种登录方式");
        }
    }

    private String allocateOauthAccountName(OauthUserProfile profile) {
        String prefix = switch (OauthProvider.from(profile.getProvider())) {
            case GITHUB -> "gh_";
            case GITEE -> "ge_";
            case QQ -> "qq_";
            case WECHAT_OPEN, WECHAT_MP -> "wx_";
        };
        String suffix = profile.getOpenId().replaceAll("[^a-zA-Z0-9]", "");
        if (suffix.length() > 12) {
            suffix = suffix.substring(0, 12);
        }
        if (!StringUtils.hasText(suffix)) {
            suffix = String.valueOf(Math.abs(RANDOM.nextInt(1_000_000)));
        }
        String base = (prefix + suffix).toLowerCase(Locale.ROOT);
        String candidate = base;
        int i = 0;
        while (accountApi.findByIdentifier(candidate, "ACCOUNT") != null) {
            i++;
            candidate = base + i;
        }
        return candidate;
    }

    private List<OauthBindingResult> toResults(List<AccountOauthBindingInfo> bindings) {
        List<OauthBindingResult> list = new ArrayList<>();
        for (AccountOauthBindingInfo binding : bindings) {
            OauthBindingResult row = new OauthBindingResult();
            row.setProvider(binding.getProvider());
            try {
                row.setLabel(OauthProvider.from(binding.getProvider()).getLabel());
            } catch (Exception e) {
                row.setLabel(binding.getProvider());
            }
            row.setOpenIdMasked(maskOpenId(binding.getOpenId()));
            row.setNickname(binding.getNickname());
            row.setAvatar(binding.getAvatar());
            row.setBoundAt(binding.getBoundAt());
            list.add(row);
        }
        return list;
    }

    private static String maskOpenId(String openId) {
        if (!StringUtils.hasText(openId)) {
            return "";
        }
        String value = openId.trim();
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private String frontendCallback(AccountType accountType) {
        String key = accountType == AccountType.ADMIN
                ? "AUTH_OAUTH_FRONTEND_CALLBACK_ADMIN"
                : "AUTH_OAUTH_FRONTEND_CALLBACK_PORTAL";
        String configured = configApi.getValue(key, "").trim();
        String candidate = StringUtils.hasText(configured)
                ? configured
                : joinBaseAndPath(
                        accountType == AccountType.ADMIN ? adminFrontendBaseUrl : portalFrontendBaseUrl,
                        "/auth/oauth/callback");
        if (!isAbsoluteHttpUrl(candidate)) {
            throw new BizException("OAuth 前端回调地址必须为 http(s) 绝对 URL，请配置 "
                    + key + " 或 hei.app."
                    + (accountType == AccountType.ADMIN ? "frontend-base-url" : "portal-frontend-base-url"));
        }
        return candidate;
    }

    private static String joinBaseAndPath(String baseUrl, String path) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String suffix = path == null ? "" : path.trim();
        if (!suffix.startsWith("/")) {
            suffix = "/" + suffix;
        }
        return base + suffix;
    }

    private static boolean isAbsoluteHttpUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private String successRedirect(String frontend, LoginResult login, String redirect, String action) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontend)
                .queryParam("oauth_status", "ok")
                .queryParam("oauth_action", action);
        if (login != null && StringUtils.hasText(login.getToken())) {
            builder.queryParam("oauth_code", oauthExchangeStore.save(login));
        }
        if (StringUtils.hasText(redirect)) {
            builder.queryParam("redirect", redirect);
        }
        return builder.build(true).toUriString();
    }

    private String failRedirect(String frontend, String message) {
        return UriComponentsBuilder.fromUriString(frontend)
                .queryParam("oauth_status", "error")
                .queryParam("oauth_message", URLEncoder.encode(nullToEmpty(message), StandardCharsets.UTF_8))
                .build(true)
                .toUriString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
