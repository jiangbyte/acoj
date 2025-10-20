package io.charlie.web.oj.modular.llm.param;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    @NotBlank(message = "conversantId不能为空")
    private String conversationId;
    
//    @NotBlank(message = "problemId不能为空")
    private String problemId;
    
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 消息类型
     * chat - 普通对话
     * generate-solution-idea - 生成解题思路
     * optimize-code - 优化用户代码
     * analyze-problem-boundary-conditions - 分析题目边界条件
     * analyze-user-code-complexity - 分析用户代码复杂度
     */
//    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 用户代码（用于代码分析）
     */
    private String userCode;

    /**
     * 编程语言
     */
    private String language;

    private Boolean isSet;

    private String setId;

    /**
     * 转换为Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", this.conversationId);
        map.put("problemId", this.problemId);
        map.put("message", this.message);
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
        if (this.message != null) map.put("message", this.message);
        if (this.messageType != null) map.put("messageType", this.messageType);
        if (this.userCode != null) map.put("userCode", this.userCode);
        if (this.language != null) map.put("language", this.language);
        if (this.isSet != null) map.put("isSet", this.isSet);
        if (this.setId != null) map.put("setId", this.setId);
        return map;
    }
}
