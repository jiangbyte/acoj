package io.charlie.app.core.modular.set.problems.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description 题单题目ID参数
 */
@Data
public class ProSetProblemIdParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "题集ID")
    private String problemSetId;

    @Schema(description = "题目ID")
    private String problemId;

}
