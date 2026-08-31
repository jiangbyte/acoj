package github.jiangbyte.io.oj.modules.judge.dispatch.param;

/**
 * OJ 派发审计分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjJudgeDispatch 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjJudgeDispatchPageParam extends PageQuery {
    @Schema(description = "提交ID")
    private String submissionId;
    @Schema(description = "执行机ID")
    private String nodeId;
    @Schema(description = "结果：SUCCESS_RESULT/TRANSPORT_FAIL/...")
    private String outcome;
}
