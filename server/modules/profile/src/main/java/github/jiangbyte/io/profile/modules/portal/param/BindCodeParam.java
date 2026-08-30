package github.jiangbyte.io.profile.modules.portal.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端发送绑定 OTP 请求。
 *
 * Author: Charlie
 */
@Schema(description = "门户端发送绑定 OTP 请求。")
@Data
public class BindCodeParam {
    @NotBlank
    @Schema(description = "target")
    private String target;
}
