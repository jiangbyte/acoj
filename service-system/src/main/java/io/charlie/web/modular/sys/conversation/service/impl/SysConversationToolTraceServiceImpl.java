package io.charlie.web.modular.sys.conversation.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.cores.pojo.CommonPageRequest;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationMapper;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import io.charlie.web.modular.sys.conversation.param.SysConversationToolTracePageParam;
import io.charlie.web.modular.sys.conversation.service.SysConversationToolTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * LLM 工具调用轨迹查询实现。
 */
@Service
@RequiredArgsConstructor
public class SysConversationToolTraceServiceImpl
        extends ServiceImpl<SysConversationToolTraceMapper, SysConversationToolTrace>
        implements SysConversationToolTraceService {

    private final SysConversationMapper sysConversationMapper;

    @Override
    @DS("slave")
    public Page<SysConversationToolTrace> page(SysConversationToolTracePageParam param) {
        // 1. 按 conversationId / user / problem / tool 精确过滤
        // 2. 若按 conversationId 查不到，回退到该会话消息时间窗内的未绑定轨迹（兼容旧 ThreadLocal 丢上下文数据）
        // 3. 按创建时间倒序分页
        Page<SysConversationToolTrace> pageReq = CommonPageRequest.Page(
                Optional.ofNullable(param.getCurrent()).orElse(1),
                Optional.ofNullable(param.getSize()).orElse(20),
                null);

        QueryWrapper<SysConversationToolTrace> queryWrapper = buildFilter(param);
        queryWrapper.lambda().orderByDesc(SysConversationToolTrace::getCreateTime);
        Page<SysConversationToolTrace> result = this.page(pageReq, queryWrapper);

        if (result.getTotal() > 0 || StrUtil.isBlank(param.getConversationId())) {
            return result;
        }

        Date[] window = resolveSessionTimeWindow(param.getConversationId());
        if (window == null) {
            return result;
        }

        QueryWrapper<SysConversationToolTrace> orphanQw = new QueryWrapper<SysConversationToolTrace>().checkSqlInjection();
        orphanQw.lambda()
                .and(w -> w.isNull(SysConversationToolTrace::getConversationId)
                        .or()
                        .eq(SysConversationToolTrace::getConversationId, ""))
                .ge(SysConversationToolTrace::getCreateTime, window[0])
                .le(SysConversationToolTrace::getCreateTime, window[1])
                .orderByDesc(SysConversationToolTrace::getCreateTime);
        return this.page(pageReq, orphanQw);
    }

    /**
     * 组装精确过滤条件。
     */
    private QueryWrapper<SysConversationToolTrace> buildFilter(SysConversationToolTracePageParam param) {
        QueryWrapper<SysConversationToolTrace> queryWrapper = new QueryWrapper<SysConversationToolTrace>().checkSqlInjection();
        if (StrUtil.isNotBlank(param.getConversationId())) {
            queryWrapper.lambda().eq(SysConversationToolTrace::getConversationId, param.getConversationId());
        }
        if (StrUtil.isNotBlank(param.getUserId())) {
            queryWrapper.lambda().eq(SysConversationToolTrace::getUserId, param.getUserId());
        }
        if (StrUtil.isNotBlank(param.getProblemId())) {
            queryWrapper.lambda().eq(SysConversationToolTrace::getProblemId, param.getProblemId());
        }
        if (StrUtil.isNotBlank(param.getToolName())) {
            queryWrapper.lambda().eq(SysConversationToolTrace::getToolName, param.getToolName());
        }
        return queryWrapper;
    }

    /**
     * 根据会话消息推算工具轨迹可能落在的时间窗（前后各放宽 2 分钟）。
     */
    private Date[] resolveSessionTimeWindow(String conversationId) {
        SysConversation earliest = sysConversationMapper.selectOne(new LambdaQueryWrapper<SysConversation>()
                .eq(SysConversation::getConversationId, conversationId)
                .orderByAsc(SysConversation::getCreateTime)
                .last("LIMIT 1"));
        SysConversation latest = sysConversationMapper.selectOne(new LambdaQueryWrapper<SysConversation>()
                .eq(SysConversation::getConversationId, conversationId)
                .orderByDesc(SysConversation::getCreateTime)
                .last("LIMIT 1"));
        if (earliest == null || earliest.getCreateTime() == null || latest == null || latest.getCreateTime() == null) {
            return null;
        }
        long padMs = 2 * 60 * 1000L;
        return new Date[]{
                new Date(earliest.getCreateTime().getTime() - padMs),
                new Date(latest.getCreateTime().getTime() + padMs)
        };
    }
}
