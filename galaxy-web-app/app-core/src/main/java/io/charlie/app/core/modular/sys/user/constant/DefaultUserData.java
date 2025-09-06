package io.charlie.app.core.modular.sys.user.constant;

import cn.hutool.core.util.RandomUtil;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 默认 数据
 */
public interface DefaultUserData {
    String USER_DEFAULT_NICKNAME = "用户-" + RandomUtil.randomString(5);
    String USER_DEFAULT_AVATAR = "https://cdn.jsdelivr.net/gh/charlie-zhang-cn/cdn/img/avatar.png";
    String USER_DEFAULT_BACKGROUND = "https://cdn.jsdelivr.net/gh/charlie-zhang-cn/cdn/img/background.png";
    String USER_DEFAULT_QUOTE = "Hello World!";
    Integer USER_DEFAULT_GENDER = 0;
}
