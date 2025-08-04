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
 * @date 28/07/2025
 * @description 题目排行实体类
 */
@Data
@TableName(autoResultMap = true)
public class ProblemUserRanking implements Serializable, TransPojo {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "排名")
    private Integer ranking;

    @Schema(description = "用户ID")
    @TableId
    private String userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "解决的总题目数")
    private Integer solvedCount;

    @Schema(description = "提交过的总题目数")
    private Integer attemptedCount;

    @Schema(description = "通过率")
    private BigDecimal acceptanceRate;

    @Schema(description = "提交数（submit_type = 1）")
    private Integer submissionCount;

    @Schema(description = "运行数（submit_type = 0）")
    private Integer executionCount;

    @Schema(description = "提交数（submit_type = 0和1的总和）")
    private Integer totalSubmissionCount;

}