package io.charlie.web.oj.modular.llm.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    @NotBlank(message = "conversantId不能为空")
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

//    @Schema(description = "提示Tokens")
//    private Integer promptTokens;
//
//    @Schema(description = "完成Tokens")
//    private Integer completionTokens;
//
//    @Schema(description = "总Tokens")
//    private Integer totalTokens;
//
//    @Schema(description = "响应时间")
//    private Date responseTime;
//
//    @Schema(description = "流式传输总耗时")
//    private Integer streamingDuration;
//
//    @Schema(description = "状态")
//    private String status;
//
//    @Schema(description = "错误信息")
//    private String errorMessage;
//
//    @Schema(description = "用户平台")
//    private String userPlatform;
//
//    @Schema(description = "IP地址")
//    private String ipAddress;

    /**
     * 转换为Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", this.conversationId);
        map.put("problemId", this.problemId);
        map.put("message", this.messageContent);
        map.put("messageType", this.messageType);
        map.put("userCode", this.userCode);
        map.put("language", this.language);
        map.put("isSet", this.isSet);
        map.put("setId", this.setId);
        return map;
    }

    /**
     * 转换为Map，只包含非空字段
     */
    public Map<String, Object> toMapExcludeNull() {
        Map<String, Object> map = new HashMap<>();
        if (this.conversationId != null) map.put("conversationId", this.conversationId);
        if (this.problemId != null) map.put("problemId", this.problemId);
        if (this.messageContent != null) map.put("message", this.messageContent);
        if (this.messageType != null) map.put("messageType", this.messageType);
        if (this.userCode != null) map.put("userCode", this.userCode);
        if (this.language != null) map.put("language", this.language);
        if (this.isSet != null) map.put("isSet", this.isSet);
        if (this.setId != null) map.put("setId", this.setId);
        return map;
    }
}
