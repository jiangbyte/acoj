package github.jiangbyte.io.sys.modules.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 批量保存配置单项入参。
 *
 * Author: Charlie
 */
@Schema(description = "批量保存配置单项入参。")
@Data
public class SysConfigBatchItemParam {

    @NotBlank
    @Schema(description = "configKey")
    private String configKey;
    @Schema(description = "configValue")
    private String configValue;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "valueType")
    private String valueType;
    @Schema(description = "label")
    private String label;
    @Schema(description = "scope")
    private String scope;
    @Schema(description = "scene")
    private String scene;
    @Schema(description = "是否内置：1 内置不可删 / 0 可维护")
    private Boolean isBuiltin;
}
