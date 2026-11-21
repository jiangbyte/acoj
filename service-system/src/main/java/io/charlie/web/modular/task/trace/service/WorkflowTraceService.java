package io.charlie.web.modular.task.trace.service;

import io.charlie.web.modular.task.trace.param.WorkflowTraceQueryParam;
import io.charlie.web.modular.task.trace.result.WorkflowTraceResult;

/**
 * 跨工作流追溯服务。
 */
public interface WorkflowTraceService {

    /**
     * 按题目追溯提交、Token、克隆结果与会话。
     */
    WorkflowTraceResult byProblem(WorkflowTraceQueryParam param);

    /**
     * 按用户追溯提交、Token、克隆结果与会话；可叠加题目过滤。
     */
    WorkflowTraceResult byUser(WorkflowTraceQueryParam param);
}
