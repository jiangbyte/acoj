package io.charlie.web.oj.modular.data.set.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.charlie.web.oj.modular.sys.category.entity.SysCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_set")
@Schema(name = "DataSet", description = "题集")
public class DataSet extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "题集类型")
    @Trans(type = TransType.DICTIONARY, key = "PROBLEM_SET_TYPE", ref = "setTypeName")
    private Integer setType;

    @Schema(description = "题集类型名称")
    @TableField(exist = false)
    private String setTypeName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类")
    @Trans(type = TransType.SIMPLE, target = SysCategory.class, fields = "name", ref = "categoryName")
    private String categoryId;

    @Schema(description = "分类名称")
    @TableField(exist = false)
    private String categoryName;

    @Schema(description = "难度")
    @Trans(type = TransType.DICTIONARY, key = "PROBLEM_DIFFICULTY", ref = "difficultyName")
    private Integer difficulty;

    @Schema(description = "难度名称")
    @TableField(exist = false)
    private String difficultyName;

    @Schema(description = "开始时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date endTime;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisible;

    @Schema(description = "是否使用AI")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean useAi;

    @Schema(description = "额外的信息")
    private String exJson;

    @Schema(description = "排行")
    @TableField(exist = false)
    private Long rank;

    @TableField(exist = false)
    private List<String> problemIds;

    @Schema(description = "通过率")
    @TableField(exist = false)
    private Double avgAcceptance;

    // 提交人数
    @Schema(description = "提交人数")
    @TableField(exist = false)
    private Long submitUserCount;

    // 参与人数
    @Schema(description = "参与人数")
    @TableField(exist = false)
    private Long participantUserCount;

}
