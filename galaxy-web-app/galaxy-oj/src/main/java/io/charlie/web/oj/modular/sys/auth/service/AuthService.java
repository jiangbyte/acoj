package io.charlie.web.oj.modular.sys.auth.service;


import io.charlie.web.oj.modular.sys.auth.param.PasswordChangeParam;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.web.oj.modular.sys.auth.param.UsernamePasswordLoginParam;
import io.charlie.web.oj.modular.sys.auth.result.CaptchaResult;
import io.charlie.web.oj.modular.sys.auth.result.LoginUser;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 认证 服务
 */
public interface AuthService {
    CaptchaResult captcha();

    String doLogin(UsernamePasswordLoginParam usernamePasswordLoginParam);

    String doRegister(UsernamePasswordEmailRegisterParam usernamePasswordEmailRegisterParam);

    void doLogout();

    LoginUser getLoginUser();

    void changePassword(PasswordChangeParam passwordChangeParam);
}
