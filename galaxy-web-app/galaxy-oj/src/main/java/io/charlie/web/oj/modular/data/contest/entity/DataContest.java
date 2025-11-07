package io.charlie.web.oj.modular.data.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "data_contest", autoResultMap = true)
@Schema(name = "DataContest", description = "竞赛表")
public class DataContest extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "竞赛标题")
    private String title;

    @Schema(description = "竞赛描述")
    private String description;

    @Schema(description = "竞赛类型: ACM/OI/OIO")
    @Trans(type = TransType.DICTIONARY, key = "CONTEST_TYPE")
    private String contestType;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "最大团队成员数")
    private Integer maxTeamMembers;

    @Schema(description = "是否团队赛")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isTeamContest;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisible;

    @Schema(description = "是否公开")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isPublic;

    @Schema(description = "访问密码")
    private String password;

    @Schema(description = "报名开始时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date registerStartTime;

    @Schema(description = "报名结束时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date registerEndTime;

    @Schema(description = "竞赛开始时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date contestStartTime;

    @Schema(description = "竞赛结束时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date contestEndTime;

    @Schema(description = "封榜时间(分钟)")
    private Integer frozenTime;

    @Schema(description = "罚时(分钟)")
    private Integer penaltyTime;

    @Schema(description = "允许语言")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allowedLanguages;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

    @TableField(exist = false)
    private Boolean isAuth;
}
