package github.jiangbyte.io.sys.support;

import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;

/**
 * 消息模块鉴权辅助：获取当前登录用户及其账户类型名称。
 *
 * Author: Charlie
 */
public final class MessageAuthSupport {

    private MessageAuthSupport() {
    }

    /** 获取当前登录用户，未登录则抛出异常。 */
    public static LoginUser requireUser() {
        return LoginHelper.requireUser();
    }

    /** 返回用户账户类型枚举名。 */
    public static String accountType(LoginUser user) {
        return user.getAccountType().name();
    }
}
