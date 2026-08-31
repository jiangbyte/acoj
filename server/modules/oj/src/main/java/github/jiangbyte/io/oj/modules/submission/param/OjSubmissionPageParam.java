package github.jiangbyte.io.oj.modules.submission.param;

/**
 * OJ 提交分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjSubmission 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjSubmissionPageParam extends PageQuery {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "提交人账户ID")
    private String accountId;
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "状态：PENDING/JUDGING/AC/WA/...")
    private String status;
}
