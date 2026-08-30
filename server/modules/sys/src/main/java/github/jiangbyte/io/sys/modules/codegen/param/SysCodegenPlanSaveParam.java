package github.jiangbyte.io.sys.modules.codegen.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存代码生成方案入参。
 *
 * Author: Charlie
 */
@Schema(description = "保存代码生成方案入参。")
@Data
public class SysCodegenPlanSaveParam {
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "名称")
    private String name;

    @NotBlank
    @Size(max = 32)
    @Schema(description = "生成类型：CRUD/TREE/SUB_TABLE 等")
    private String genType = "TABLE";

    @NotBlank
    @Size(max = 64)
    @Schema(description = "作者")
    private String author;
    @Schema(description = "代码生成方案描述")

    private String description;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "主表数据库名")
    private String tableName;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "主表主键列名")
    private String pkColumn = "id";

    @NotBlank
    @Size(max = 128)
    @Schema(description = "生成的主实体类名")
    private String entityName;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "后端模块包路径")
    private String modulePath;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "主业务中文名")
    private String businessName;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "REST API 路径前缀")
    private String apiPrefix;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "权限标识前缀")
    private String permissionPrefix;
    @Schema(description = "挂载的资源模块ID")

    private String resourceModuleId;
    @Schema(description = "挂载的父菜单资源ID")
    private String parentResourceId;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "生成菜单名称")
    private String menuName;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "生成菜单路由路径")
    private String menuPath;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "生成前端组件路径")
    private String componentPath;
    @Schema(description = "图标标识")

    private String icon;

    @NotNull
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "树表父级字段名")

    private String treeParentField;
    @Schema(description = "树节点展示字段名")
    private String treeLabelField;
    @Schema(description = "子表数据库名")
    private String subTable;
    @Schema(description = "子表主键列名")
    private String subPk;
    @Schema(description = "子表外键列名")
    private String subForeignKey;
    @Schema(description = "子实体类名")
    private String subEntityName;
    @Schema(description = "子业务中文名")
    private String subBusinessName;
}
