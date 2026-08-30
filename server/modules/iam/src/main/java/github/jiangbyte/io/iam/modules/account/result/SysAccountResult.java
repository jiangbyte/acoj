package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.profile.ProfileIdentityStatusInfo;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号详情/分页行结果：合并账号主数据、资料与身份展示字段。
 *
 * Author: Charlie
 */
@Schema(description = "账号详情/分页行结果：合并账号主数据、资料与身份展示字段。")
@Data
public class SysAccountResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户状态：ACTIVE（正常）/ LOCKED（锁定）/ CANCELLED（已注销）")
    private String accountStatus;
    @Schema(description = "名称")
    private String name;
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
    @Schema(description = "是否启用邮箱登录")
    private Boolean emailLoginEnabled = false;
    @Schema(description = "是否启用手机号登录")
    private Boolean phoneLoginEnabled = false;
    @Schema(description = "邮箱身份标识")
    private String emailIdentity;
    @Schema(description = "手机号身份标识")
    private String phoneIdentity;
    @Schema(description = "邮箱身份是否已验证")
    private Boolean emailIdentityVerified = false;
    @Schema(description = "手机号身份是否已验证")
    private Boolean phoneIdentityVerified = false;
    @Schema(description = "邮箱身份绑定状态")
    private String emailIdentityBindStatus;
    @Schema(description = "手机号身份绑定状态")
    private String phoneIdentityBindStatus;
    @Schema(description = "账号身份标识列表")
    private List<AccountIdentityResult> identities = new ArrayList<>();
    @Schema(description = "三方登录绑定列表")
    private List<AccountOauthBindingResult> oauthBindings = new ArrayList<>();
    @Schema(description = "实名认证快照（仅详情返回）")
    private ProfileIdentityStatusInfo identityStatus;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "账号注销完成时间")
    private OffsetDateTime cancelledAt;
    @Schema(description = "执行注销的操作人账户ID")
    private String cancelledBy;
    @Schema(description = "注销原因说明")
    private String cancelReason;
    @Schema(description = "上一次成功登录 IP")
    private String lastLoginIp;
    @Schema(description = "上一次成功登录地理位置")
    private String lastLoginAddress;
    @Schema(description = "上一次成功登录时间")
    private OffsetDateTime lastLoginTime;
    @Schema(description = "上一次成功登录设备标识")
    private String lastLoginDevice;
    @Schema(description = "最近一次成功登录 IP")
    private String latestLoginIp;
    @Schema(description = "最近一次成功登录地理位置")
    private String latestLoginAddress;
    @Schema(description = "最近一次成功登录时间")
    private OffsetDateTime latestLoginTime;
    @Schema(description = "最近一次成功登录设备标识")
    private String latestLoginDevice;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
}
