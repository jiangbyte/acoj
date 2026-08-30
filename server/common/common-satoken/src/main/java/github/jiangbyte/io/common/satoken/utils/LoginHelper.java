package github.jiangbyte.io.common.satoken.utils;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 登录会话辅助：写入/读取 LoginUser、判断登录态与账号类型。
 *
 * Author: Charlie
 */
public final class LoginHelper {

    public static final String LOGIN_USER_KEY = "login_user";

    private static final Pattern PORTAL_PATH = Pattern.compile("^/api/[^/]+/portal(?:/|$)");
    private static final Pattern ADMIN_PATH = Pattern.compile("^/api/[^/]+/admin(?:/|$)");

    private LoginHelper() {
    }

    /** 写入登录会话（LoginUser）。 */
    public static void login(LoginUser loginUser) {
        login(loginUser, -1);
    }

    /**
     * 写入登录会话，并可指定 Token 超时秒数。
     *
     * @param timeoutSeconds Sa-Token 超时；{@code <= 0} 使用全局 {@code sa-token.timeout}
     */
    public static void login(LoginUser loginUser, long timeoutSeconds) {
        StpLogic stpLogic = stpLogic(loginUser.getAccountType());
        SaLoginParameter param = SaLoginParameter.create()
                .setRightNowCreateTokenSession(true)
                .setIsLastingCookie(loginUser.isRememberMe());
        if (timeoutSeconds > 0) {
            param.setTimeout(timeoutSeconds);
        }
        stpLogic.login(loginUser.getAccountId(), param);
        stpLogic.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /** 注销当前登录会话。 */
    public static void logout(AccountType accountType) {
        stpLogic(accountType).logout();
    }

    /** 注销指定账号在 ADMIN/PORTAL 下的全部会话（授权变更后踢下线）。 */
    public static void logoutAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return;
        }
        StpKit.ADMIN.logout(accountId);
        StpKit.PORTAL.logout(accountId);
    }

    /** 获取当前登录用户（Optional）。 */
    public static Optional<LoginUser> currentUser() {
        StpLogic stpLogic = resolveStpLogic();
        if (!stpLogic.isLogin()) {
            return Optional.empty();
        }
        Object user = stpLogic.getTokenSession().get(LOGIN_USER_KEY);
        if (user instanceof LoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    /** 获取当前登录用户；未登录则抛错。 */
    public static LoginUser requireUser() {
        return currentUser().orElseThrow(() -> new BizException(401, "未登录"));
    }

    /** 按账号类型解析对应 StpLogic。 */
    public static StpLogic stpLogic(AccountType accountType) {
        if (accountType == AccountType.PORTAL) {
            return StpKit.PORTAL;
        }
        return StpKit.ADMIN;
    }

    /** 按请求路径或登录态解析当前 StpLogic。 */
    public static StpLogic resolveStpLogic() {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            String path = request.getRequestURI();
            if (PORTAL_PATH.matcher(path).find()) {
                return StpKit.PORTAL;
            }
            if (ADMIN_PATH.matcher(path).find()) {
                return StpKit.ADMIN;
            }
        }
        if (StpKit.ADMIN.isLogin()) {
            return StpKit.ADMIN;
        }
        if (StpKit.PORTAL.isLogin()) {
            return StpKit.PORTAL;
        }
        return StpKit.ADMIN;
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
