package github.jiangbyte.io.sys.modules.codegen.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新代码生成字段配置入参。
 *
 * Author: Charlie
 */
@Schema(description = "批量更新代码生成字段配置入参。")
@Data
public class SysCodegenFieldsUpdateBatchParam {
    @NotBlank
    @Schema(description = "planId")
    private String planId;

    @NotEmpty
    @Valid
    @Schema(description = "fields")
    private List<SysCodegenFieldUpdateItemParam> fields;
}
