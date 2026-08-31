package github.jiangbyte.io.oj.modules.problemcase.param;

/**
 * OJ 题目测例分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjProblemCase 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjProblemCasePageParam extends PageQuery {
    @Schema(description = "所属题目ID")
    private String problemId;
    @Schema(description = "测例包版本")
    private Integer caseVersion;
    @Schema(description = "题内测例号")
    private String caseKey;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
