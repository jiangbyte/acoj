package github.jiangbyte.io.auth.modules.login.convert;

import github.jiangbyte.io.auth.modules.login.result.CurrentUserResult;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import github.jiangbyte.io.auth.modules.login.result.RegisterResult;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 认证模块对象转换：LoginUser / 账号字段映射为登录、注册与当前用户响应。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthConvert {

    /** 将会话中的 LoginUser 转为当前用户响应。 */
    CurrentUserResult toCurrentUser(LoginUser loginUser);

    /** 组装登录响应骨架（Token/TTL/告警天数由调用方补齐）。 */
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "passwordExpiryWarningDays", ignore = true)
    LoginResult toLoginResponse(String accountId, AccountType accountType, Boolean passwordExpired);

    /** 组装门户注册成功响应。 */
    RegisterResult toRegisterResponse(String accountId, String account, AccountType accountType);
}
