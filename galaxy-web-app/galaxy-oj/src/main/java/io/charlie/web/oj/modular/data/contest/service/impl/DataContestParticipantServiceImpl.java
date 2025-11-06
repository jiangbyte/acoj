package io.charlie.web.oj.modular.data.contest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.contest.entity.DataContestParticipant;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantPageParam;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestParticipantMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestParticipantService;
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
* @description 竞赛参与表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DataContestParticipantServiceImpl extends ServiceImpl<DataContestParticipantMapper, DataContestParticipant> implements DataContestParticipantService {

    @Override
    public Page<DataContestParticipant> page(DataContestParticipantPageParam dataContestParticipantPageParam) {
        QueryWrapper<DataContestParticipant> queryWrapper = new QueryWrapper<DataContestParticipant>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataContestParticipantPageParam.getSortField(), dataContestParticipantPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataContestParticipantPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataContestParticipantPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataContestParticipantPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataContestParticipantPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataContestParticipantPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataContestParticipantAddParam dataContestParticipantAddParam) {
        DataContestParticipant bean = BeanUtil.toBean(dataContestParticipantAddParam, DataContestParticipant.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataContestParticipantEditParam dataContestParticipantEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataContestParticipant>().eq(DataContestParticipant::getId, dataContestParticipantEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataContestParticipant bean = BeanUtil.toBean(dataContestParticipantEditParam, DataContestParticipant.class);
        BeanUtil.copyProperties(dataContestParticipantEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataContestParticipantIdParam> dataContestParticipantIdParamList) {
        if (ObjectUtil.isEmpty(dataContestParticipantIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataContestParticipantIdParamList, DataContestParticipantIdParam::getId));
    }

    @Override
    public DataContestParticipant detail(DataContestParticipantIdParam dataContestParticipantIdParam) {
        DataContestParticipant dataContestParticipant = this.getById(dataContestParticipantIdParam.getId());
        if (ObjectUtil.isEmpty(dataContestParticipant)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataContestParticipant;
    }

}