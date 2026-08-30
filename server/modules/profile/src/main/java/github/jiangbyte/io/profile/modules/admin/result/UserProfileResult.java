package github.jiangbyte.io.profile.modules.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 管理端用户资料 DTO：基础资料字段及手机/邮箱登录开关。
 *
 * Author: Charlie
 */
@Schema(description = "管理端用户资料 DTO：基础资料字段及手机/邮箱登录开关。")
@Data
public class UserProfileResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "signature")
    private String signature;
    @Schema(description = "phone")
    private String phone;
    @Schema(description = "email")
    private String email;
    @Schema(description = "是否启用手机号登录")
    private Boolean phoneLoginEnabled;
    @Schema(description = "是否启用邮箱登录")
    private Boolean emailLoginEnabled;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
