package io.charlie.web.oj.modular.llm.param;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    @NotBlank(message = "conversantId不能为空")
    private String conversantId;
    
//    @NotBlank(message = "problemId不能为空")
    private String problemId;
    
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 消息类型
     * chat - 普通对话
     * generate_solution - 生成解题思路
     * analyze_code - 分析代码
     * explain_complexity - 解释时间复杂度
     * boundary_cases - 边界情况分析
     * optimize_code - 代码优化
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
}
