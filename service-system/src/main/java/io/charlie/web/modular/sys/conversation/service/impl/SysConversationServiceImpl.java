package io.charlie.web.modular.sys.conversation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.modular.sys.conversation.constant.MessageRole;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationMapper;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import io.charlie.web.modular.sys.conversation.param.SysConversationIdParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationSessionIdParam;
import io.charlie.web.modular.sys.conversation.result.SysConversationSessionVO;
import io.charlie.web.modular.sys.conversation.service.SysConversationService;
import io.charlie.web.modular.llm.param.ChatRequest;
import io.charlie.web.modular.sys.conversation.param.SysConversationAddParam;
import io.charlie.web.modular.sys.conversation.param.SysConversationEditParam;
import io.charlie.cores.enums.ISortOrderEnum;
import io.charlie.cores.exception.BusinessException;
import io.charlie.cores.pojo.CommonPageRequest;
import io.charlie.cores.result.ResultCode;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 大模型对话表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConversationServiceImpl extends ServiceImpl<SysConversationMapper, SysConversation> implements SysConversationService {

    private final TransService transService;
    private final SysConversationToolTraceMapper sysConversationToolTraceMapper;

    @Override
    @DS("slave")
    public Page<SysConversation> page(SysConversationPageParam sysConversationPageParam) {
        // 1. 管理端按 user/problem/conversation 过滤
        // 2. 排序后分页
        QueryWrapper<SysConversation> queryWrapper = new QueryWrapper<SysConversation>().checkSqlInjection();
        if (StrUtil.isNotBlank(sysConversationPageParam.getUserId())) {
            queryWrapper.lambda().eq(SysConversation::getUserId, sysConversationPageParam.getUserId());
        }
        if (StrUtil.isNotBlank(sysConversationPageParam.getProblemId())) {
            queryWrapper.lambda().eq(SysConversation::getProblemId, sysConversationPageParam.getProblemId());
        }
        if (StrUtil.isNotBlank(sysConversationPageParam.getConversationId())) {
            queryWrapper.lambda().eq(SysConversation::getConversationId, sysConversationPageParam.getConversationId());
        }
        if (ObjectUtil.isAllNotEmpty(sysConversationPageParam.getSortField(), sysConversationPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysConversationPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysConversationPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysConversationPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(SysConversation::getId);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysConversationPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysConversationPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysConversationAddParam sysConversationAddParam) {
        SysConversation bean = BeanUtil.toBean(sysConversationAddParam, SysConversation.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysConversationEditParam sysConversationEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysConversation>().eq(SysConversation::getId, sysConversationEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysConversation bean = BeanUtil.toBean(sysConversationEditParam, SysConversation.class);
        BeanUtil.copyProperties(sysConversationEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysConversationIdParam> sysConversationIdParamList) {
        if (ObjectUtil.isEmpty(sysConversationIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysConversationIdParamList, SysConversationIdParam::getId));
    }

    @Override
    @DS("slave")
    public SysConversation detail(SysConversationIdParam sysConversationIdParam) {
        SysConversation sysConversation = this.getById(sysConversationIdParam.getId());
        if (ObjectUtil.isEmpty(sysConversation)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysConversation;
    }

    @Override
    public SysConversation saveUserConversation(ChatRequest chatRequest) {
        SysConversation sysConversation = BeanUtil.toBean(chatRequest, SysConversation.class);
        sysConversation.setMessageRole(MessageRole.USER);

        try {
            if (StpUtil.isLogin()) {
                sysConversation.setUserId(StpUtil.getLoginIdAsString());
                sysConversation.setUserPlatform(StpUtil.getLoginDevice());
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
        }

        sysConversation.setResponseTime(new Date());
        this.save(sysConversation);
        return sysConversation;
    }

    @Override
    public SysConversation saveBotConversation(ChatRequest chatRequest, String content, Long time, String error) {
        return saveBotConversation(chatRequest, content, time, error, null, null, null);
    }

    @Override
    public SysConversation saveBotConversation(ChatRequest chatRequest, String content, Long time, String error,
                                               Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        // 1. 从请求拷贝基础字段并设为助手角色
        // 2. 写入内容 / 状态 / 耗时 / token 用量
        // 3. 落库返回
        SysConversation sysConversation = BeanUtil.toBean(chatRequest, SysConversation.class);
        if (ObjectUtil.isNotEmpty(content)) {
            sysConversation.setMessageContent(content);
        }
        sysConversation.setMessageRole(MessageRole.ASSISTANT);

        try {
            if (StpUtil.isLogin()) {
                sysConversation.setUserId(StpUtil.getLoginIdAsString());
                sysConversation.setUserPlatform(StpUtil.getLoginDevice());
            }
        } catch (Exception e) {
            log.error("获取用户信息失败");
        }

        sysConversation.setStatus(error == null ? "SUCCESS" : "ERROR");

        if (ObjectUtil.isNotEmpty(error)) {
            sysConversation.setErrorMessage(error);
        }

        if (ObjectUtil.isNotEmpty(time)) {
            sysConversation.setStreamingDuration(Math.toIntExact(time));
        }

        if (promptTokens != null) {
            sysConversation.setPromptTokens(promptTokens);
        }
        if (completionTokens != null) {
            sysConversation.setCompletionTokens(completionTokens);
        }
        if (totalTokens != null) {
            sysConversation.setTotalTokens(totalTokens);
        } else if (promptTokens != null || completionTokens != null) {
            int prompt = promptTokens == null ? 0 : promptTokens;
            int completion = completionTokens == null ? 0 : completionTokens;
            sysConversation.setTotalTokens(prompt + completion);
        }

        this.save(sysConversation);
        return sysConversation;
    }

    @Override
    @DS("slave")
    public List<SysConversation> historyByConversationId(String conversationId) {
        return this.list(new LambdaQueryWrapper<SysConversation>()
                .eq(SysConversation::getConversationId, conversationId)
                .orderByAsc(SysConversation::getCreateTime));
    }

    @Override
    @DS("slave")
    public Page<SysConversation> historyPageByConversationId(String conversationId, Integer current, Integer size) {
        // 1. 校验 conversationId
        // 2. 按创建时间升序分页，保证时间线顺序
        LambdaQueryWrapper<SysConversation> wrapper = new LambdaQueryWrapper<SysConversation>()
                .eq(SysConversation::getConversationId, conversationId)
                .orderByAsc(SysConversation::getCreateTime);
        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(current).orElse(1),
                        Optional.ofNullable(size).orElse(20),
                        null),
                wrapper);
    }

    @Override
    @DS("slave")
    public Page<SysConversation> userHistorypage(SysConversationPageParam sysConversationPageParam) {
        // 1. 仅当前登录用户；可选按 problemId 收窄
        // 2. 分页返回会话消息
        QueryWrapper<SysConversation> queryWrapper = new QueryWrapper<SysConversation>().checkSqlInjection();
        if (StpUtil.isLogin()) {
            queryWrapper.lambda().eq(SysConversation::getUserId, StpUtil.getLoginIdAsString());
        } else {
            return new Page<>();
        }
        if (StrUtil.isNotBlank(sysConversationPageParam.getProblemId())) {
            queryWrapper.lambda().eq(SysConversation::getProblemId, sysConversationPageParam.getProblemId());
        }
        if (StrUtil.isNotBlank(sysConversationPageParam.getConversationId())) {
            queryWrapper.lambda().eq(SysConversation::getConversationId, sysConversationPageParam.getConversationId());
        }

        if (ObjectUtil.isAllNotEmpty(sysConversationPageParam.getSortField(), sysConversationPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysConversationPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysConversationPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysConversationPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(SysConversation::getCreateTime);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysConversationPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysConversationPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Override
    @DS("slave")
    public Page<SysConversationSessionVO> sessionPage(SysConversationPageParam sysConversationPageParam) {
        // 1. 按筛选条件做 conversation_id 聚合分页
        // 2. 翻译用户/题目/消息类型等展示字段
        Page<SysConversationSessionVO> page = CommonPageRequest.Page(
                Optional.ofNullable(sysConversationPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(sysConversationPageParam.getSize()).orElse(20),
                null
        );
        Page<SysConversationSessionVO> result = (Page<SysConversationSessionVO>) this.baseMapper.selectSessionPage(page, sysConversationPageParam);
        if (ObjectUtil.isNotEmpty(result.getRecords())) {
            transService.transBatch(result.getRecords());
            truncateSessionPreviews(result.getRecords());
        }
        return result;
    }

    @Override
    @DS("slave")
    public Page<SysConversationSessionVO> userSessionPage(SysConversationPageParam sysConversationPageParam) {
        // 1. 未登录直接空页；已登录强制绑定当前用户
        // 2. 可选按 problemId 收窄后走会话聚合
        if (!StpUtil.isLogin()) {
            return new Page<>();
        }
        sysConversationPageParam.setUserId(StpUtil.getLoginIdAsString());
        return sessionPage(sysConversationPageParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByConversationIds(List<SysConversationSessionIdParam> sessionIdParamList) {
        // 1. 校验入参并收集 conversationId
        // 2. 分批删除会话消息行
        // 3. 同步清理同 conversationId 的工具轨迹，避免孤儿数据
        if (ObjectUtil.isEmpty(sessionIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<String> conversationIds = CollStreamUtil.toList(sessionIdParamList, SysConversationSessionIdParam::getConversationId)
                .stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (conversationIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        for (int i = 0; i < conversationIds.size(); i += 500) {
            List<String> batch = conversationIds.subList(i, Math.min(i + 500, conversationIds.size()));
            this.remove(new LambdaQueryWrapper<SysConversation>().in(SysConversation::getConversationId, batch));
            sysConversationToolTraceMapper.delete(new LambdaQueryWrapper<SysConversationToolTrace>()
                    .in(SysConversationToolTrace::getConversationId, batch));
        }
    }

    /**
     * 截断预览文本，避免列表携带过长内容。
     */
    private void truncateSessionPreviews(List<SysConversationSessionVO> records) {
        for (SysConversationSessionVO record : records) {
            String preview = record.getPreview();
            if (StrUtil.isNotBlank(preview) && preview.length() > 120) {
                record.setPreview(preview.substring(0, 120) + "...");
            }
        }
    }

}