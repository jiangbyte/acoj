package io.charlie.web.modular.sys.conversation.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 按会话 ID 删除时的入参。
 */
@Data
@Schema(name = "SysConversationSessionIdParam", description = "大模型对话会话 ID 参数")
public class SysConversationSessionIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "对话ID不能为空")
    @Schema(description = "对话ID")
    private String conversationId;
}
