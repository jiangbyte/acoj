package github.jiangbyte.io.oj.modules.judge.dispatch.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * OJ 派发审计实体，对应表 {@code oj_judge_dispatch}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 派发审计")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_judge_dispatch", autoResultMap = true)
public class OjJudgeDispatch extends BaseEntity {
    @Schema(description = "提交ID")
    private String submissionId;
    @Schema(description = "执行机ID")
    private String nodeId;
    @Schema(description = "派发时节点 epoch")
    private Long nodeEpoch;
    @Schema(description = "该提交第几次派发")
    private Integer attemptNo;
    @Schema(description = "Worker 实例 ID")
    private String workerId;
    @Schema(description = "链路追踪 ID")
    private String requestId;
    @Schema(description = "开始时间")
    private OffsetDateTime startedAt;
    @Schema(description = "结束时间")
    private OffsetDateTime finishedAt;
    @Schema(description = "耗时毫秒")
    private Integer durationMs;
    @Schema(description = "结果：STARTED/SUCCESS_RESULT/TRANSPORT_FAIL/SANDBOX_INTERNAL/CANCELLED_LEASE/TIMEOUT")
    private String outcome;
    @Schema(description = "HTTP 状态码")
    private Integer httpStatus;
    @Schema(description = "错误码")
    private String errorCode;
    @Schema(description = "错误摘要（脱敏）")
    private String errorMessage;
    @Schema(description = "本轮用户结果 AC/WA/...；无则空")
    private String userVerdict;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "摘要扩展")
    private Map<String, Object> extra;
}
