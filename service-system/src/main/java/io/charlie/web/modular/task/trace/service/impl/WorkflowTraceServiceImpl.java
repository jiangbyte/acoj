package io.charlie.web.modular.task.trace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.cores.exception.BusinessException;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.modular.data.submit.entity.DataSubmit;
import io.charlie.web.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationMapper;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import io.charlie.web.modular.task.trace.param.WorkflowTraceQueryParam;
import io.charlie.web.modular.task.trace.result.WorkflowTraceResult;
import io.charlie.web.modular.task.trace.service.WorkflowTraceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 跨工作流追溯：按 problem / user 聚合只读查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowTraceServiceImpl implements WorkflowTraceService {

    private final DataSubmitMapper dataSubmitMapper;
    private final DataLibraryMapper dataLibraryMapper;
    private final TaskSimilarityMapper taskSimilarityMapper;
    private final SysConversationMapper sysConversationMapper;
    private final SysConversationToolTraceMapper toolTraceMapper;

    @Override
    @DS("slave")
    public WorkflowTraceResult byProblem(WorkflowTraceQueryParam param) {
        // 1. 校验 problemId
        // 2. 分别按 limit 拉取四类记录（禁止 N+1）
        // 3. 组装结果并标注会话关联边界
        if (StrUtil.isBlank(param.getProblemId())) {
            throw new BusinessException("题目ID不能为空");
        }
        return query(param.getProblemId(), param.getUserId(), resolveLimit(param.getLimit()));
    }

    @Override
    @DS("slave")
    public WorkflowTraceResult byUser(WorkflowTraceQueryParam param) {
        // 1. 校验 userId
        // 2. 可选叠加 problemId
        // 3. 聚合四类记录
        if (StrUtil.isBlank(param.getUserId())) {
            throw new BusinessException("用户ID不能为空");
        }
        return query(param.getProblemId(), param.getUserId(), resolveLimit(param.getLimit()));
    }

    /**
     * 按题目/用户组合查询四类工作流数据。
     */
    private WorkflowTraceResult query(String problemId, String userId, int limit) {
        WorkflowTraceResult result = new WorkflowTraceResult();
        result.setProblemId(problemId);
        result.setUserId(userId);

        LambdaQueryWrapper<DataSubmit> submitQw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(problemId)) {
            submitQw.eq(DataSubmit::getProblemId, problemId);
        }
        if (StrUtil.isNotBlank(userId)) {
            submitQw.eq(DataSubmit::getUserId, userId);
        }
        submitQw.orderByDesc(DataSubmit::getCreateTime).last("LIMIT " + limit);
        result.setSubmits(dataSubmitMapper.selectList(submitQw));

        LambdaQueryWrapper<DataLibrary> libraryQw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(problemId)) {
            libraryQw.eq(DataLibrary::getProblemId, problemId);
        }
        if (StrUtil.isNotBlank(userId)) {
            libraryQw.eq(DataLibrary::getUserId, userId);
        }
        libraryQw.orderByDesc(DataLibrary::getCreateTime).last("LIMIT " + limit);
        result.setLibraries(dataLibraryMapper.selectList(libraryQw));

        LambdaQueryWrapper<TaskSimilarity> simQw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(problemId)) {
            simQw.eq(TaskSimilarity::getProblemId, problemId);
        }
        if (StrUtil.isNotBlank(userId)) {
            simQw.and(w -> w.eq(TaskSimilarity::getSubmitUser, userId)
                    .or()
                    .eq(TaskSimilarity::getOriginUser, userId));
        }
        simQw.orderByDesc(TaskSimilarity::getSimilarity).last("LIMIT " + limit);
        result.setSimilarities(taskSimilarityMapper.selectList(simQw));

        LambdaQueryWrapper<SysConversation> convQw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(problemId)) {
            convQw.eq(SysConversation::getProblemId, problemId);
        }
        if (StrUtil.isNotBlank(userId)) {
            convQw.eq(SysConversation::getUserId, userId);
        }
        convQw.orderByDesc(SysConversation::getCreateTime).last("LIMIT " + limit);
        result.setConversations(sysConversationMapper.selectList(convQw));

        LambdaQueryWrapper<SysConversationToolTrace> toolQw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(problemId)) {
            toolQw.eq(SysConversationToolTrace::getProblemId, problemId);
        }
        if (StrUtil.isNotBlank(userId)) {
            toolQw.eq(SysConversationToolTrace::getUserId, userId);
        }
        toolQw.orderByDesc(SysConversationToolTrace::getCreateTime).last("LIMIT " + limit);
        result.setToolTraces(toolTraceMapper.selectList(toolQw));

        return result;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 50;
        }
        return Math.min(limit, 100);
    }
}
