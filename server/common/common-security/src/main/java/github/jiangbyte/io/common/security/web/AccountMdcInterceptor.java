package github.jiangbyte.io.common.security.web;

import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.common.web.log.RequestLogMdc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Sa-Token 鉴权后将账号身份写入 MDC（对齐 hei-fastapi session_auth bind）。
 * 由 TraceIdFilter 清理，访问日志仍可读到账号字段。
 *
 * Author: Charlie
 */
public class AccountMdcInterceptor implements HandlerInterceptor {

    /** 请求前将账号信息写入 MDC。 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginHelper.currentUser().ifPresent(this::bind);
        return true;
    }

    private void bind(LoginUser user) {
        RequestLogMdc.putIfHasText(RequestLogMdc.ACCOUNT_ID, user.getAccountId());
        if (user.getAccountType() != null) {
            RequestLogMdc.putIfHasText(RequestLogMdc.ACCOUNT_TYPE, user.getAccountType().name());
        }
    }
}
