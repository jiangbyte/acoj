package io.charlie.app.core.modular.set.similarity.result.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.similarity.result.entity.ProSetSimilarityResult;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultAddParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultEditParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultIdParam;
import io.charlie.app.core.modular.set.similarity.result.param.ProSetSimilarityResultPageParam;
import io.charlie.app.core.modular.set.similarity.result.mapper.ProSetSimilarityResultMapper;
import io.charlie.app.core.modular.set.similarity.result.service.ProSetSimilarityResultService;
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
* @description 题单代码相似度检测结果详情表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetSimilarityResultServiceImpl extends ServiceImpl<ProSetSimilarityResultMapper, ProSetSimilarityResult> implements ProSetSimilarityResultService {

    @Override
    public Page<ProSetSimilarityResult> page(ProSetSimilarityResultPageParam proSetSimilarityResultPageParam) {
        QueryWrapper<ProSetSimilarityResult> queryWrapper = new QueryWrapper<ProSetSimilarityResult>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSetSimilarityResultPageParam.getSortField(), proSetSimilarityResultPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetSimilarityResultPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetSimilarityResultPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetSimilarityResultPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetSimilarityResultPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetSimilarityResultPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetSimilarityResultAddParam proSetSimilarityResultAddParam) {
        ProSetSimilarityResult bean = BeanUtil.toBean(proSetSimilarityResultAddParam, ProSetSimilarityResult.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetSimilarityResultEditParam proSetSimilarityResultEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSetSimilarityResult>().eq(ProSetSimilarityResult::getId, proSetSimilarityResultEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSetSimilarityResult bean = BeanUtil.toBean(proSetSimilarityResultEditParam, ProSetSimilarityResult.class);
        BeanUtil.copyProperties(proSetSimilarityResultEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetSimilarityResultIdParam> proSetSimilarityResultIdParamList) {
        if (ObjectUtil.isEmpty(proSetSimilarityResultIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetSimilarityResultIdParamList, ProSetSimilarityResultIdParam::getId));
    }

    @Override
    public ProSetSimilarityResult detail(ProSetSimilarityResultIdParam proSetSimilarityResultIdParam) {
        ProSetSimilarityResult proSetSimilarityResult = this.getById(proSetSimilarityResultIdParam.getId());
        if (ObjectUtil.isEmpty(proSetSimilarityResult)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSetSimilarityResult;
    }

}