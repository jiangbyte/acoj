package github.jiangbyte.io.oj.modules.problem.param;

/**
 * OJ 题目分页查询入参。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "OjProblem 分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class OjProblemPageParam extends PageQuery {
    @Schema(description = "对外题号，如 P1001")
    private String problemKey;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "难度：EASY/MEDIUM/HARD")
    private String difficulty;
    @Schema(description = "状态：DRAFT/PUBLISHED/DISABLED")
    private String status;
    @Schema(description = "本人做题状态过滤：ACCEPTED/ATTEMPTED/UNTRIED")
    private String myStatus;
    @Schema(description = "标签 ID 过滤（单选，AND）")
    private String tagId;
    @Schema(description = "关键词（题号或标题，OR）")
    private String keyword;
}
