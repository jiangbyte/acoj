package github.jiangbyte.io.auth.modules.login.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送绑定邮箱/手机 OTP 的请求（登录态）。
 *
 * Author: Charlie
 */
@Schema(description = "发送绑定邮箱/手机 OTP 的请求（登录态）。")
@Data
public class SendBindCodeParam {

    @NotBlank
    @Schema(description = "target")
    private String target;
}
