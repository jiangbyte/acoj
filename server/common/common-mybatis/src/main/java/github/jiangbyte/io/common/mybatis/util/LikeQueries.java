package github.jiangbyte.io.common.mybatis.util;

import github.jiangbyte.io.common.mybatis.dialect.SqlSafe;

/**
 * LIKE 查询辅助：转义用户输入通配符。
 *
 * Author: Charlie
 */
public final class LikeQueries {

    private LikeQueries() {
    }

    /** 供 MyBatis-Plus {@code .like} 使用的已转义关键字（MP 仍会两侧加 %）。 */
    public static String keyword(String raw) {
        return SqlSafe.escapeLike(raw == null ? "" : raw);
    }
}
