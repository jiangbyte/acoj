package github.jiangbyte.io.profile.modules.portal.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门户端更新个人资料的请求参数（姓名、昵称、头像、签名、备注）。
 *
 * Author: Charlie
 */
@Schema(description = "门户端更新个人资料的请求参数（姓名、昵称、头像、签名、备注）。")
@Data
public class ProfileUpdateParam {
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "signature")
    private String signature;
    @Schema(description = "备注说明")
    private String remark;
}
