package io.charlie.app.core.modular.problem.relation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 27/07/2025
 * @description 题目-标签
 */
@Data
@TableName("pro_problem_tag")
@Schema(name = "ProProblemTag", description = "题目-标签 关联表(1-N)")
public class ProProblemTag  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "标签ID")
    private String tagId;
}