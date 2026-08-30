package github.jiangbyte.io.sys.modules.codegen.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库列元数据查询结果。
 *
 * Author: Charlie
 */
@Schema(description = "数据库列元数据查询结果。")
@Data
public class SysCodegenDatabaseColumnResult {
    @Schema(description = "columnName")
    private String columnName;
    @Schema(description = "label")
    private String label;
    @Schema(description = "dbType")
    private String dbType;
    @Schema(description = "valueType")
    private String valueType;
    @Schema(description = "uiType")
    private String uiType;
    @Schema(description = "primaryKey")
    private Boolean primaryKey;
    @Schema(description = "nullable")
    private Boolean nullable;
    @Schema(description = "maxLength")
    private Integer maxLength;
}
