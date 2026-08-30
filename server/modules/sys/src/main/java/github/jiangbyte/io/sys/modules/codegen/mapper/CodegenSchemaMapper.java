package github.jiangbyte.io.sys.modules.codegen.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/**
 * 代码生成数据库元数据查询 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface CodegenSchemaMapper {

    @SelectProvider(type = CodegenSchemaSqlProvider.class, method = "listTables")
    List<Map<String, Object>> listTables();

    @SelectProvider(type = CodegenSchemaSqlProvider.class, method = "listColumns")
    List<Map<String, Object>> listColumns(@Param("tableName") String tableName);

    @SelectProvider(type = CodegenSchemaSqlProvider.class, method = "listPrimaryKeys")
    List<String> listPrimaryKeys(@Param("tableName") String tableName);
}
