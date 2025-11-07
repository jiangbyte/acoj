package io.charlie.web.oj.modular.data.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.galaxy.pojo.CommonEntity;

import java.math.BigDecimal;
import java.io.Serial;
import java.util.List;

import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.similarity.dto.CloneLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-21
 * @description 报告库表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "task_reports", autoResultMap = true)
@Schema(name = "TaskReports", description = "报告库表")
public class TaskReports extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "报告类型")
    @Trans(type = TransType.DICTIONARY, key = "REPORT_TYPE", ref = "reportTypeName")
    private Integer reportType;

    @Schema(description = "报告类型名称")
    @TableField(exist = false)
    private String reportTypeName;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "模块类型")
    @Trans(type = TransType.DICTIONARY, key = "CODE_SUBMIT_MODULE")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

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
    private List<CloneLevel> degreeStatistics;

    @Schema(description = "检测模式")
    @Trans(type = TransType.DICTIONARY, key = "CHECK_MODE", ref = "checkModeName")
    private Integer checkMode;

    @Schema(description = "检测模式名称")
    @TableField(exist = false)
    private String checkModeName;
}
