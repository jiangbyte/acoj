package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * authOptions 中下发的三方登录入口。
 *
 * Author: Charlie
 */
@Schema(description = "authOptions 中下发的三方登录入口。")
@Data
public class OauthProviderOptionResult {
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "label")
    private String label;
    @Schema(description = "是否启用：1 启用 / 0 停用")
    private Boolean enabled = false;
    @Schema(description = "是否网页 OAuth（小程序为 false，前端不展示跳转按钮）。")
    /** 是否网页 OAuth（小程序为 false，前端不展示跳转按钮）。 */
    private Boolean webOAuth = true;
}
