package github.jiangbyte.io.sys.modules.codegen.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 单条代码生成字段更新项入参。
 *
 * Author: Charlie
 */
@Schema(description = "单条代码生成字段更新项入参。")
@Data
public class SysCodegenFieldUpdateItemParam {
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Size(max = 16)
    @Schema(description = "tableRole")
    private String tableRole = "MAIN";

    @NotBlank
    @Size(max = 128)
    @Schema(description = "columnName")
    private String columnName;
    @Schema(description = "label")

    private String label;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "dbType")
    private String dbType;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "valueType")
    private String valueType = "str";

    @NotBlank
    @Size(max = 64)
    @Schema(description = "uiType")
    private String uiType = "string";

    @NotBlank
    @Size(max = 32)
    @Schema(description = "widget")
    private String widget = "input";
    @Schema(description = "dictCode")

    private String dictCode;
    @Schema(description = "queryOperator")
    private String queryOperator;

    @NotNull
    @Schema(description = "inTable")
    private Boolean inTable = true;

    @NotNull
    @Schema(description = "inForm")
    private Boolean inForm = true;

    @NotNull
    @Schema(description = "inDetail")
    private Boolean inDetail = true;

    @NotNull
    @Schema(description = "inQuery")
    private Boolean inQuery = false;

    @NotNull
    @Schema(description = "primaryKey")
    private Boolean primaryKey = false;

    @NotNull
    @Schema(description = "required")
    private Boolean required = false;

    @NotNull
    @Schema(description = "uniqueFlag")
    private Boolean uniqueFlag = false;

    @NotNull
    @Schema(description = "nullable")
    private Boolean nullable = true;
    @Schema(description = "maxLength")

    private Integer maxLength;

    @NotNull
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
}
