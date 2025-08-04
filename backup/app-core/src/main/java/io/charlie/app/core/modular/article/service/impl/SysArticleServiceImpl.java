package io.charlie.app.core.modular.article.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.article.entity.SysArticle;
import io.charlie.app.core.modular.article.param.SysArticleAddParam;
import io.charlie.app.core.modular.article.param.SysArticleEditParam;
import io.charlie.app.core.modular.article.param.SysArticleIdParam;
import io.charlie.app.core.modular.article.param.SysArticlePageParam;
import io.charlie.app.core.modular.article.mapper.SysArticleMapper;
import io.charlie.app.core.modular.article.service.SysArticleService;
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
* @description 系统文章表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysArticleServiceImpl extends ServiceImpl<SysArticleMapper, SysArticle> implements SysArticleService {

    @Override
    public Page<SysArticle> page(SysArticlePageParam sysArticlePageParam) {
        QueryWrapper<SysArticle> queryWrapper = new QueryWrapper<SysArticle>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysArticlePageParam.getKeyword())) {
            queryWrapper.lambda().like(SysArticle::getTitle, sysArticlePageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysArticlePageParam.getSortField(), sysArticlePageParam.getSortOrder()) && ISortOrderEnum.isValid(sysArticlePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysArticlePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysArticlePageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysArticle::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysArticlePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysArticlePageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysArticleAddParam sysArticleAddParam) {
        SysArticle bean = BeanUtil.toBean(sysArticleAddParam, SysArticle.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysArticleEditParam sysArticleEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysArticle>().eq(SysArticle::getId, sysArticleEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysArticle bean = BeanUtil.toBean(sysArticleEditParam, SysArticle.class);
        BeanUtil.copyProperties(sysArticleEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysArticleIdParam> sysArticleIdParamList) {
        if (ObjectUtil.isEmpty(sysArticleIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysArticleIdParamList, SysArticleIdParam::getId));
    }

    @Override
    public SysArticle detail(SysArticleIdParam sysArticleIdParam) {
        SysArticle sysArticle = this.getById(sysArticleIdParam.getId());
        if (ObjectUtil.isEmpty(sysArticle)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysArticle;
    }

}