package github.jiangbyte.io.auth;

/** Author: Charlie **/

import cn.dev33.satoken.stp.StpLogic;
import github.jiangbyte.io.auth.modules.login.convert.AuthConvert;
import github.jiangbyte.io.auth.modules.login.param.LoginParam;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.service.AuthServiceImpl;
import github.jiangbyte.io.auth.modules.login.support.AuthCryptoService;
import github.jiangbyte.io.auth.modules.login.support.LoginProtectionService;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.notify.mail.MailSenderFacade;
import github.jiangbyte.io.common.notify.sms.SmsSenderFacade;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.account.AccountApi;
import github.jiangbyte.io.iam.account.AccountAuthorizationInfo;
import github.jiangbyte.io.iam.account.AccountInfo;
import github.jiangbyte.io.iam.password.PasswordPolicyApi;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    @Mock
    private AuthCryptoService cryptoService;
    @Mock
    private AccountApi accountApi;
    @Mock
    private ProfileUserAdminApi adminUserProfileApi;
    @Mock
    private ProfileUserPortalApi portalUserProfileApi;
    @Mock
    private MailSenderFacade mailSenderFacade;
    @Mock
    private SmsSenderFacade smsSenderFacade;
    @Mock
    private PasswordPolicyApi passwordPolicyApi;
    @Mock
    private LoginProtectionService loginProtectionService;
    @Mock
    private ConfigApi configApi;
    @Mock
    private AuthConvert authConvert;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(configApi.getLong("AUTH_TOKEN_TTL_SECONDS", 0)).thenReturn(2592000L);
        lenient().when(configApi.getInt("PASSWORD_VALIDITY_DAYS", 90)).thenReturn(90);
        lenient().when(configApi.getInt("PASSWORD_EXPIRY_WARNING_DAYS", 0)).thenReturn(0);
        lenient().when(configApi.getBoolean("AUTH_LOGIN_ADMIN_ALLOW_EMAIL", true)).thenReturn(true);
        lenient().when(configApi.getBoolean("AUTH_LOGIN_ADMIN_ALLOW_PHONE", true)).thenReturn(true);
        lenient().when(configApi.getBoolean("AUTH_LOGIN_ADMIN_ALLOW_OTP", true)).thenReturn(true);
        authService = new AuthServiceImpl(
                cryptoService,
                accountApi,
                adminUserProfileApi,
                portalUserProfileApi,
                mailSenderFacade,
                smsSenderFacade,
                passwordPolicyApi,
                loginProtectionService,
                configApi,
                authConvert);
    }

    @Test
    void loginSucceedsWithValidBcryptPassword() {
        LoginParam request = new LoginParam();
        request.setAccount("superadmin");
        request.setPassword("encrypted");
        request.setPasswordKeyId("pk");
        request.setCaptchaId("c1");
        request.setCaptchaValue("ab12");
        request.setAccountType(AccountType.ADMIN);

        AccountInfo account = new AccountInfo();
        account.setId("1");
        account.setAccountType("ADMIN");
        account.setAccountStatus("ENABLED");
        account.setPasswordHash("$2a$10$hashed");

        AccountAuthorizationInfo authorization = new AccountAuthorizationInfo();
        authorization.setRoleCodes(List.of("SUPER_ADMIN"));
        authorization.setPermissionKeys(List.of("*"));
        authorization.setRoleIds(List.of("1"));

        doNothing().when(cryptoService).verifyCaptcha("c1", "ab12");
        doNothing().when(loginProtectionService).ensureAllowed(eq(AccountType.ADMIN), eq("superadmin"), isNull());
        when(cryptoService.decryptPassword("pk", "encrypted")).thenReturn("123456");
        when(accountApi.findByIdentifier("superadmin", "ACCOUNT")).thenReturn(account);
        when(accountApi.matchesPassword("123456", account.getPasswordHash())).thenReturn(true);
        when(accountApi.getAuthorization("1")).thenReturn(authorization);
        when(accountApi.isPasswordExpired(eq("1"), anyInt())).thenReturn(false);
        doNothing().when(accountApi).updateLoginMeta(eq("1"), any(), any(), any());
        LoginResult converted = new LoginResult();
        converted.setAccountId("1");
        converted.setAccountType(AccountType.ADMIN);
        converted.setPasswordExpired(false);
        when(authConvert.toLoginResponse(eq("1"), eq(AccountType.ADMIN), eq(false))).thenReturn(converted);

        StpLogic stpLogic = mock(StpLogic.class);
        when(stpLogic.getTokenValue()).thenReturn("token-abc");

        try (MockedStatic<LoginHelper> loginHelper = mockStatic(LoginHelper.class)) {
            loginHelper.when(() -> LoginHelper.login(any(), any(Long.class))).thenAnswer(invocation -> null);
            loginHelper.when(() -> LoginHelper.stpLogic(AccountType.ADMIN)).thenReturn(stpLogic);

            LoginResult response = authService.login(request);

            assertEquals("token-abc", response.getToken());
            assertEquals("1", response.getAccountId());
            assertEquals(AccountType.ADMIN, response.getAccountType());
            verify(accountApi).matchesPassword("123456", account.getPasswordHash());
            verify(loginProtectionService).recordSuccess(eq(AccountType.ADMIN), eq("superadmin"), isNull());
        }
    }

    @Test
    void loginFailsWhenPasswordMismatch() {
        LoginParam request = new LoginParam();
        request.setAccount("superadmin");
        request.setPassword("encrypted");
        request.setPasswordKeyId("pk");
        request.setCaptchaId("c1");
        request.setCaptchaValue("ab12");
        request.setAccountType(AccountType.ADMIN);

        AccountInfo account = new AccountInfo();
        account.setId("1");
        account.setAccountType("ADMIN");
        account.setAccountStatus("ENABLED");
        account.setPasswordHash("$2a$10$hashed");

        doNothing().when(cryptoService).verifyCaptcha(anyString(), anyString());
        doNothing().when(loginProtectionService).ensureAllowed(any(), anyString(), isNull());
        when(cryptoService.decryptPassword(anyString(), anyString())).thenReturn("wrong");
        when(accountApi.findByIdentifier(anyString(), anyString())).thenReturn(account);
        when(accountApi.matchesPassword("wrong", account.getPasswordHash())).thenReturn(false);

        BizException ex = assertThrows(BizException.class, () -> authService.login(request));
        assertEquals(401, ex.getCode());
        verify(loginProtectionService).recordFailure(eq(AccountType.ADMIN), eq("superadmin"), isNull());
    }
}
