package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注销当前登录账号的请求参数，可选填写注销原因。
 *
 * Author: Charlie
 */
@Schema(description = "注销当前登录账号的请求参数，可选填写注销原因。")
@Data
public class CancelAccountParam {
    @Schema(description = "cancelReason")
    private String cancelReason;
}
