package github.jiangbyte.io.profile.modules.identity.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 实名业务工单，对应表 {@code real_name_case}。
 *
 * Author: Charlie
 */
@Schema(description = "实名业务工单，对应表 real_name_case。")
@Data
@TableName(value = "real_name_case", autoResultMap = true)
public class RealNameCase {

    @TableId(value = "case_id", type = IdType.ASSIGN_ID)
    @Schema(description = "实名工单ID（主键）")
    private String caseId;
    @Schema(description = "业务类型：ACCOUNT_VERIFY/ACCOUNT_RECOVERY")
    private String businessType;
    @Schema(description = "认证通道：THIRD_PARTY/MANUAL")
    private String verifyChannel;
    @Schema(description = "工单状态：PENDING/APPROVED/REJECTED 等")
    private String status;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "目标账户提示信息密文")
    private String targetAccountHintCipher;
    @Schema(description = "申请人联系方式密文")
    private String applicantContactCipher;
    @Schema(description = "证件类型：ID_CARD/PASSPORT 等")
    private String documentType;
    @Schema(description = "真实姓名密文")
    private String realNameCipher;
    @Schema(description = "证件号码密文")
    private String documentNoCipher;
    @Schema(description = "证件号码哈希")
    private String documentNoHash;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "附件ID列表（JSON数组）")
    private List<String> attachmentIds = new ArrayList<>();
    @Schema(description = "扩展业务载荷密文")
    private String payloadCipher;
    @Schema(description = "受理部门ID")
    private String handlerDeptId;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "第三方业务订单号")
    private String providerOrderNo;
    @Schema(description = "提交人账户ID")
    private String submitterId;
    @Schema(description = "审核人账户ID")
    private String reviewerId;
    @Schema(description = "审核完成时间")
    private OffsetDateTime reviewedAt;
    @Schema(description = "审核驳回原因")
    private String rejectReason;
    @Schema(description = "过期时间")
    private OffsetDateTime expireAt;
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
