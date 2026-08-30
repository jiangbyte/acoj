package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图形验证码响应：验证码 ID、Base64 图片内容与 MIME 类型。
 *
 * Author: Charlie
 */
@Schema(description = "图形验证码响应：验证码 ID、Base64 图片内容与 MIME 类型。")
@Data
public class CaptchaResult {
    @Schema(description = "captchaId")
    private String captchaId;
    @Schema(description = "imageBase64")
    private String imageBase64;
    @Schema(description = "imageType")
    private String imageType = "image/svg+xml";
}
