package github.jiangbyte.io.oj.modules.problemdryrun.param;

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 试跑历史分页入参。
 *
 * Author: Charlie
 */
@Schema(description = "OjProblemDryRun 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjProblemDryRunPageParam extends PageQuery {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "SINGLE/ALL")
    private String mode;
    @Schema(description = "PROBLEM/RELAXED")
    private String limitMode;
    @Schema(description = "整单结果")
    private String overallStatus;
    @Schema(description = "语言 key")
    private String language;
}
