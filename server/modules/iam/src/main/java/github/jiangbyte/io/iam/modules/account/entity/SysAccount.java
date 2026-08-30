package github.jiangbyte.io.iam.modules.account.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * 系统账号实体，对应表 sys_account；承载登录态、状态、密码哈希与取消注销等字段。
 *
 * Author: Charlie
 */
@Schema(description = "系统账号实体，对应表 sys_account；承载登录态、状态、密码哈希与取消注销等字段。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_account")
public class SysAccount extends BaseEntity {
    @Schema(description = "登录密码哈希值（bcrypt 等）")
    private String passwordHash;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户状态：ACTIVE（正常）/ LOCKED（锁定）/ CANCELLED（已注销）")
    private String accountStatus;
    @Schema(description = "账号注销完成时间")
    private OffsetDateTime cancelledAt;
    @Schema(description = "执行注销的操作人账户ID")
    private String cancelledBy;
    @Schema(description = "注销原因说明")
    private String cancelReason;
    @Schema(description = "注销前快照：通知邮箱（身份清理前保留）")
    private String cancelNotifyEmail;
    @Schema(description = "注销前快照：通知手机号（身份清理前保留）")
    private String cancelNotifyPhone;
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
}
