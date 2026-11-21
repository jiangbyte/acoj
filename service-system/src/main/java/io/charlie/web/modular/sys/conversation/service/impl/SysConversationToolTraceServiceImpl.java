package io.charlie.web.modular.sys.conversation.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.cores.pojo.CommonPageRequest;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import io.charlie.web.modular.sys.conversation.param.SysConversationToolTracePageParam;
import io.charlie.web.modular.sys.conversation.service.SysConversationToolTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * LLM 工具调用轨迹查询实现。
 */
@Service
@RequiredArgsConstructor
public class SysConversationToolTraceServiceImpl
        extends ServiceImpl<SysConversationToolTraceMapper, SysConversationToolTrace>
        implements SysConversationToolTraceService {

    @Override
    @DS("slave")
    public Page<SysConversationToolTrace> page(SysConversationToolTracePageParam param) {
        // 1. 按 conversation/user/problem/tool 过滤
        // 2. 按创建时间倒序分页
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
        queryWrapper.lambda().orderByDesc(SysConversationToolTrace::getCreateTime);
        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(param.getCurrent()).orElse(1),
                        Optional.ofNullable(param.getSize()).orElse(20),
                        null),
                queryWrapper);
    }
}
