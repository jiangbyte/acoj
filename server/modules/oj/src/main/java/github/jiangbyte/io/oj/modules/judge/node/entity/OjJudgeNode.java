package github.jiangbyte.io.oj.modules.judge.node.entity;

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
 * OJ 执行机实体，对应表 {@code oj_judge_node}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 执行机（SparkSandbox）")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_judge_node", autoResultMap = true)
public class OjJudgeNode extends BaseEntity {
    @Schema(description = "节点编码")
    private String code;
    @Schema(description = "展示名")
    private String name;
    @Schema(description = "SparkSandbox 根地址")
    private String baseUrl;
    @Schema(description = "是否对该节点验签")
    private Boolean signingEnabled;
    @Schema(description = "节点密钥密文；空则用全局默认")
    private String signingSecretCipher;
    @Schema(description = "管理状态：ENABLED/DISABLED/DRAINING")
    private String adminStatus;
    @Schema(description = "运行态：ONLINE/OFFLINE/UNHEALTHY")
    private String runtimeStatus;
    @Schema(description = "熔断状态：CLOSED/OPEN/HALF_OPEN")
    private String circuitState;
    @Schema(description = "熔断打开时间")
    private OffsetDateTime circuitOpenedAt;
    @Schema(description = "进入半开时间")
    private OffsetDateTime circuitHalfOpenAt;
    @Schema(description = "调度权重，越大越易被选中")
    private Integer weight;
    @Schema(description = "并列时优先级，越小越优先")
    private Integer priority;
    @Schema(description = "ACOJ 侧最大在途")
    private Integer maxConcurrency;
    @Schema(description = "持久化在途")
    private Integer inflightCount;
    @Schema(description = "节点世代；OFFLINE/硬故障时递增")
    private Long epoch;
    @Schema(description = "累计派发")
    private Long totalDispatch;
    @Schema(description = "累计成功产出用户结果")
    private Long totalSuccess;
    @Schema(description = "累计传输失败")
    private Long totalTransportFail;
    @Schema(description = "连续传输/探活失败")
    private Integer consecutiveFailCount;
    @Schema(description = "最近探活成功")
    private OffsetDateTime lastHeartbeatAt;
    @Schema(description = "最近被选中")
    private OffsetDateTime lastSelectedAt;
    @Schema(description = "最近 SUCCESS_RESULT")
    private OffsetDateTime lastSuccessAt;
    @Schema(description = "最近错误时间")
    private OffsetDateTime lastErrorAt;
    @Schema(description = "最近错误摘要")
    private String lastErrorMessage;
    @Schema(description = "最近探活 RTT")
    private Integer probeLatencyMs;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "支持语言；空数组=全语言")
    private List<String> supportedLanguages;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "机房/AZ/备注等")
    private Map<String, Object> extra;
}
