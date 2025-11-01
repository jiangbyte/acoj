package io.charlie.web.oj.modular.sys.conversation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationIdParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationAddParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationEditParam;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-10-20
 * @description 大模型对话表 服务类
 */
public interface SysConversationService extends IService<SysConversation> {
    Page<SysConversation> page(SysConversationPageParam sysConversationPageParam);

    void add(SysConversationAddParam sysConversationAddParam);

    void edit(SysConversationEditParam sysConversationEditParam);

    void delete(List<SysConversationIdParam> sysConversationIdParamList);

    SysConversation detail(SysConversationIdParam sysConversationIdParam);

    SysConversation saveUserConversation(ChatRequest chatRequest);

    SysConversation saveBotConversation(ChatRequest chatRequest, String content, Long time, String error);

    List<SysConversation> historyByConversationId(String conversationId);

    Page<SysConversation> userHistorypage(SysConversationPageParam sysConversationPageParam);
}