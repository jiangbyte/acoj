package github.jiangbyte.io.oj.modules.problemsolution.param;

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OJ 参考答案分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "OjProblemSolution 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjProblemSolutionPageParam extends PageQuery {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
