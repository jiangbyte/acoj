package io.charlie.web.oj.modular.data.contest.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛 增加参数
*/
@Data
@Schema(name = "DataContest", description = "竞赛 增加参数")
public class DataContestAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "竞赛标题")
    private String title;

    @Schema(description = "竞赛描述")
    private String description;

    @Schema(description = "竞赛类型: ACM/OI/OIO")
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
    private Boolean isTeamContest;

    @Schema(description = "是否公开")
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
    private List<String> allowedLanguages;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

}