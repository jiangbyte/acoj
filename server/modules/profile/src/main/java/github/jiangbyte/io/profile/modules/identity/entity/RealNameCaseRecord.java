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
 * 实名业务工单流水，对应表 {@code real_name_case_record}。
 *
 * Author: Charlie
 */
@Schema(description = "实名业务工单流水，对应表 real_name_case_record。")
@Data
@TableName("real_name_case_record")
public class RealNameCaseRecord {

    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    @Schema(description = "工单流水ID（主键）")
    private String recordId;
    @Schema(description = "关联实名工单ID")
    private String caseId;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "动作")
    private String action;
    @Schema(description = "动作前工单状态")
    private String statusBefore;
    @Schema(description = "动作后工单状态")
    private String statusAfter;
    @Schema(description = "认证通道：THIRD_PARTY（三方）/ MANUAL（人工）")
    private String verifyChannel;
    @Schema(description = "第三方服务提供方")
    private String provider;
    @Schema(description = "操作人账户ID")
    private String operatorId;
    @Schema(description = "操作所属部门ID")
    private String deptId;
    @Schema(description = "备注说明")
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
