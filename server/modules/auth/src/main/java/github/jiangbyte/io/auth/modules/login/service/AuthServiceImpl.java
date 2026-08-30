package github.jiangbyte.io.auth.modules.login.service;

import github.jiangbyte.io.auth.modules.login.param.CancelAccountParam;
import github.jiangbyte.io.auth.modules.login.result.AuthOptionsResult;
import github.jiangbyte.io.auth.modules.login.result.CaptchaResult;
import github.jiangbyte.io.auth.modules.login.result.CurrentUserResult;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ForgotPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.LoginParam;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.result.PasswordKeyResult;
import github.jiangbyte.io.auth.modules.login.param.RegisterParam;
import github.jiangbyte.io.auth.modules.login.result.RegisterResult;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordByPhoneParam;
import github.jiangbyte.io.auth.modules.login.param.ResetPasswordParam;
import github.jiangbyte.io.auth.modules.login.param.SendLoginCodeParam;
import github.jiangbyte.io.auth.modules.login.convert.AuthConvert;
import github.jiangbyte.io.auth.modules.login.support.AuthCryptoService;
import github.jiangbyte.io.common.security.account.AccountLoginSupport;
import github.jiangbyte.io.common.security.web.CsrfDoubleSubmitFilter;
import github.jiangbyte.io.auth.modules.login.support.LoginProtectionService;
import github.jiangbyte.io.auth.modules.login.result.OauthProviderOptionResult;
import github.jiangbyte.io.auth.modules.oauth.support.OauthClientFacade;
import github.jiangbyte.io.auth.modules.oauth.support.OauthProvider;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.DataSourceSticky;
import github.jiangbyte.io.common.notify.mail.MailSenderFacade;
import github.jiangbyte.io.common.notify.sms.SmsSenderFacade;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.util.IdUtil;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.account.AccountApi;
import github.jiangbyte.io.iam.account.AccountAuthorizationInfo;
import github.jiangbyte.io.iam.account.AccountInfo;
import github.jiangbyte.io.iam.account.PermissionGrantInfo;
import github.jiangbyte.io.iam.password.PasswordPolicyApi;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.sys.config.ConfigAppNames;
import github.jiangbyte.io.sys.config.SiteFooterConfig;
import github.jiangbyte.io.sys.config.SiteFooterResult;
import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * {@link AuthService} 实现：整合验证码/加解密、账号 IAM、登录保护、通知发送与 Sa-Token 会话签发。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_OTP_TTL_SECONDS = 300;

    private final AuthCryptoService cryptoService;
    private final AccountApi accountApi;
    private final ProfileUserAdminApi adminUserProfileApi;
    private final ProfileUserPortalApi portalUserProfileApi;
    private final MailSenderFacade mailSenderFacade;
    private final SmsSenderFacade smsSenderFacade;
    private final PasswordPolicyApi passwordPolicyApi;
    private final LoginProtectionService loginProtectionService;
    private final ConfigApi configApi;
    private final AuthConvert authConvert;

    @Override
    public CaptchaResult captcha(String format) {
        return cryptoService.createCaptcha(format == null ? "svg" : format);
    }

    @Override
    public PasswordKeyResult passwordKey() {
        return cryptoService.createPasswordKey();
    }

    @Override
    public AuthOptionsResult authOptions(AccountType accountType) {
        AccountType type = accountType == null ? AccountType.ADMIN : accountType;
        String typeName = type.name();
        AuthOptionsResult result = new AuthOptionsResult();
        result.setAccountType(type);
        result.setAllowAccount(true);
        // 按账号类型读取登录/注册相关开关
        result.setAllowEmail(configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_EMAIL", true));
        result.setAllowPhone(configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_PHONE", true));
        result.setAllowOtp(configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_OTP", true));
        boolean defaultRegister = type == AccountType.PORTAL;
        result.setRegisterEnabled(configApi.getBoolean("AUTH_REGISTER_" + typeName + "_ENABLED", defaultRegister));
        if (type == AccountType.PORTAL) {
            result.setRegisterAllowAccount(configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_ACCOUNT", true));
            result.setRegisterAllowEmail(configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_EMAIL", true));
            result.setRegisterAllowPhone(configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_PHONE", false));
        } else {
            result.setRegisterAllowAccount(false);
            result.setRegisterAllowEmail(false);
            result.setRegisterAllowPhone(false);
        }
        result.setForceBindEmail(configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_EMAIL", false));
        result.setForceBindPhone(configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_PHONE", false));
        result.setRegisterRequireEmail(configApi.getBoolean(
                "AUTH_REGISTER_" + typeName + "_REQUIRE_EMAIL", type == AccountType.PORTAL));
        result.setRegisterRequirePhone(configApi.getBoolean(
                "AUTH_REGISTER_" + typeName + "_REQUIRE_PHONE", false));
        result.setOauthProviders(buildOauthProviderOptions(type));
        result.setPasswordChangeVerifyMethod(
                configApi.getValue("PASSWORD_CHANGE_VERIFY_METHOD", "OLD_PASSWORD").trim().toUpperCase(Locale.ROOT));
        SiteFooterResult siteFooter = SiteFooterConfig.resolve(configApi);
        result.setSiteFooter(siteFooter);
        result.setCopyrightText(siteFooter.getCopyrightText());
        result.setCopyrightUrl(siteFooter.getCopyrightUrl());
        return result;
    }

    private java.util.List<OauthProviderOptionResult> buildOauthProviderOptions(AccountType type) {
        java.util.List<OauthProviderOptionResult> list = new java.util.ArrayList<>();
        for (OauthProvider provider : OauthProvider.values()) {
            // 管理端隐藏小程序入口
            if (type == AccountType.ADMIN && provider == OauthProvider.WECHAT_MP) {
                continue;
            }
            OauthProviderOptionResult item = new OauthProviderOptionResult();
            item.setProvider(provider.name());
            item.setLabel(provider.getLabel());
            item.setWebOAuth(provider.isWebOAuth());
            item.setEnabled(configApi.getBoolean(OauthClientFacade.configKey(type, provider, "ENABLED"), false));
            list.add(item);
        }
        return list;
    }

    @Override
    public LoginResult issueLoginForAccount(AccountInfo account, AccountType accountType, String loginLabel) {
        if (account == null || !"ENABLED".equalsIgnoreCase(account.getAccountStatus())) {
            throw new BizException(401, "账号不可用");
        }
        if (!accountType.name().equalsIgnoreCase(account.getAccountType())) {
            throw new BizException(401, "账号类型不匹配");
        }
        String label = StringUtils.hasText(loginLabel) ? loginLabel : accountApi.findIdentifier(account.getId(), "ACCOUNT");
        return issueSession(account, accountType, true, label);
    }

    @Override
    public void sendLoginCode(SendLoginCodeParam request, AccountType accountType) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        AccountType type = accountType == null ? AccountType.ADMIN : accountType;
        String channel = normalizeChannel(request.getChannel());
        String identityType = "EMAIL".equals(channel) ? "EMAIL" : "PHONE";
        ensureIdentityAllowed(type, identityType, "OTP");
        String normalized = normalizeTarget(channel, request.getTarget());
        AccountInfo account = accountApi.findByIdentifier(normalized, identityType);
        if (account == null) {
            // 非 AUTO_CREATE 时静默返回，避免枚举账号
            String policy = noUserPolicy(type, identityType);
            if (!"AUTO_CREATE".equals(policy)) {
                return;
            }
        }
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeLoginOtp(type.name(), channel, normalized, code, ttl);
        sendCodeMailOrSms(channel, normalized, "LOGIN_CODE", code, ttl);
    }

    @Override
    public void sendRegisterCode(SendLoginCodeParam request) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        if (!configApi.getBoolean("AUTH_REGISTER_PORTAL_ENABLED", true)) {
            throw new BizException("门户注册已关闭");
        }
        String channel = normalizeChannel(request.getChannel());
        ensureRegisterChannelAllowed(channel);
        String normalized = normalizeTarget(channel, request.getTarget());
        String identityType = "EMAIL".equals(channel) ? "EMAIL" : "PHONE";
        if (accountApi.findByIdentifier(normalized, identityType) != null) {
            throw new BizException("EMAIL".equals(channel) ? "邮箱已被使用" : "手机号已被使用");
        }
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeRegisterOtp(channel, normalized, code, ttl);
        sendCodeMailOrSms(channel, normalized, "LOGIN_CODE", code, ttl);
    }

    @Override
    public LoginResult login(LoginParam request) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        AuditSnapshots.subject(request.getAccount());
        AccountType accountType = request.getAccountType() == null ? AccountType.ADMIN : request.getAccountType();
        String identityType = StringUtils.hasText(request.getIdentityType()) ? request.getIdentityType() : "ACCOUNT";
        String loginMode = StringUtils.hasText(request.getLoginMode())
                ? request.getLoginMode().trim().toUpperCase(Locale.ROOT)
                : "PASSWORD";
        String clientIp = currentClientIp();
        // 登录前检查账号/IP 是否被锁定
        loginProtectionService.ensureAllowed(accountType, request.getAccount(), clientIp);
        ensureIdentityAllowed(accountType, identityType, loginMode);

        try {
            AccountInfo account;
            if ("OTP".equals(loginMode)) {
                // OTP 登录：消费验证码后按邮箱/手机查账号
                verifyLoginOtp(request, accountType, identityType);
                account = accountApi.findByIdentifier(
                        normalizeLoginAccount(identityType, request.getAccount()), identityType);
                if (account == null
                        || !"ENABLED".equalsIgnoreCase(account.getAccountStatus())
                        || !accountType.name().equalsIgnoreCase(account.getAccountType())) {
                    throw new BizException(401, "账号或密码错误");
                }
            } else {
                // 密码登录：解密传输密文并校验哈希
                if (!StringUtils.hasText(request.getPassword()) || !StringUtils.hasText(request.getPasswordKeyId())) {
                    throw new BizException("请输入密码");
                }
                String rawPassword = cryptoService.decryptPassword(request.getPasswordKeyId(), request.getPassword());
                account = accountApi.findByIdentifier(request.getAccount(), identityType);
                if (account == null
                        || !"ENABLED".equalsIgnoreCase(account.getAccountStatus())
                        || !accountApi.matchesPassword(rawPassword, account.getPasswordHash())) {
                    throw new BizException(401, "账号或密码错误");
                }
                if (!accountType.name().equalsIgnoreCase(account.getAccountType())) {
                    throw new BizException(401, "账号类型不匹配");
                }
            }
            LoginResult result = issueSession(account, accountType, request.getRememberMe(), request.getAccount());
            AuditSnapshots.resourceId(account.getId());
            loginProtectionService.recordSuccess(accountType, request.getAccount(), clientIp);
            return result;
        } catch (BizException ex) {
            loginProtectionService.recordFailure(accountType, request.getAccount(), clientIp);
            throw ex;
        }
    }

    @Override
    @Transactional
    public RegisterResult registerPortal(RegisterParam request) {
        if (!configApi.getBoolean("AUTH_REGISTER_PORTAL_ENABLED", true)) {
            throw new BizException("门户注册已关闭");
        }
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        String channel = normalizeRegisterChannel(request.getRegisterChannel());
        ensureRegisterChannelAllowed(channel);

        String accountName;
        String email = null;
        String phone = null;
        if ("ACCOUNT".equals(channel)) {
            accountName = requireAccountName(request.getAccount());
            if (accountApi.findByIdentifier(accountName, "ACCOUNT") != null) {
                throw new BizException("账号已存在");
            }
            // 策略要求联系方式时，ACCOUNT 通道需在载荷中补齐（缺失则拒绝）
            email = StringUtils.hasText(request.getEmail())
                    ? request.getEmail().trim().toLowerCase(Locale.ROOT)
                    : null;
            phone = StringUtils.hasText(request.getPhone())
                    ? request.getPhone().trim()
                    : null;
            if (configApi.getBoolean("AUTH_REGISTER_PORTAL_REQUIRE_EMAIL", true)
                    && !StringUtils.hasText(email)) {
                throw new BizException("注册必填邮箱");
            }
            if (configApi.getBoolean("AUTH_REGISTER_PORTAL_REQUIRE_PHONE", false)
                    && !StringUtils.hasText(phone)) {
                throw new BizException("注册必填手机号");
            }
        } else if ("EMAIL".equals(channel)) {
            email = normalizeTarget("EMAIL", request.getEmail());
            if (!StringUtils.hasText(request.getOtpCode())) {
                throw new BizException("请输入邮箱验证码");
            }
            if (!cryptoService.consumeRegisterOtp("EMAIL", email, request.getOtpCode().trim())) {
                throw new BizException("验证码无效或已过期");
            }
            if (accountApi.findByIdentifier(email, "EMAIL") != null) {
                throw new BizException("邮箱已被使用");
            }
            accountName = allocateAccountFromEmail(email);
        } else {
            phone = normalizeTarget("PHONE", request.getPhone());
            if (!StringUtils.hasText(request.getOtpCode())) {
                throw new BizException("请输入手机验证码");
            }
            if (!cryptoService.consumeRegisterOtp("PHONE", phone, request.getOtpCode().trim())) {
                throw new BizException("验证码无效或已过期");
            }
            if (accountApi.findByIdentifier(phone, "PHONE") != null) {
                throw new BizException("手机号已被使用");
            }
            accountName = allocateAccountFromPhone(phone);
        }

        String rawPassword = cryptoService.decryptPassword(request.getPasswordKeyId(), request.getPassword());
        passwordPolicyApi.assertValid(rawPassword, null, accountName, email, phone);

        AccountInfo account = accountApi.createPortalAccount(
                accountName,
                email,
                accountApi.encodePassword(rawPassword));
        if (StringUtils.hasText(phone)) {
            accountApi.upsertIdentity(account.getId(), "PHONE", phone, true);
        }

        String nickname = "user-" + account.getId().substring(Math.max(0, account.getId().length() - 8));
        portalUserProfileApi.createProfile(account.getId(), nickname, email);
        if (StringUtils.hasText(phone)) {
            portalUserProfileApi.updatePhone(account.getId(), phone);
        }
        accountApi.recordPasswordHistory(account.getId(), rawPassword, account.getId(), "register");

        AuditSnapshots.created(account);
        AuditSnapshots.subject(accountName);

        // 按配置挂接默认角色/部门
        String roleId = configApi.getValue("AUTH_REGISTER_PORTAL_DEFAULT_ROLE_ID", "").trim();
        if (StringUtils.hasText(roleId)) {
            accountApi.assignRole(account.getId(), roleId);
        }
        String deptId = configApi.getValue("AUTH_REGISTER_PORTAL_DEFAULT_DEPT_ID", "").trim();
        if (StringUtils.hasText(deptId)) {
            accountApi.assignPrimaryDept(account.getId(), deptId);
        }

        if (StringUtils.hasText(email)) {
            try {
                mailSenderFacade.sendTemplated("REGISTER_SUCCESS", email, Map.of(
                        "app_name", appName(),
                        "account", accountName));
            } catch (Exception ignored) {
                // 尽力发送生命周期邮件
            }
        }
        if (StringUtils.hasText(phone)) {
            try {
                smsSenderFacade.sendTemplated("REGISTER_SUCCESS", phone, Map.of(
                        "app_name", appName(),
                        "account", accountName));
            } catch (Exception ignored) {
                // 尽力发送生命周期短信
            }
        }

        return authConvert.toRegisterResponse(account.getId(), accountName, AccountType.PORTAL);
    }

    @Override
    public void forgotPassword(ForgotPasswordParam request, AccountType accountType) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        AuditSnapshots.subject(email);
        AccountInfo account = accountApi.findByIdentifier(email, "EMAIL");
        // 账号不存在或类型不匹配时静默返回，避免枚举
        if (account == null || !accountType.name().equalsIgnoreCase(account.getAccountType())) {
            return;
        }
        long ttlSeconds = Math.max(60L, configApi.getLong("AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS", 600));
        String token = IdUtil.simpleUUID();
        cryptoService.storeResetToken(token, account.getId(), Duration.ofSeconds(ttlSeconds));
        String resetLink = buildPasswordResetLink(token, accountType);
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("app_name", appName());
        vars.put("reset_link", resetLink);
        vars.put("email", email);
        vars.put("expire_minutes", String.valueOf(Math.max(1, ttlSeconds / 60)));
        mailSenderFacade.sendTemplated("RESET_PASSWORD_CODE", email, vars);
    }

    @Override
    public void forgotPasswordByPhone(ForgotPasswordByPhoneParam request, AccountType accountType) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        String phone = normalizeTarget("PHONE", request.getPhone());
        AuditSnapshots.subject(phone);
        AccountInfo account = accountApi.findByIdentifier(phone, "PHONE");
        if (account == null || !accountType.name().equalsIgnoreCase(account.getAccountType())) {
            return;
        }
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeResetPasswordOtp(accountType.name(), phone, code, ttl);
        sendCodeMailOrSms("PHONE", phone, "RESET_PASSWORD_CODE", code, ttl);
    }

    private String buildPasswordResetLink(String token, AccountType accountType) {
        AccountType type = accountType == null ? AccountType.ADMIN : accountType;
        String key = "AUTH_PASSWORD_RESET_URL_" + type.name();
        String base = configApi.getValue(key, "").trim();
        if (!StringUtils.hasText(base)) {
            throw new BizException("缺少系统配置: " + key);
        }
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordParam request, AccountType accountType) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        String rawPassword = cryptoService.decryptPassword(request.getPasswordKeyId(), request.getPassword());
        // 消费一次性重置令牌
        String accountId = cryptoService.consumeResetToken(request.getToken());
        if (!StringUtils.hasText(accountId)) {
            throw new BizException("重置令牌无效或已过期");
        }
        AccountInfo account = accountApi.getById(accountId);
        if (account == null || !accountType.name().equalsIgnoreCase(account.getAccountType())) {
            throw new BizException("重置令牌无效");
        }
        String accountName = accountApi.findIdentifier(accountId, "ACCOUNT");
        AuditSnapshots.before(account);
        AuditSnapshots.subject(StringUtils.hasText(accountName) ? accountName : accountId);
        String email = accountApi.findIdentifier(accountId, "EMAIL");
        String phone = accountApi.findIdentifier(accountId, "PHONE");
        passwordPolicyApi.assertValid(rawPassword, accountId, accountName, email, phone);
        accountApi.updatePasswordHash(accountId, accountApi.encodePassword(rawPassword));
        accountApi.recordPasswordHistory(accountId, rawPassword, accountId, "self_reset");
        AccountInfo updated = accountApi.getById(accountId);
        AuditSnapshots.after(updated != null ? updated : account);
        notifyPasswordResetSuccess(accountId, accountName, email, phone);
    }

    @Override
    @Transactional
    public void resetPasswordByPhone(ResetPasswordByPhoneParam request, AccountType accountType) {
        cryptoService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaValue());
        String phone = normalizeTarget("PHONE", request.getPhone());
        String otpCode = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        if (!cryptoService.consumeResetPasswordOtp(accountType.name(), phone, otpCode)) {
            throw new BizException("验证码无效或已过期");
        }
        AccountInfo account = accountApi.findByIdentifier(phone, "PHONE");
        if (account == null || !accountType.name().equalsIgnoreCase(account.getAccountType())) {
            throw new BizException("账号不存在");
        }
        String rawPassword = cryptoService.decryptPassword(request.getPasswordKeyId(), request.getPassword());
        String accountId = account.getId();
        String accountName = accountApi.findIdentifier(accountId, "ACCOUNT");
        AuditSnapshots.before(account);
        AuditSnapshots.subject(StringUtils.hasText(accountName) ? accountName : accountId);
        String email = accountApi.findIdentifier(accountId, "EMAIL");
        passwordPolicyApi.assertValid(rawPassword, accountId, accountName, email, phone);
        accountApi.updatePasswordHash(accountId, accountApi.encodePassword(rawPassword));
        accountApi.recordPasswordHistory(accountId, rawPassword, accountId, "self_reset_phone");
        AccountInfo updated = accountApi.getById(accountId);
        AuditSnapshots.after(updated != null ? updated : account);
        notifyPasswordResetSuccess(accountId, accountName, email, phone);
    }

    @Override
    public CurrentUserResult currentUser() {
        return LoginHelper.currentUser()
                .map(authConvert::toCurrentUser)
                .orElseThrow(() -> new BizException(401, "未登录"));
    }

    @Override
    public void logout() {
        LoginHelper.currentUser().ifPresent(user -> {
            AuditSnapshots.subject(StringUtils.hasText(user.getAccount()) ? user.getAccount() : user.getAccountId());
            AuditSnapshots.resourceId(user.getAccountId());
        });
        LoginHelper.currentUser().ifPresent(user -> LoginHelper.logout(user.getAccountType()));
        clearCsrfCookie();
    }

    @Override
    public LoginResult refreshSession() {
        LoginUser user = LoginHelper.requireUser();
        AccountInfo account = accountApi.getById(user.getAccountId());
        if (account == null) {
            throw new BizException(401, "账号不存在或已失效");
        }
        AccountAuthorizationInfo authorization = accountApi.getAuthorization(account.getId());
        int expireDays = configApi.getInt("PASSWORD_VALIDITY_DAYS", 90);
        boolean passwordExpired = accountApi.isPasswordExpired(account.getId(), expireDays);

        user.setRoles(authorization.roleSet());
        user.setPermissions(authorization.permissionSet());
        user.setClientPermissions(authorization.clientPermissionSet());
        user.setClientResources(authorization.clientResourceSet());
        user.setRoleIds(authorization.getRoleIds());
        user.setDeptIds(authorization.getDeptIds());
        user.setGroupIds(authorization.getGroupIds());
        user.setResourceIds(List.of());
        user.setButtonCodes(List.of());
        user.setPermissionGrants(PermissionGrantInfo.toLoginGrants(authorization.getPermissionGrants()));
        user.setPasswordExpired(passwordExpired);

        StpLogic logic = LoginHelper.stpLogic(user.getAccountType());
        logic.getTokenSession().set(LoginHelper.LOGIN_USER_KEY, user);

        long renewTtl = configApi.getLong("AUTH_TOKEN_TTL_SECONDS", 0);
        if (renewTtl <= 0) {
            renewTtl = logic.getTokenTimeout();
        }
        if (renewTtl > 0) {
            logic.renewTimeout(renewTtl);
        }
        LoginResult response = authConvert.toLoginResponse(
                user.getAccountId(), user.getAccountType(), user.isPasswordExpired());
        response.setToken(logic.getTokenValue());
        response.setExpiresIn(logic.getTokenTimeout());
        response.setPasswordExpiryWarningDays(passwordExpiryWarningDays(user.getAccountId()));
        applyForceBindFlags(response, user.getAccountType(), user.getAccountId());
        return response;
    }

    @Override
    @Transactional
    public void cancelAccount(CancelAccountParam request) {
        LoginUser loginUser = LoginHelper.currentUser()
                .orElseThrow(() -> new BizException(401, "未登录"));
        AccountInfo account = accountApi.getById(loginUser.getAccountId());
        if (account == null) {
            throw new BizException(404, "账号不存在");
        }
        AuditSnapshots.before(account);
        AuditSnapshots.subject(accountApi.findIdentifier(account.getId(), "ACCOUNT"));
        // 标记注销后立即踢出当前会话
        accountApi.cancelAccount(
                loginUser.getAccountId(),
                loginUser.getAccountId(),
                request == null ? null : request.getCancelReason());
        AccountInfo updated = accountApi.getById(loginUser.getAccountId());
        AuditSnapshots.after(updated != null ? updated : account);
        LoginHelper.logout(loginUser.getAccountType());
    }

    @Override
    public void sendChangePasswordCode() {
        LoginUser loginUser = LoginHelper.requireUser();
        // 解析改密校验方式与联系人
        String method = changeVerifyMethod();
        if (!"EMAIL_CODE".equals(method) && !"PHONE_CODE".equals(method)) {
            throw new BizException("当前改密方式不使用验证码");
        }
        String identityType = "EMAIL_CODE".equals(method) ? "EMAIL" : "PHONE";
        String channel = "EMAIL_CODE".equals(method) ? "EMAIL" : "PHONE";
        String target = accountApi.findIdentifier(loginUser.getAccountId(), identityType);
        if (!StringUtils.hasText(target)) {
            throw new BizException("账号未绑定可用于校验的联系方式");
        }
        // 生成 OTP 写入缓存
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeChangePasswordOtp(loginUser.getAccountType().name(), channel, loginUser.getAccountId(), code, ttl);
        // 发送邮件或短信
        sendCodeMailOrSms(channel, target, "CHANGE_PASSWORD_CODE", code, ttl);
    }

    @Transactional
    @Override
    public void updateCurrentPassword(String passwordKeyId, String oldPassword, String newPassword, String otpCode) {
        LoginUser loginUser = LoginHelper.requireUser();
        // 解密旧/新密码
        String[] decrypted = cryptoService.decryptPasswords(passwordKeyId, oldPassword, newPassword);
        String rawOld = decrypted[0];
        String rawNew = decrypted[1];
        if (!StringUtils.hasText(rawNew)) {
            throw new BizException("请输入新密码");
        }
        // 加载账号并校验旧密码或 OTP
        AccountInfo account = accountApi.getById(loginUser.getAccountId());
        if (account == null) {
            throw new BizException(404, "账号不存在");
        }
        AuditSnapshots.before(account);
        verifyChangePassword(account, loginUser.getAccountType(), rawOld, otpCode);
        // 校验密码策略后更新哈希与历史
        String accountName = accountApi.findIdentifier(account.getId(), "ACCOUNT");
        String email = accountApi.findIdentifier(account.getId(), "EMAIL");
        String phone = accountApi.findIdentifier(account.getId(), "PHONE");
        passwordPolicyApi.assertValid(rawNew, account.getId(), accountName, email, phone);
        accountApi.updatePasswordHash(account.getId(), accountApi.encodePassword(rawNew));
        accountApi.recordPasswordHistory(account.getId(), rawNew, account.getId(), "self_update");
        AccountInfo updated = accountApi.getById(account.getId());
        AuditSnapshots.after(updated != null ? updated : account);
    }

    @Transactional
    @Override
    public void updateCurrentPhone(String passwordKeyId, String password, String phone, boolean phoneLoginEnabled, String otpCode) {
        LoginUser loginUser = LoginHelper.requireUser();
        // 解密并校验登录密码
        String rawPassword = cryptoService.decryptPassword(passwordKeyId, password);
        AccountInfo account = accountApi.getById(loginUser.getAccountId());
        if (account == null) {
            throw new BizException(404, "账号不存在");
        }
        if (!accountApi.matchesPassword(rawPassword, account.getPasswordHash())) {
            throw new BizException("密码错误");
        }
        AuditSnapshots.before(account);
        if (phoneLoginEnabled && !StringUtils.hasText(phone)) {
            throw new BizException("开启手机登录需填写手机号");
        }
        String normalized = StringUtils.hasText(phone) ? phone.trim() : null;
        String current = accountApi.findIdentifier(account.getId(), "PHONE");
        boolean needsOtp = StringUtils.hasText(normalized) && !normalized.equals(nullToEmpty(current));
        if (needsOtp) {
            if (!StringUtils.hasText(otpCode)) {
                throw new BizException("请输入手机验证码");
            }
            if (!cryptoService.consumeBindOtp(
                    loginUser.getAccountType().name(), "PHONE", account.getId(), normalized, otpCode.trim())) {
                throw new BizException("验证码无效或已过期");
            }
        }
        // 更新手机身份
        accountApi.upsertIdentity(account.getId(), "PHONE", normalized, phoneLoginEnabled);
        // 同步档案手机号
        if (loginUser.getAccountType() == AccountType.ADMIN) {
            adminUserProfileApi.updatePhone(loginUser.getAccountId(), normalized);
        } else {
            portalUserProfileApi.updatePhone(loginUser.getAccountId(), normalized);
        }
        AccountInfo updated = accountApi.getById(account.getId());
        AuditSnapshots.after(updated != null ? updated : account);
    }

    @Transactional
    @Override
    public void updateCurrentEmail(String passwordKeyId, String password, String email, boolean emailLoginEnabled, String otpCode) {
        LoginUser loginUser = LoginHelper.requireUser();
        // 解密并校验登录密码
        String rawPassword = cryptoService.decryptPassword(passwordKeyId, password);
        AccountInfo account = accountApi.getById(loginUser.getAccountId());
        if (account == null) {
            throw new BizException(404, "账号不存在");
        }
        if (!accountApi.matchesPassword(rawPassword, account.getPasswordHash())) {
            throw new BizException("密码错误");
        }
        AuditSnapshots.before(account);
        if (emailLoginEnabled && !StringUtils.hasText(email)) {
            throw new BizException("开启邮箱登录需填写邮箱");
        }
        // 更新邮箱身份
        String normalized = StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : null;
        String current = accountApi.findIdentifier(account.getId(), "EMAIL");
        boolean needsOtp = StringUtils.hasText(normalized) && !normalized.equalsIgnoreCase(nullToEmpty(current));
        if (needsOtp) {
            if (!StringUtils.hasText(otpCode)) {
                throw new BizException("请输入邮箱验证码");
            }
            if (!cryptoService.consumeBindOtp(
                    loginUser.getAccountType().name(), "EMAIL", account.getId(), normalized, otpCode.trim())) {
                throw new BizException("验证码无效或已过期");
            }
        }
        accountApi.upsertIdentity(account.getId(), "EMAIL", normalized, emailLoginEnabled);
        // 同步档案邮箱
        if (loginUser.getAccountType() == AccountType.ADMIN) {
            adminUserProfileApi.updateEmail(loginUser.getAccountId(), normalized);
        } else {
            portalUserProfileApi.updateEmail(loginUser.getAccountId(), normalized);
        }
        AccountInfo updated = accountApi.getById(account.getId());
        AuditSnapshots.after(updated != null ? updated : account);
    }

    @Override
    public void sendBindEmailCode(String target) {
        LoginUser loginUser = LoginHelper.requireUser();
        String normalized = normalizeTarget("EMAIL", target);
        AccountInfo other = accountApi.findByIdentifier(normalized, "EMAIL");
        if (other != null && !loginUser.getAccountId().equals(other.getId())) {
            throw new BizException("邮箱已被使用");
        }
        String current = accountApi.findIdentifier(loginUser.getAccountId(), "EMAIL");
        String scene = StringUtils.hasText(current) ? "CHANGE_EMAIL_CODE" : "BIND_EMAIL_CODE";
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeBindOtp(loginUser.getAccountType().name(), "EMAIL", loginUser.getAccountId(), normalized, code, ttl);
        sendCodeMailOrSms("EMAIL", normalized, scene, code, ttl);
    }

    @Override
    public void sendBindPhoneCode(String target) {
        LoginUser loginUser = LoginHelper.requireUser();
        String normalized = normalizeTarget("PHONE", target);
        AccountInfo other = accountApi.findByIdentifier(normalized, "PHONE");
        if (other != null && !loginUser.getAccountId().equals(other.getId())) {
            throw new BizException("手机号已被使用");
        }
        String current = accountApi.findIdentifier(loginUser.getAccountId(), "PHONE");
        String scene = StringUtils.hasText(current) ? "CHANGE_PHONE_CODE" : "BIND_PHONE_CODE";
        String code = sixDigitCode();
        Duration ttl = otpTtl();
        cryptoService.storeBindOtp(loginUser.getAccountType().name(), "PHONE", loginUser.getAccountId(), normalized, code, ttl);
        sendCodeMailOrSms("PHONE", normalized, scene, code, ttl);
    }

    private LoginResult issueSession(AccountInfo account, AccountType accountType, Boolean rememberMe, String loginAccount) {
        // 加载授权快照并组装 LoginUser
        AccountAuthorizationInfo authorization = accountApi.getAuthorization(account.getId());
        int expireDays = configApi.getInt("PASSWORD_VALIDITY_DAYS", 90);
        boolean passwordExpired = accountApi.isPasswordExpired(account.getId(), expireDays);

        LoginUser loginUser = new LoginUser();
        loginUser.setAccountId(account.getId());
        loginUser.setAccount(loginAccount);
        loginUser.setAccountType(accountType);
        loginUser.setRoles(authorization.roleSet());
        loginUser.setPermissions(authorization.permissionSet());
        loginUser.setClientPermissions(authorization.clientPermissionSet());
        loginUser.setClientResources(authorization.clientResourceSet());
        loginUser.setRoleIds(authorization.getRoleIds());
        loginUser.setDeptIds(authorization.getDeptIds());
        loginUser.setGroupIds(authorization.getGroupIds());
        // 菜单/按钮资源不进会话，按需走资源 API / 授权回源
        loginUser.setResourceIds(List.of());
        loginUser.setButtonCodes(List.of());
        loginUser.setPermissionGrants(PermissionGrantInfo.toLoginGrants(authorization.getPermissionGrants()));
        loginUser.setRememberMe(rememberMe == null || rememberMe);
        loginUser.setPasswordExpired(passwordExpired);
        HttpServletRequest httpRequest = currentRequest();
        if (httpRequest != null) {
            loginUser.setClientIp(httpRequest.getRemoteAddr());
            loginUser.setUserAgent(httpRequest.getHeader("User-Agent"));
            loginUser.setDeviceLabel(deviceLabel(httpRequest.getHeader("User-Agent")));
        }
        DataSourceSticky.mark();
        LoginHelper.login(loginUser, resolveTokenTtlSeconds());
        issueCsrfCookie(loginUser.isRememberMe());

        accountApi.updateLoginMeta(
                account.getId(),
                loginUser.getClientIp(),
                OffsetDateTime.now(),
                loginUser.getDeviceLabel());

        LoginResult response = authConvert.toLoginResponse(account.getId(), accountType, passwordExpired);
        response.setToken(LoginHelper.stpLogic(accountType).getTokenValue());
        response.setExpiresIn(LoginHelper.stpLogic(accountType).getTokenTimeout());
        response.setPasswordExpiryWarningDays(passwordExpiryWarningDays(account.getId()));
        applyForceBindFlags(response, accountType, account.getId());
        maybeNotifyPasswordExpiring(account);
        return response;
    }

    private void applyForceBindFlags(LoginResult response, AccountType accountType, String accountId) {
        AccountType type = accountType == null ? AccountType.ADMIN : accountType;
        String typeName = type.name();
        boolean forceEmail = configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_EMAIL", false)
                && !StringUtils.hasText(accountApi.findIdentifier(accountId, "EMAIL"));
        boolean forcePhone = configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_PHONE", false)
                && !StringUtils.hasText(accountApi.findIdentifier(accountId, "PHONE"));
        response.setForceBindEmail(forceEmail);
        response.setForceBindPhone(forcePhone);
    }

    private void ensureRegisterChannelAllowed(String channel) {
        if ("ACCOUNT".equals(channel) && !configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_ACCOUNT", true)) {
            throw new BizException("用户名注册已关闭");
        }
        if ("EMAIL".equals(channel) && !configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_EMAIL", true)) {
            throw new BizException("邮箱注册已关闭");
        }
        if ("PHONE".equals(channel) && !configApi.getBoolean("AUTH_REGISTER_PORTAL_ALLOW_PHONE", false)) {
            throw new BizException("手机注册已关闭");
        }
    }

    private static String normalizeRegisterChannel(String channel) {
        if (!StringUtils.hasText(channel)) {
            throw new BizException("请选择注册通道");
        }
        String value = channel.trim().toUpperCase(Locale.ROOT);
        if (!"ACCOUNT".equals(value) && !"EMAIL".equals(value) && !"PHONE".equals(value)) {
            throw new BizException("不支持的注册通道");
        }
        return value;
    }

    private static String requireAccountName(String account) {
        return AccountLoginSupport.requireLogin(account);
    }

    private String allocateAccountFromEmail(String email) {
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String base = AccountLoginSupport.sanitizeBase(local);
        return allocateUniqueAccount(base);
    }

    private String allocateAccountFromPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        String base = "u" + (digits.length() > 8 ? digits.substring(digits.length() - 8) : digits);
        return allocateUniqueAccount(base);
    }

    private String allocateUniqueAccount(String base) {
        String candidate = base;
        int suffix = 0;
        while (accountApi.findByIdentifier(candidate, "ACCOUNT") != null) {
            suffix++;
            candidate = base + suffix;
            if (candidate.length() > 64) {
                candidate = base.substring(0, Math.max(3, 64 - String.valueOf(suffix).length())) + suffix;
            }
        }
        return candidate;
    }

    private Integer passwordExpiryWarningDays(String accountId) {
        int warning = configApi.getInt("PASSWORD_EXPIRY_WARNING_DAYS", 0);
        int expireDays = configApi.getInt("PASSWORD_VALIDITY_DAYS", 0);
        if (warning <= 0 || expireDays <= 0) {
            return null;
        }
        Integer age = accountApi.getPasswordAgeDays(accountId);
        if (age == null) {
            return null;
        }
        int remaining = expireDays - age;
        if (remaining > 0 && remaining <= warning) {
            return remaining;
        }
        return null;
    }

    private void verifyLoginOtp(LoginParam request, AccountType accountType, String identityType) {
        String code = request.getOtpCode() == null ? "" : request.getOtpCode().trim();
        if (!StringUtils.hasText(code)) {
            throw new BizException(401, "验证码无效或已过期");
        }
        String channel;
        if ("EMAIL".equalsIgnoreCase(identityType)) {
            channel = "EMAIL";
        } else if ("PHONE".equalsIgnoreCase(identityType)) {
            channel = "PHONE";
        } else {
            throw new BizException("OTP 登录需使用邮箱或手机");
        }
        String normalized = normalizeLoginAccount(identityType, request.getAccount());
        if (!cryptoService.consumeLoginOtp(accountType.name(), channel, normalized, code)) {
            throw new BizException(401, "验证码无效或已过期");
        }
    }

    private void verifyChangePassword(AccountInfo account, AccountType accountType, String oldPassword, String otpCode) {
        String method = changeVerifyMethod();
        if ("OLD_PASSWORD".equals(method)) {
            if (!StringUtils.hasText(oldPassword) || !accountApi.matchesPassword(oldPassword, account.getPasswordHash())) {
                throw new BizException("旧密码不正确");
            }
            return;
        }
        if ("EMAIL_CODE".equals(method) || "PHONE_CODE".equals(method)) {
            if (!StringUtils.hasText(otpCode)) {
                throw new BizException("请输入验证码");
            }
            String channel = "EMAIL_CODE".equals(method) ? "EMAIL" : "PHONE";
            if (!cryptoService.consumeChangePasswordOtp(accountType.name(), channel, account.getId(), otpCode.trim())) {
                throw new BizException("验证码无效或已过期");
            }
            return;
        }
        throw new BizException("不支持的改密校验方式: " + method);
    }

    private void ensureIdentityAllowed(AccountType accountType, String identityType, String loginMode) {
        String typeName = accountType.name();
        String mode = StringUtils.hasText(loginMode) ? loginMode.trim().toUpperCase(Locale.ROOT) : "PASSWORD";
        if ("OTP".equals(mode) && !configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_OTP", true)) {
            throw new BizException("OTP 登录已关闭");
        }
        if ("EMAIL".equalsIgnoreCase(identityType)
                && !configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_EMAIL", true)) {
            throw new BizException("邮箱登录已关闭");
        }
        if ("PHONE".equalsIgnoreCase(identityType)
                && !configApi.getBoolean("AUTH_LOGIN_" + typeName + "_ALLOW_PHONE", true)) {
            throw new BizException("手机登录已关闭");
        }
        if ("ACCOUNT".equalsIgnoreCase(identityType) && "OTP".equals(mode)) {
            throw new BizException("OTP 登录需使用邮箱或手机");
        }
    }

    private String noUserPolicy(AccountType accountType, String identityType) {
        String typeName = accountType.name();
        if ("EMAIL".equalsIgnoreCase(identityType)) {
            return configApi.getValue("AUTH_LOGIN_" + typeName + "_EMAIL_NO_USER_POLICY", "DENY")
                    .trim()
                    .toUpperCase(Locale.ROOT);
        }
        if ("PHONE".equalsIgnoreCase(identityType)) {
            return configApi.getValue("AUTH_LOGIN_" + typeName + "_PHONE_NO_USER_POLICY", "DENY")
                    .trim()
                    .toUpperCase(Locale.ROOT);
        }
        return "DENY";
    }

    private void sendCodeMailOrSms(String channel, String target, String templateScene, String code, Duration ttl) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("app_name", appName());
        vars.put("code", code);
        vars.put("expire_minutes", String.valueOf(Math.max(1, ttl.toMinutes())));
        if ("EMAIL".equals(channel)) {
            mailSenderFacade.sendTemplated(templateScene, target, vars);
            return;
        }
        if ("PHONE".equals(channel)) {
            smsSenderFacade.sendTemplated(templateScene, target, vars);
            return;
        }
        throw new BizException("不支持的发送渠道");
    }

    private String appName() {
        return ConfigAppNames.resolve(configApi);
    }

    private void notifyPasswordResetSuccess(String accountId, String accountName, String email, String phone) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("app_name", appName());
        vars.put("account", nullToEmpty(accountName));
        if (StringUtils.hasText(email)) {
            try {
                mailSenderFacade.sendTemplated("RESET_PASSWORD_SUCCESS", email.trim(), vars);
            } catch (Exception ignored) {
                // 尽力发送
            }
        }
        if (StringUtils.hasText(phone)) {
            try {
                smsSenderFacade.sendTemplated("RESET_PASSWORD_SUCCESS", phone.trim(), vars);
            } catch (Exception ignored) {
                // 尽力发送
            }
        }
    }

    private void maybeNotifyPasswordExpiring(AccountInfo account) {
        if (account == null || !StringUtils.hasText(account.getId())) {
            return;
        }
        Integer remainingDays = passwordExpiryWarningDays(account.getId());
        if (remainingDays == null || remainingDays <= 0) {
            return;
        }
        if (!cryptoService.tryMarkPasswordExpiryNotified(account.getId())) {
            return;
        }
        String email = accountApi.findIdentifier(account.getId(), "EMAIL");
        String phone = accountApi.findIdentifier(account.getId(), "PHONE");
        String accountName = accountApi.findIdentifier(account.getId(), "ACCOUNT");
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("app_name", appName());
        vars.put("account", nullToEmpty(accountName));
        vars.put("remaining_days", String.valueOf(remainingDays));
        if (StringUtils.hasText(email)) {
            try {
                mailSenderFacade.sendTemplated("PASSWORD_EXPIRING", email.trim(), vars);
            } catch (Exception ignored) {
                // 尽力发送
            }
        }
        if (StringUtils.hasText(phone)) {
            try {
                smsSenderFacade.sendTemplated("PASSWORD_EXPIRING", phone.trim(), vars);
            } catch (Exception ignored) {
                // 尽力发送
            }
        }
    }

    private Duration otpTtl() {
        int seconds = configApi.getInt("AUTH_OTP_TTL_SECONDS", DEFAULT_OTP_TTL_SECONDS);
        return Duration.ofSeconds(Math.max(60, seconds));
    }

    private String changeVerifyMethod() {
        return configApi.getValue("PASSWORD_CHANGE_VERIFY_METHOD", "OLD_PASSWORD")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static String sixDigitCode() {
        return String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private static String normalizeChannel(String channel) {
        if (!StringUtils.hasText(channel)) {
            throw new BizException("请指定发送渠道");
        }
        String value = channel.trim().toUpperCase(Locale.ROOT);
        if (!"EMAIL".equals(value) && !"PHONE".equals(value)) {
            throw new BizException("不支持的发送渠道");
        }
        return value;
    }

    private static String normalizeTarget(String channel, String target) {
        if (!StringUtils.hasText(target)) {
            throw new BizException("请填写接收目标");
        }
        return "EMAIL".equals(channel) ? target.trim().toLowerCase(Locale.ROOT) : target.trim();
    }

    private static String normalizeLoginAccount(String identityType, String account) {
        if (!StringUtils.hasText(account)) {
            return account;
        }
        return "EMAIL".equalsIgnoreCase(identityType)
                ? account.trim().toLowerCase(Locale.ROOT)
                : account.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private void issueCsrfCookie(boolean rememberMe) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getResponse() == null) {
            return;
        }
        HttpServletResponse response = attributes.getResponse();
        long ttl = resolveTokenTtlSeconds();
        int maxAge = rememberMe && ttl > 0 ? (int) Math.min(ttl, Integer.MAX_VALUE) : -1;
        CsrfDoubleSubmitFilter.issueCsrfCookie(response, false, "Lax", maxAge);
    }

    private void clearCsrfCookie() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getResponse() == null) {
            return;
        }
        CsrfDoubleSubmitFilter.clearCsrfCookie(attributes.getResponse());
    }

    private static String currentClientIp() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getRemoteAddr();
    }

    private static String deviceLabel(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return null;
        }
        String value = userAgent.toLowerCase(Locale.ROOT);
        if (value.contains("mobile") || value.contains("android") || value.contains("iphone")) {
            return "Mobile";
        }
        if (value.contains("ipad") || value.contains("tablet")) {
            return "Tablet";
        }
        return "Desktop";
    }

    private long resolveTokenTtlSeconds() {
        long configured = configApi.getLong("AUTH_TOKEN_TTL_SECONDS", 0);
        return configured > 0 ? configured : -1;
    }
}
