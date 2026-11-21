package io.charlie.web.modular.task.trace.result;

import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.modular.data.submit.entity.DataSubmit;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨工作流追溯结果（只读聚合）。
 */
@Data
@Schema(name = "WorkflowTraceResult", description = "跨工作流追溯结果")
public class WorkflowTraceResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "提交记录")
    private List<DataSubmit> submits = new ArrayList<>();

    @Schema(description = "Accepted 后样本 Token（data_library）")
    private List<DataLibrary> libraries = new ArrayList<>();

    @Schema(description = "克隆筛查结果对")
    private List<TaskSimilarity> similarities = new ArrayList<>();

    @Schema(description = "LLM 会话消息")
    private List<SysConversation> conversations = new ArrayList<>();

    @Schema(description = "LLM 工具调用轨迹")
    private List<SysConversationToolTrace> toolTraces = new ArrayList<>();
}
