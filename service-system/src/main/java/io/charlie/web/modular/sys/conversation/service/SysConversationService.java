package io.charlie.web.modular.sys.conversation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.param.SysConversationIdParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationSessionIdParam;
import io.charlie.web.modular.llm.param.ChatRequest;
import io.charlie.web.modular.sys.conversation.param.SysConversationAddParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationEditParam;
import io.charlie.web.modular.sys.conversation.result.SysConversationSessionVO;

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

    /**
     * 保存助手回复；tokens 可为空（兼容旧调用）。
     */
    SysConversation saveBotConversation(ChatRequest chatRequest, String content, Long time, String error);

    /**
     * 保存助手回复并写入 token 用量。
     */
    SysConversation saveBotConversation(ChatRequest chatRequest, String content, Long time, String error,
                                        Integer promptTokens, Integer completionTokens, Integer totalTokens);

    List<SysConversation> historyByConversationId(String conversationId);

    /**
     * 按会话 ID 分页拉取消息时间线。
     */
    Page<SysConversation> historyPageByConversationId(String conversationId, Integer current, Integer size);

    Page<SysConversation> userHistorypage(SysConversationPageParam sysConversationPageParam);

    /**
     * 管理端：按会话聚合分页。
     */
    Page<SysConversationSessionVO> sessionPage(SysConversationPageParam sysConversationPageParam);

    /**
     * 当前登录用户：按会话聚合分页（可按题目过滤）。
     */
    Page<SysConversationSessionVO> userSessionPage(SysConversationPageParam sysConversationPageParam);

    /**
     * 按 conversationId 批量删除会话下全部消息。
     */
    void deleteByConversationIds(List<SysConversationSessionIdParam> sessionIdParamList);
}