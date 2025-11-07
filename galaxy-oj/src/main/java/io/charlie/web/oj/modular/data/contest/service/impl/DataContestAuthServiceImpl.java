package io.charlie.web.oj.modular.data.contest.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import io.charlie.web.oj.modular.data.contest.entity.DataContestAuth;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import io.charlie.web.oj.modular.data.contest.param.*;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestAuthMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestAuthService;
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
 * @description 竞赛认证表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataContestAuthServiceImpl extends ServiceImpl<DataContestAuthMapper, DataContestAuth> implements DataContestAuthService {
    private final DataContestMapper dataContestMapper;

    @Override
    public Page<DataContestAuth> page(DataContestAuthPageParam dataContestAuthPageParam) {
        QueryWrapper<DataContestAuth> queryWrapper = new QueryWrapper<DataContestAuth>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataContestAuthPageParam.getSortField(), dataContestAuthPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataContestAuthPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataContestAuthPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataContestAuthPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataContestAuthPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataContestAuthPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataContestAuthAddParam dataContestAuthAddParam) {
        DataContestAuth bean = BeanUtil.toBean(dataContestAuthAddParam, DataContestAuth.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataContestAuthEditParam dataContestAuthEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataContestAuth>().eq(DataContestAuth::getId, dataContestAuthEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataContestAuth bean = BeanUtil.toBean(dataContestAuthEditParam, DataContestAuth.class);
        BeanUtil.copyProperties(dataContestAuthEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataContestAuthIdParam> dataContestAuthIdParamList) {
        if (ObjectUtil.isEmpty(dataContestAuthIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataContestAuthIdParamList, DataContestAuthIdParam::getId));
    }

    @Override
    public DataContestAuth detail(DataContestAuthIdParam dataContestAuthIdParam) {
        DataContestAuth dataContestAuth = this.getById(dataContestAuthIdParam.getId());
        if (ObjectUtil.isEmpty(dataContestAuth)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataContestAuth;
    }

    @Override
    public Boolean auth(DataContestAuthParam dataContestAuthParam) {
        DataContest dataContest = dataContestMapper.selectById(dataContestAuthParam.getContestId());
        if (ObjectUtil.isEmpty(dataContest)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (!dataContest.getIsVisible()) {
            throw new BusinessException("非私有竞赛，无需认证");
        }

        if (!dataContest.getIsPublic()) {
            // 私有竞赛验证密码
            if (!dataContest.getPassword().equals(dataContestAuthParam.getPassword())) {
                throw new BusinessException("密码错误");
            }
            DataContestAuth dataContestAuth = new DataContestAuth();
            dataContestAuth.setContestId(dataContestAuthParam.getContestId());
            String loginIdAsString = StpUtil.getLoginIdAsString();
            dataContestAuth.setUserId(loginIdAsString);
            dataContestAuth.setIsAuth(true);
            return this.save(dataContestAuth);
        }

        return false;
    }

}