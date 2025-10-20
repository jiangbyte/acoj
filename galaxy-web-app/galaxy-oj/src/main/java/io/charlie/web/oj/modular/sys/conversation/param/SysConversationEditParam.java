package io.charlie.web.oj.modular.sys.conversation.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-20
* @description 大模型对话 编辑参数
*/
@Data
@Schema(name = "SysConversation", description = "大模型对话 编辑参数")
public class SysConversationEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "对话ID")
    private String conversationId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "题集对话")
    private Boolean isSet;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息角色")
    private String messageRole;

    @Schema(description = "消息内容")
    private String messageContent;

    @Schema(description = "用户代码")
    private String userCode;

    @Schema(description = "代码语言")
    private String language;

    @Schema(description = "提示Tokens")
    private Integer promptTokens;

    @Schema(description = "完成Tokens")
    private Integer completionTokens;

    @Schema(description = "总Tokens")
    private Integer totalTokens;

    @Schema(description = "响应时间")
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