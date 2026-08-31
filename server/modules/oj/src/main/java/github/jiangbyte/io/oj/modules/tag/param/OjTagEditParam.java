package github.jiangbyte.io.oj.modules.tag.param;

/**
 * 编辑 OJ 标签入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "OjTag 编辑入参")
@Data
public class OjTagEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "标签名称")
    private String name;
    @NotBlank
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
