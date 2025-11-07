package io.charlie.web.oj.modular.data.contest.entity;

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

import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_contest_participant")
@Schema(name = "DataContestParticipant", description = "竞赛参与表")
public class DataContestParticipant extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private String id;

    @Schema(description = "竞赛ID")
    @Trans(type = TransType.SIMPLE, target = DataContest.class, fields = {"title"}, refs = {"contestTitle"})
    private String contestId;

    @Schema(description = "")
    @TableField(exist = false)
    private String contestTitle;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"userIdName", "userAvatar"})
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String userAvatar;

    @Schema(description = "团队ID")
    private String teamId;

    @Schema(description = "团队名称")
    private String teamName;

    @Schema(description = "是否队长")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isTeamLeader;

    @Schema(description = "报名时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date registerTime;

    @Schema(description = "状态")
    private String status;
}
