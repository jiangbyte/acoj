package github.jiangbyte.io.iam.modules.account.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号密码历史实体，对应表 sys_account_password_history；用于密码复用校验与审计。
 *
 * Author: Charlie
 */
@Schema(description = "账号密码历史实体，对应表 sys_account_password_history；用于密码复用校验与审计。")
@Data
@TableName("sys_account_password_history")
public class SysAccountPasswordHistory {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "密码哈希值（不可逆）")
    private String passwordHash;
    @Schema(description = "密码变更操作人（账户ID 或 system）")
    private String changedBy;
    @Schema(description = "变更原因：register/admin_reset/self_reset/password_expired")
    private String changeReason;
    @Schema(description = "密码写入历史时间")
    private OffsetDateTime createdAt;
}
