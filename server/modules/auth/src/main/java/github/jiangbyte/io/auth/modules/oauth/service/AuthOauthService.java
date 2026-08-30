package github.jiangbyte.io.auth.modules.oauth.service;

import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.oauth.result.OauthAuthorizeResult;
import github.jiangbyte.io.auth.modules.oauth.result.OauthBindingResult;
import github.jiangbyte.io.common.core.enums.AccountType;

import java.util.List;

/**
 * 三方登录：授权、回调、小程序登录、绑定/解绑。
 *
 * Author: Charlie
 */
public interface AuthOauthService {

    OauthAuthorizeResult authorize(AccountType accountType, String provider, String intent, String redirect);

    /** 处理网页回调；返回前端跳转 URL（带 oauth_code，不含 token）。 */
    String handleCallback(AccountType accountType, String provider, String code, String state);

    /** 用一次性 oauth_code 兑换登录结果。 */
    LoginResult exchange(String code);

    LoginResult loginWechatMp(AccountType accountType, String code);

    List<OauthBindingResult> listCurrentBindings();

    OauthAuthorizeResult bindAuthorize(AccountType accountType, String provider);

    void unbind(String provider);

    /** 管理端强制解绑指定账号的提供商。 */
    void adminUnbind(String accountId, String provider);
}
