package github.jiangbyte.io.sys.modules.codegen.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库表元数据查询结果。
 *
 * Author: Charlie
 */
@Schema(description = "数据库表元数据查询结果。")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysCodegenDatabaseTableResult {
    @Schema(description = "tableName")
    private String tableName;
    @Schema(description = "tableComment")
    private String tableComment;
}
