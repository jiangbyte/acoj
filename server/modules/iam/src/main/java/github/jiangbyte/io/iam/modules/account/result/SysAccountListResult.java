package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号分页列表行：仅合并 sys_account 与 profile 展示字段。
 *
 * Author: Charlie
 */
@Schema(description = "账号分页列表行：仅合并 sys_account 与 profile 展示字段。")
@Data
public class SysAccountListResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户状态：ACTIVE（正常）/ LOCKED（锁定）/ CANCELLED（已注销）")
    private String accountStatus;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "phone")
    private String phone;
    @Schema(description = "email")
    private String email;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "最近一次成功登录时间")
    private OffsetDateTime latestLoginTime;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
