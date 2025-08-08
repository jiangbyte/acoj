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
 * @description 自动填充创建人、更新人、创建时间、更新时间
 */
@Slf4j
@Component
public class DateMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        setFieldValByName("createTime", now, metaObject);
        setFieldValByName("createUser", getLoginUserOrNull(), metaObject);

        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updateUser", getLoginUserOrNull(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Date now = new Date();
        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updateUser", getLoginUserOrNull(), metaObject);
    }

    public String getLoginUserOrNull() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            // 0 为系统（含脱离了上下文的）
            return "0";
        }
    }
}
