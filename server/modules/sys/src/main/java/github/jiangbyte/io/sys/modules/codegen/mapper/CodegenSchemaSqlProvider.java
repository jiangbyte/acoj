package github.jiangbyte.io.sys.modules.codegen.mapper;

import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;

import java.util.Map;

/**
 * 代码生成元数据 SQL Provider。
 *
 * Author: Charlie
 */
public final class CodegenSchemaSqlProvider {

    private CodegenSchemaSqlProvider() {
    }

    public static String listTables(Map<String, Object> params) {
        return DbDialectHolder.get().codegenListTablesSql();
    }

    public static String listColumns(Map<String, Object> params) {
        return DbDialectHolder.get().codegenListColumnsSql();
    }

    public static String listPrimaryKeys(Map<String, Object> params) {
        return DbDialectHolder.get().codegenListPrimaryKeysSql();
    }
}
