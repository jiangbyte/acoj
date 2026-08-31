package github.jiangbyte.io.oj.modules.submission.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * OJ 提交实体，对应表 {@code oj_submission}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 提交")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_submission", autoResultMap = true)
public class OjSubmission extends BaseEntity {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "提交人账户ID")
    private String accountId;
    @Schema(description = "语言 key，与 SparkSandbox 一致")
    private String language;
    @Schema(description = "源代码")
    private String sourceCode;
    @Schema(description = "提交时题目测例版本快照")
    private Integer caseVersion;
    @Schema(description = "状态：PENDING/JUDGING/AC/WA/TLE/MLE/OLE/RE/CE/SE")
    private String status;
    @Schema(description = "得分；P0: AC=100 否则 0")
    private Integer score;
    @Schema(description = "测点耗时汇总")
    private Integer timeMs;
    @Schema(description = "测点内存峰值汇总")
    private Long memoryBytes;
    @Schema(description = "编译输出（CE）")
    private String compileOutput;
    @Schema(description = "简短说明")
    private String judgeMessage;
    @Schema(description = "用户备注")
    private String note;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "业务裁决后的测点摘要数组")
    private List<Map<String, Object>> caseResults;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "截断后的执行侧摘要（排障）")
    private Map<String, Object> sandboxRaw;
    @Schema(description = "入队时间")
    private OffsetDateTime queuedAt;
    @Schema(description = "终态时间")
    private OffsetDateTime judgedAt;
    @Schema(description = "当前/最后一次派发节点")
    private String judgeNodeId;
    @Schema(description = "已派发次数（含换机）")
    private Integer dispatchCount;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "已尝试节点 ID 列表")
    private List<String> triedNodeIds;
    @Schema(description = "领取租约截止")
    private OffsetDateTime judgeLeaseUntil;
    @Schema(description = "Worker 实例 ID")
    private String judgeLeaseOwner;
    @Schema(description = "本次领取 token；终态 CAS 校验")
    private String judgeToken;
    @Schema(description = "调度/传输错误摘要")
    private String lastDispatchError;
    @Schema(description = "退避重试时间")
    private OffsetDateTime nextRetryAt;
    @Schema(description = "错误码，如 NODE_UNAVAILABLE")
    private String errorCode;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
}
