package github.jiangbyte.io.common.core.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 单 id 请求体，用于详情/删除等只传主键的接口。
 *
 * Author: Charlie
 */
@Schema(description = "单 id 请求体，用于详情/删除等只传主键的接口。")
@Data
public class IdParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
}
