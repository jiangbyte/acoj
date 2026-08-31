package github.jiangbyte.io.oj.modules.tag.param;

/**
 * 创建 OJ 标签入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "OjTag 创建入参")
@Data
public class OjTagAddParam {
    @NotBlank
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
