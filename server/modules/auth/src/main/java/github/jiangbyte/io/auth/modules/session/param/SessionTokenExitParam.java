package github.jiangbyte.io.auth.modules.session.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 按 Token 值批量强制下线的请求参数。
 *
 * Author: Charlie
 */
@Schema(description = "按 Token 值批量强制下线的请求参数。")
@Data
public class SessionTokenExitParam {

    @NotEmpty
    @Schema(description = "tokens")
    private List<String> tokens = new ArrayList<>();
}
