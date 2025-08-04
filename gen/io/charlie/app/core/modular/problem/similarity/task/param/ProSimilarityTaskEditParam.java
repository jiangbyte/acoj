package io.charlie.app.core.modular.problem.similarity.task.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 代码相似度检测任务 编辑参数
*/
@Data
@Schema(name = "ProSimilarityTask", description = "代码相似度检测任务 编辑参数")
public class ProSimilarityTaskEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "比较范围")
    private String compareRange;

    @Schema(description = "近期天数")
    private Integer daysBefore;

    @Schema(description = "比较提交数")
    private Integer totalCompared;

    @Schema(description = "最大相似度")
    private BigDecimal maxSimilarity;

    @Schema(description = "手动任务")
    private Boolean isManual;

}