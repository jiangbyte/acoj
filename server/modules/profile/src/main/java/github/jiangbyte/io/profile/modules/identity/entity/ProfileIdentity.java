package github.jiangbyte.io.profile.modules.identity.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号实名认证快照，对应表 {@code profile_identity}。
 *
 * Author: Charlie
 */
@Schema(description = "账号实名认证快照，对应表 profile_identity。")
@Data
@TableName("profile_identity")
public class ProfileIdentity {

    @TableId(value = "account_id", type = IdType.INPUT)
    @Schema(description = "关联系统账号ID（主键）")
    private String accountId;
    @Schema(description = "认证状态：UNVERIFIED/PENDING/VERIFIED/REJECTED")
    private String status;
    @Schema(description = "证件类型：ID_CARD/PASSPORT 等")
    private String documentType;
    @Schema(description = "真实姓名密文（加密存储）")
    private String realNameCipher;
    @Schema(description = "证件号码密文（加密存储）")
    private String documentNoCipher;
    @Schema(description = "证件号码哈希（用于脱敏检索）")
    private String documentNoHash;
    @Schema(description = "认证通道：THIRD_PARTY/MANUAL")
    private String verifyChannel;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "第三方业务订单号")
    private String providerOrderNo;
    @Schema(description = "实名认证通过时间")
    private OffsetDateTime verifiedAt;
    @Schema(description = "来源实名工单ID")
    private String sourceCaseId;
    @Schema(description = "实名认证撤销时间")
    private OffsetDateTime revokedAt;
    @Schema(description = "撤销操作人账户ID")
    private String revokedBy;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
}
