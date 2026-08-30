package github.jiangbyte.io.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectDetector;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;
import github.jiangbyte.io.common.mybatis.dialect.DbVendor;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JSON 列与 Jackson 对象互转：PostgreSQL 使用 PGobject(json)，MySQL 使用字符串。
 *
 * Author: Charlie
 */
@MappedTypes({Map.class, List.class, Object.class})
public class JacksonJsonTypeHandler extends JacksonTypeHandler {

    public JacksonJsonTypeHandler(Class<?> type) {
        super(type);
    }

    public JacksonJsonTypeHandler(Class<?> type, Field field) {
        super(type, field);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        String json = toJson(parameter);
        DbVendor vendor = resolveVendor(ps);
        if (vendor == DbVendor.MYSQL) {
            ps.setString(i, json);
            return;
        }
        PGobject pg = new PGobject();
        pg.setType("json");
        pg.setValue(json);
        ps.setObject(i, pg);
    }

    private static DbVendor resolveVendor(PreparedStatement ps) throws SQLException {
        if (DbDialectHolder.isReady()) {
            return DbDialectHolder.get().vendor();
        }
        return DbDialectDetector.require(null, ps.getConnection().getMetaData().getDatabaseProductName());
    }
}
