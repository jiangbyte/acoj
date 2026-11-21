package io.charlie.web.modular.sys.conversation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.param.SysConversationToolTracePageParam;

/**
 * LLM 工具调用轨迹查询。
 */
public interface SysConversationToolTraceService extends IService<SysConversationToolTrace> {

    Page<SysConversationToolTrace> page(SysConversationToolTracePageParam param);
}
