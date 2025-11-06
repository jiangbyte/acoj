package io.charlie.web.oj.modular.data.reports.param;

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
* @date 2025-09-21
* @description 报告库 增加参数
*/
@Data
@Schema(name = "TaskReports", description = "报告库 增加参数")
public class TaskReportsAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "报告类型")
    private Integer reportType;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "模块类型")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "样例数量")
    private Integer sampleCount;

    @Schema(description = "相似组数量")
    private Integer similarityGroupCount;

    @Schema(description = "平均相似度")
    private BigDecimal avgSimilarity;

    @Schema(description = "最大相似度")
    private BigDecimal maxSimilarity;

    @Schema(description = "检测阈值")
    private BigDecimal threshold;

    @Schema(description = "相似度分布")
    private String similarityDistribution;

    @Schema(description = "程度统计")
    private String degreeStatistics;

    @Schema(description = "检测模式")
    private Integer checkMode;

}