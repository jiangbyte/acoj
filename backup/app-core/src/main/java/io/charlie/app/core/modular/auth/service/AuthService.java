package io.charlie.app.core.modular.auth.service;

import io.charlie.app.core.modular.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.app.core.modular.auth.param.UsernamePasswordLoginParam;
import io.charlie.app.core.modular.auth.result.CaptchaResult;
import io.charlie.app.core.modular.auth.result.LoginUser;

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
}
