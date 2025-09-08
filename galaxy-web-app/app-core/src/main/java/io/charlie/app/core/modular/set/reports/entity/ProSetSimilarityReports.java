package io.charlie.app.core.modular.set.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.app.core.modular.similarity.utils.DynamicCloneLevelDetector;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-08
* @description 题库题目报告库表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "pro_set_similarity_reports", autoResultMap = true)
@Schema(name = "ProSetSimilarityReports", description = "题库题目报告库表")
public class ProSetSimilarityReports extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "报告类型")
    private Integer reportType;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "题集ID")
    private String setId;

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> similarityDistribution;

    @Schema(description = "程度统计")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<DynamicCloneLevelDetector.CloneLevel> degreeStatistics;

    @Schema(description = "检测模式")
    private Integer checkMode;
}
