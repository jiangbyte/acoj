package github.jiangbyte.io.sys.modules.codegen.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成字段配置实体，对应表 sys_codegen_field。
 *
 * Author: Charlie
 */
@Schema(description = "代码生成字段配置实体，对应表 sys_codegen_field。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_codegen_field")
public class SysCodegenField extends BaseEntity {
    @Schema(description = "所属代码生成方案ID")
    private String planId;
    @Schema(description = "表角色：MASTER/SUB 等")
    private String tableRole;
    @Schema(description = "数据库列名")
    private String columnName;
    @Schema(description = "字段展示标签（通常来自库表注释）。")
    /** 字段展示标签（通常来自库表注释）。 */
    private String label;
    @Schema(description = "数据库物理类型")
    private String dbType;
    @Schema(description = "语义值类型（如 str / int / bool / datetime / dict）。")
    /** 语义值类型（如 str / int / bool / datetime / dict）。 */
    private String valueType;
    @Schema(description = "UI 类型（如 string / number / boolean）。")
    /** UI 类型（如 string / number / boolean）。 */
    private String uiType;
    @Schema(description = "表单控件类型")
    private String widget;
    @Schema(description = "关联数据字典编码")
    private String dictCode;
    @Schema(description = "列表查询运算符：eq/like/between 等")
    private String queryOperator;
    @Schema(description = "是否在表格列展示：1 是 / 0 否")
    private Boolean inTable;
    @Schema(description = "是否在表单展示：1 是 / 0 否")
    private Boolean inForm;
    @Schema(description = "是否在详情展示：1 是 / 0 否")
    private Boolean inDetail;
    @Schema(description = "是否作为查询条件：1 是 / 0 否")
    private Boolean inQuery;
    @Schema(description = "是否主键列：1 是 / 0 否")
    private Boolean primaryKey;
    @Schema(description = "是否必填：1 是 / 0 否")
    private Boolean required;
    @Schema(description = "是否唯一：1 是 / 0 否")
    private Boolean uniqueFlag;
    @Schema(description = "是否允许为空：1 可空 / 0 非空")
    private Boolean nullable;
    @Schema(description = "字段最大长度限制")
    private Integer maxLength;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
}
