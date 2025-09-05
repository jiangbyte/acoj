package io.charlie.app.core.modular.set.ranking.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SetRanking implements Serializable, TransPojo {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "排名")
    private Integer ranking;

    @Schema(description = "题集ID")
    @TableId
    private String setId;

    @Schema(description = "题集名称")
    private String title;

    @Schema(description = "题集封面")
    private String cover;

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