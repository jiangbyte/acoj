package github.jiangbyte.io.sys.modules.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量保存系统配置入参。
 *
 * Author: Charlie
 */
@Schema(description = "批量保存系统配置入参。")
@Data
public class SysConfigBatchSaveParam {

    @NotEmpty
    @Valid
    @Schema(description = "items")
    private List<SysConfigBatchItemParam> items;
}
