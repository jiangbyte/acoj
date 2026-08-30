package github.jiangbyte.io.sys.modules.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 编辑系统配置入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑系统配置入参。")
@Data
public class SysConfigEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "配置项唯一键")
    private String configKey;
    @Schema(description = "配置项值（按 value_type 解析）")
    private String configValue;
    @Schema(description = "配置分类/分组")
    private String category;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "同分类下排序码")
    private Integer sortCode = 0;
    @Schema(description = "值类型：STRING/JSON/BOOL/NUMBER")
    private String valueType = "STRING";
    @Schema(description = "配置项展示名称")
    private String label;
    @Schema(description = "作用域账户类型：GLOBAL/ADMIN/PORTAL")
    private String scope;
    @Schema(description = "业务场景编码")
    private String scene;
    @Schema(description = "是否内置配置：1 内置不可删 / 0 可维护")
    private Boolean isBuiltin = false;
    @Schema(description = "扩展配置（JSON）")
    private Map<String, Object> extJson = Map.of();
}
