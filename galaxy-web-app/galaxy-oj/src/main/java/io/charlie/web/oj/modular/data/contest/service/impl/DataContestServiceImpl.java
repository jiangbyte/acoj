package io.charlie.web.oj.modular.data.contest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import io.charlie.web.oj.modular.data.contest.param.DataContestAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestPageParam;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-06-23
* @description 竞赛表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataContestServiceImpl extends ServiceImpl<DataContestMapper, DataContest> implements DataContestService {

    @Override
    public Page<DataContest> page(DataContestPageParam dataContestPageParam) {
        QueryWrapper<DataContest> queryWrapper = new QueryWrapper<DataContest>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataContestPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataContest::getTitle, dataContestPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(dataContestPageParam.getSortField(), dataContestPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataContestPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataContestPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataContestPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(DataContest::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataContestPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataContestPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataContestAddParam dataContestAddParam) {
        DataContest bean = BeanUtil.toBean(dataContestAddParam, DataContest.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataContestEditParam dataContestEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataContest>().eq(DataContest::getId, dataContestEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataContest bean = BeanUtil.toBean(dataContestEditParam, DataContest.class);
        BeanUtil.copyProperties(dataContestEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataContestIdParam> dataContestIdParamList) {
        if (ObjectUtil.isEmpty(dataContestIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataContestIdParamList, DataContestIdParam::getId));
    }

    @Override
    public DataContest detail(DataContestIdParam dataContestIdParam) {
        DataContest dataContest = this.getById(dataContestIdParam.getId());
        if (ObjectUtil.isEmpty(dataContest)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataContest;
    }

    @Override
    @DS("slave")
    public List<DataContest> getHotN(int n) {
        List<DataContest> dataSets = this.baseMapper.selectTopNBySubmitCount(10);
//        setBuildTool.buildSets(dataSets);
//        transService.transBatch(dataSets);
        return dataSets;
    }

}