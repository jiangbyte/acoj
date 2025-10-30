package io.charlie.web.oj.constant;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 默认 数据
 */
public interface DefaultUserData {
    String USER_DEFAULT_NICKNAME = "用户-" + IdUtil.objectId();
    String USER_DEFAULT_QUOTE = "Hello World!";
    Integer USER_DEFAULT_GENDER = 0;
    String DEFAULT_PASSWORD = "123456789";
}
