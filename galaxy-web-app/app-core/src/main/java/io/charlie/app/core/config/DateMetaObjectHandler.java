package io.charlie.app.core.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 自动填充创建人、更新人、创建时间、更新时间,当脱离上下文无法获取当前用户时，设置更新人为创建人
 */
@Slf4j
@Component
public class DateMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        String currentUser = getLoginUserOrNull();

        setFieldValByName("createTime", now, metaObject);
        setFieldValByName("createUser", currentUser, metaObject);

        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updateUser", currentUser, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Date now = new Date();
        String currentUser = getLoginUserOrNull();

        // 如果无法获取当前用户，则尝试使用创建人作为更新人
        if ("0".equals(currentUser)) {
            Object createUser = getFieldValByName("createUser", metaObject);
            if (createUser != null) {
                currentUser = createUser.toString();
            }
        }

        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updateUser", currentUser, metaObject);
    }

    public String getLoginUserOrNull() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            // 0 为系统（含脱离了上下文的）
            log.warn("无法获取当前登录用户，使用系统默认值");
            return "0";
        }
    }
}
