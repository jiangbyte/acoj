package github.jiangbyte.io.oj.modules.judge.node.param;

/**
 * OJ 执行机分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjJudgeNode 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjJudgeNodePageParam extends PageQuery {
    @Schema(description = "节点编码")
    private String code;
    @Schema(description = "展示名")
    private String name;
    @Schema(description = "管理状态：ENABLED/DISABLED/DRAINING")
    private String adminStatus;
    @Schema(description = "运行态：ONLINE/OFFLINE/UNHEALTHY")
    private String runtimeStatus;
    @Schema(description = "熔断状态：CLOSED/OPEN/HALF_OPEN")
    private String circuitState;
}
