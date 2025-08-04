package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.similarity.result.entity.ProSimilarityResult;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultAddParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultEditParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultIdParam;
import io.charlie.app.core.modular.problem.similarity.result.param.ProSimilarityResultPageParam;
import io.charlie.app.core.modular.problem.similarity.result.mapper.ProSimilarityResultMapper;
import io.charlie.app.core.modular.problem.similarity.result.service.ProSimilarityResultService;
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
* @description 代码相似度检测结果详情表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSimilarityResultServiceImpl extends ServiceImpl<ProSimilarityResultMapper, ProSimilarityResult> implements ProSimilarityResultService {

    @Override
    public Page<ProSimilarityResult> page(ProSimilarityResultPageParam proSimilarityResultPageParam) {
        QueryWrapper<ProSimilarityResult> queryWrapper = new QueryWrapper<ProSimilarityResult>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(proSimilarityResultPageParam.getSortField(), proSimilarityResultPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSimilarityResultPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSimilarityResultPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSimilarityResultPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSimilarityResultPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSimilarityResultPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSimilarityResultAddParam proSimilarityResultAddParam) {
        ProSimilarityResult bean = BeanUtil.toBean(proSimilarityResultAddParam, ProSimilarityResult.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSimilarityResultEditParam proSimilarityResultEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSimilarityResult>().eq(ProSimilarityResult::getId, proSimilarityResultEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSimilarityResult bean = BeanUtil.toBean(proSimilarityResultEditParam, ProSimilarityResult.class);
        BeanUtil.copyProperties(proSimilarityResultEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSimilarityResultIdParam> proSimilarityResultIdParamList) {
        if (ObjectUtil.isEmpty(proSimilarityResultIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSimilarityResultIdParamList, ProSimilarityResultIdParam::getId));
    }

    @Override
    public ProSimilarityResult detail(ProSimilarityResultIdParam proSimilarityResultIdParam) {
        ProSimilarityResult proSimilarityResult = this.getById(proSimilarityResultIdParam.getId());
        if (ObjectUtil.isEmpty(proSimilarityResult)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return proSimilarityResult;
    }

}