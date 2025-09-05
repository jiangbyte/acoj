package io.charlie.app.core.modular.problem.similarity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
* @date 2025-09-05
* @description 题目检测结果任务库
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "pro_similarity_detail", autoResultMap = true)
@Schema(name = "ProSimilarityDetail", description = "题目检测结果任务库")
public class ProSimilarityDetail extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "手动")
    private Boolean taskType;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "相似度")
    private BigDecimal similarity;

    @Schema(description = "提交用户")
    private String submitUser;

    @Schema(description = "源代码")
    private String submitCode;

    @Schema(description = "源代码长度")
    private Integer submitCodeLength;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "提交用户Token名称")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> submitTokenName;

    @Schema(description = "提交用户Token内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private  List<String> submitTokenTexts;

    @Schema(description = "样本用户")
    private String originUser;

    @Schema(description = "样本源代码")
    private String originCode;

    @Schema(description = "样本源代码长度")
    private Integer originCodeLength;

    @Schema(description = "样本提交ID")
    private String originId;

    @Schema(description = "样本提交时间")
    private Date originTime;

    @Schema(description = "样本用户Token名称")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private  List<String> originTokenName;

    @Schema(description = "样本用户Token内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private  List<String> originTokenTexts;
}
