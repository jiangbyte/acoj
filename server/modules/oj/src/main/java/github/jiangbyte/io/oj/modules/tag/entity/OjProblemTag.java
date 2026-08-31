package github.jiangbyte.io.oj.modules.tag.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OJ 题目-标签关联实体，对应表 {@code oj_problem_tag}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目-标签关联")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_problem_tag")
public class OjProblemTag extends BaseEntity {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "标签ID")
    private String tagId;
}
