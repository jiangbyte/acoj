package github.jiangbyte.io.iam.modules.account.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号身份实体，对应表 sys_account_identity；标识 ACCOUNT/EMAIL/PHONE 等登录标识。
 *
 * Author: Charlie
 */
@Schema(description = "账号身份实体，对应表 sys_account_identity；标识 ACCOUNT/EMAIL/PHONE 等登录标识。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_account_identity")
public class SysAccountIdentity extends BaseEntity {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "身份类型：USERNAME/EMAIL/PHONE 等")
    private String identityType;
    @Schema(description = "登录标识值（用户名/邮箱/手机号）")
    private String identifier;
    @Schema(description = "标识是否已完成验证：1 是 / 0 否")
    private Boolean verified;
    @Schema(description = "是否主登录标识：1 主标识 / 0 次标识")
    private Boolean isPrimary;
    @Schema(description = "绑定状态：BOUND/UNBOUND/PENDING 等")
    private String bindStatus;
}
