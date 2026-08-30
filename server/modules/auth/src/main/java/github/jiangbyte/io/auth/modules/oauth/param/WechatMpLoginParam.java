package github.jiangbyte.io.auth.modules.oauth.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求。
 *
 * Author: Charlie
 */
@Schema(description = "微信小程序登录请求。")
@Data
public class WechatMpLoginParam {
    @NotBlank
    @Schema(description = "编码")
    private String code;
}
