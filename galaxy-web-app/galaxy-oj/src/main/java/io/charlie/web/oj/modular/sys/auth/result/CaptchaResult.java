package io.charlie.web.oj.modular.sys.auth.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 验证码结果
 */
@Data
public class CaptchaResult {
    @Schema(description = "验证码图片")
    private String captcha;

    @Schema(description = "验证码编号")
    private String uuid;
}
