package github.jiangbyte.io.sys.modules.job.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 任务启停入参。
 *
 * Author: Charlie
 */
@Schema(description = "任务启停入参。")
@Data
public class SysJobEnabledParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;

    @NotNull
    @Schema(description = "是否启用：1 启用 / 0 停用")
    private Boolean enabled;
}
