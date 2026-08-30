package github.jiangbyte.io.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import org.apache.ibatis.reflection.MetaObject;

import java.time.OffsetDateTime;

/**
 * MyBatis-Plus 元对象填充：自动写入创建/更新时间与操作者。
 *
 * Author: Charlie
 */
public class HeiMetaObjectHandler implements MetaObjectHandler {

    /** 插入时填充审计字段。 */
    @Override
    public void insertFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now();
        String operator = currentAccountId();
        strictInsertFill(metaObject, "createdAt", OffsetDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", OffsetDateTime.class, now);
        if (operator != null) {
            strictInsertFill(metaObject, "createdBy", String.class, operator);
            strictInsertFill(metaObject, "updatedBy", String.class, operator);
        }
    }

    /** 更新时填充审计字段。 */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", OffsetDateTime.class, OffsetDateTime.now());
        String operator = currentAccountId();
        if (operator != null) {
            strictUpdateFill(metaObject, "updatedBy", String.class, operator);
        }
    }

    private static String currentAccountId() {
        try {
            return LoginHelper.currentUser()
                    .map(LoginUser::getAccountId)
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
