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
    
//    @NotBlank(message = "消息内容不能为空")
    private String message;

//    @NotBlank(message = "消息类型不能为空")
    private String messageType;
}
