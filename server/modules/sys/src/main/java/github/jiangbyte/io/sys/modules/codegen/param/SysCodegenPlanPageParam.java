package github.jiangbyte.io.sys.modules.codegen.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成方案分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "代码生成方案分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysCodegenPlanPageParam extends PageQuery {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "主表数据库名")
    private String tableName;
    @Schema(description = "生成类型：CRUD/TREE/SUB_TABLE 等")
    private String genType;
}
