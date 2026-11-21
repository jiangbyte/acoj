package io.charlie.web.modular.sys.conversation.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * LLM 工具轨迹分页参数。
 */
@Data
@Schema(name = "SysConversationToolTracePageParam", description = "工具轨迹分页参数")
public class SysConversationToolTracePageParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "工具名")
    private String toolName;
}
