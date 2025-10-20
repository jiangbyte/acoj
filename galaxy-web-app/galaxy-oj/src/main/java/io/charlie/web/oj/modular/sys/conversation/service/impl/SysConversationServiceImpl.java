package io.charlie.web.oj.modular.sys.conversation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationAddParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationEditParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationIdParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.oj.modular.sys.conversation.mapper.SysConversationMapper;
import io.charlie.web.oj.modular.sys.conversation.service.SysConversationService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
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

    @Override
    public Page<SysConversation> page(SysConversationPageParam sysConversationPageParam) {
        QueryWrapper<SysConversation> queryWrapper = new QueryWrapper<SysConversation>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysConversationPageParam.getSortField(), sysConversationPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysConversationPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysConversationPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysConversationPageParam.getSortField()));
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
    public SysConversation detail(SysConversationIdParam sysConversationIdParam) {
        SysConversation sysConversation = this.getById(sysConversationIdParam.getId());
        if (ObjectUtil.isEmpty(sysConversation)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysConversation;
    }

    @Override
    public SysConversation saveConversation(ChatRequest chatRequest) {
        SysConversation bean = BeanUtil.toBean(chatRequest, SysConversation.class);
        bean.setMessageContent(chatRequest.getMessage());
        this.save(bean);
        return bean;
    }

}