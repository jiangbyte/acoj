package io.charlie.app.core.modular.problem.similarity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
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
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.anno.UnTrans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.constant.UnTransType;

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
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "taskTypeName")
    private Boolean taskType;

    @Schema(description = "名称")
    @TableField(exist = false)
    private String taskTypeName;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = ProProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "编程语言")
    @Trans(type = TransType.DICTIONARY, key = "ALLOW_LANGUAGE", ref = "languageName")
    private String language;

    @Schema(description = "语言名称")
    @TableField(exist = false)
    private String languageName;

    @Schema(description = "相似度")
    private BigDecimal similarity;

    @Schema(description = "提交用户")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"submitUserName", "submitUserAvatar"})
    private String submitUser;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String submitUserName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String submitUserAvatar;

    @Schema(description = "源代码")
    private String submitCode;

    @Schema(description = "源代码长度")
    private Integer submitCodeLength;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "提交时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date submitTime;

    @Schema(description = "提交用户Token名称")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> submitTokenName;

    @Schema(description = "提交用户Token内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> submitTokenTexts;

    @Schema(description = "样本用户")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"originUserName", "originUserAvatar"})
    private String originUser;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String originUserName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String originUserAvatar;

    @Schema(description = "样本源代码")
    private String originCode;

    @Schema(description = "样本源代码长度")
    private Integer originCodeLength;

    @Schema(description = "样本提交ID")
    private String originId;

    @Schema(description = "样本提交时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date originTime;

    @Schema(description = "样本用户Token名称")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> originTokenName;

    @Schema(description = "样本用户Token内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> originTokenTexts;
}
