package github.jiangbyte.io.oj.modules.problemdryrun.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * OJ 管理端试跑历史实体，对应表 {@code oj_problem_dry_run}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目试跑历史")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_problem_dry_run", autoResultMap = true)
public class OjProblemDryRun extends BaseEntity {
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "试跑时测例版本")
    private Integer caseVersion;
    @Schema(description = "SINGLE/ALL")
    private String mode;
    @Schema(description = "SINGLE 时测例号")
    private String caseKey;
    @Schema(description = "PROBLEM/RELAXED")
    private String limitMode;
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "实际执行源码快照")
    private String source;
    @Schema(description = "STORED/OVERRIDE")
    private String sourceFrom;
    @Schema(description = "整单结果")
    private String overallStatus;
    @Schema(description = "测点耗时峰值")
    private Integer maxTimeMs;
    @Schema(description = "测点内存峰值")
    private Long maxMemoryBytes;
    @Schema(description = "建议时限毫秒")
    private Integer suggestedTimeMs;
    @Schema(description = "建议内存字节")
    private Long suggestedMemoryBytes;
    @Schema(description = "本次传给沙箱的 CPU 时限")
    private Integer appliedTimeMs;
    @Schema(description = "本次传给沙箱的内存限额")
    private Long appliedMemoryBytes;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "每测例结果摘要")
    private List<Map<String, Object>> caseResults;
    @Schema(description = "执行机ID")
    private String nodeId;
    @Schema(description = "错误摘要")
    private String errorMessage;
}
