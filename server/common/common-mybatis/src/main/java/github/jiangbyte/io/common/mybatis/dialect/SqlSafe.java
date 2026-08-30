package github.jiangbyte.io.common.mybatis.dialect;

import java.util.regex.Pattern;

/**
 * SQL 标识符与 LIKE 通配符安全工具。
 *
 * Author: Charlie
 */
public final class SqlSafe {

    private static final Pattern IDENT =
            Pattern.compile("(?i)^[a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)?$");

    private SqlSafe() {
    }

    /** 校验列名/表名，仅允许安全标识符。 */
    public static String requireIdent(String name) {
        if (name == null || !IDENT.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("unsafe sql identifier: " + name);
        }
        return name.trim();
    }

    /** 转义 LIKE 通配符。 */
    public static String escapeLike(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
