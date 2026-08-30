package github.jiangbyte.io.sys.modules.codegen.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成方案实体，对应表 sys_codegen_plan。
 *
 * Author: Charlie
 */
@Schema(description = "代码生成方案实体，对应表 sys_codegen_plan。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_codegen_plan")
public class SysCodegenPlan extends BaseEntity {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "生成类型：CRUD/TREE/SUB_TABLE 等")
    private String genType;
    @Schema(description = "作者")
    private String author;
    @Schema(description = "代码生成方案描述")
    private String description;
    @Schema(description = "主表数据库名")
    private String tableName;
    @Schema(description = "主表主键列名")
    private String pkColumn;
    @Schema(description = "生成的主实体类名")
    private String entityName;
    @Schema(description = "后端模块包路径")
    private String modulePath;
    @Schema(description = "主业务中文名")
    private String businessName;
    @Schema(description = "REST API 路径前缀")
    private String apiPrefix;
    @Schema(description = "权限标识前缀")
    private String permissionPrefix;
    @Schema(description = "挂载的资源模块ID")
    private String resourceModuleId;
    @Schema(description = "挂载的父菜单资源ID")
    private String parentResourceId;
    @Schema(description = "生成菜单名称")
    private String menuName;
    @Schema(description = "生成菜单路由路径")
    private String menuPath;
    @Schema(description = "生成前端组件路径")
    private String componentPath;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
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
