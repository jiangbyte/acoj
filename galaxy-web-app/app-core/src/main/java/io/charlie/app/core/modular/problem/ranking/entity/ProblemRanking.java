package io.charlie.app.core.modular.problem.ranking.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 03/08/2025
 * @description TODO
 */
@Data
@TableName(autoResultMap = true)
public class ProblemRanking implements Serializable, TransPojo {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "排名")
    private Integer ranking;

    @Schema(description = "题目ID")
    @TableId
    private String problemId;

    @Schema(description = "题目名称")
    private String title;

    @Schema(description = "参与用户数")
    private Integer participantCount;

    @Schema(description = "通过率")
    private BigDecimal acceptanceRate;

    @Schema(description = "提交数（submit_type = 1）")
    private Integer submissionCount;

    @Schema(description = "运行数（submit_type = 0）")
    private Integer executionCount;

    @Schema(description = "提交数（submit_type = 0和1的总和）")
    private Integer totalSubmissionCount;
}
