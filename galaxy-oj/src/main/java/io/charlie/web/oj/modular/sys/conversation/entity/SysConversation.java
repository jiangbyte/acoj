package io.charlie.web.oj.modular.sys.conversation.entity;

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

import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-20
* @description 大模型对话表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_conversation")
@Schema(name = "SysConversation", description = "大模型对话表")
public class SysConversation extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "对话ID")
    private String conversationId;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "题集ID")
    @Trans(type = TransType.SIMPLE, target = DataSet.class, fields = "title", ref = "setIdName")
    private String setId;

    @Schema(description = "题集名称")
    @TableField(exist = false)
    private String setIdName;

    @Schema(description = "是否是题集提交")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isSet;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"userIdName", "userAvatar"})
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String userAvatar;

    @Schema(description = "消息类型")
    @Trans(type = TransType.DICTIONARY, key = "MESSAGE_TYPE")
    private String messageType;

    @Schema(description = "消息角色")
    @Trans(type = TransType.DICTIONARY, key = "MESSAGE_ROLE")
    private String messageRole;

    @Schema(description = "消息内容")
    private String messageContent;

    @Schema(description = "用户代码")
    private String userCode;

    @Schema(description = "编程语言")
    @Trans(type = TransType.DICTIONARY, key = "ALLOW_LANGUAGE", ref = "languageName")
    private String language;

    @Schema(description = "语言名称")
    @TableField(exist = false)
    private String languageName;

    @Schema(description = "提示Tokens")
    private Integer promptTokens;

    @Schema(description = "完成Tokens")
    private Integer completionTokens;

    @Schema(description = "总Tokens")
    private Integer totalTokens;

    @Schema(description = "响应时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date responseTime;

    @Schema(description = "流式传输总耗时")
    private Integer streamingDuration;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "用户平台")
    private String userPlatform;

    @Schema(description = "IP地址")
    private String ipAddress;
}
