package io.charlie.app.core.modular.set.problems.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description TODO
 */
@Data
@TableName("pro_set_problem")
@Schema(name = "ProSetProblem", description = "题单题目表")
public class ProSetProblem implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "题集ID")
    private String problemSetId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题单题目排序")
    private Integer sort;
}
