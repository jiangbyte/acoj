package io.charlie.web.modular.sys.conversation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.cores.pojo.CommonEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * LLM 工具调用轨迹。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_conversation_tool_trace")
@Schema(name = "SysConversationToolTrace", description = "LLM 工具调用轨迹")
public class SysConversationToolTrace extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "对话ID")
    private String conversationId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "工具名")
    private String toolName;

    @Schema(description = "工具参数JSON")
    private String toolArgs;

    @Schema(description = "工具返回摘要")
    private String toolResult;

    @Schema(description = "耗时毫秒")
    private Integer durationMs;

    @Schema(description = "SUCCESS/ERROR")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;
}
