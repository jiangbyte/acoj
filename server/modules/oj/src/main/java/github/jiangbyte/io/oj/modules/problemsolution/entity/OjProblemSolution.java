package github.jiangbyte.io.oj.modules.problemsolution.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OJ 题目参考答案实体，对应表 {@code oj_problem_solution}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目参考答案")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oj_problem_solution")
public class OjProblemSolution extends BaseEntity {
    @Schema(description = "所属题目ID")
    private String problemId;
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "参考答案源码")
    private String source;
    @Schema(description = "是否同题默认答案")
    private Boolean isDefault;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
