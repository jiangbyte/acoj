package io.charlie.web.modular.data.contest.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.cores.config.timestamp.DateToTimestampSerializer;
import io.charlie.cores.config.timestamp.TimestampToDateDeserializer;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.Date;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与 编辑参数
*/
@Data
@Schema(name = "DataContestParticipant", description = "竞赛参与 编辑参数")
public class DataContestParticipantEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "竞赛ID")
    private String contestId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "团队ID")
    private String teamId;

    @Schema(description = "团队名称")
    private String teamName;

    @Schema(description = "是否队长")
    private Boolean isTeamLeader;

    @Schema(description = "报名时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date registerTime;

    @Schema(description = "状态")
    private String status;

}