package io.charlie.web.oj.modular.data.contest.service.impl;

import cn.dev33.satoken.secure.BCrypt;
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
import io.charlie.web.oj.modular.data.contest.entity.DataContestParticipant;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestParticipantMapper;
import io.charlie.web.oj.modular.data.contest.param.*;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.contest.utils.ContestAuthTool;
import io.charlie.web.oj.modular.data.contest.utils.ContestBuildTool;
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
 * @description 竞赛表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataContestServiceImpl extends ServiceImpl<DataContestMapper, DataContest> implements DataContestService {
    private final TransService transService;

    private final ContestBuildTool contestBuildTool;
    private final ContestAuthTool contestAuthTool;

    private final DataContestParticipantMapper dataContestParticipantMapper;

    @Override
    @DS("slave")
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

        Page<DataContest> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataContestPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataContestPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        contestBuildTool.buildContests(page.getRecords());
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataContestAddParam dataContestAddParam) {
        DataContest bean = BeanUtil.toBean(dataContestAddParam, DataContest.class);

//        // 加密密码
//        String encodePassword = BCrypt.hashpw(bean.getPassword());
//        bean.setPassword(encodePassword);

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
    @DS("slave")
    public DataContest detail(DataContestIdParam dataContestIdParam) {
        DataContest dataContest = this.getById(dataContestIdParam.getId());
        if (ObjectUtil.isEmpty(dataContest)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        contestBuildTool.buildContest(dataContest);
        return dataContest;
    }

    @Override
    @DS("slave")
    public List<DataContest> getHotN(int n) {
        List<DataContest> dataContests = this.baseMapper.selectTopNBySubmitCount(10);
        contestBuildTool.buildContests(dataContests);
        transService.transBatch(dataContests);
        return dataContests;
    }

    @Override
    public Object signUp(DataContestSignUpParam signUpParam) {
        DataContest dataContest = this.getById(signUpParam.getContestId());
        if (ObjectUtil.isEmpty(dataContest)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (!dataContest.getIsPublic()) {
            if (!contestAuthTool.userIsAuth(signUpParam.getContestId(), signUpParam.getUserId())) {
                throw new BusinessException("该竞赛无权限");
            }
        }

        if (!dataContest.getIsVisible()) {
            throw new BusinessException("该竞赛无效");
        }

        if (dataContest.getRegisterStartTime().getTime() > System.currentTimeMillis()) {
            throw new BusinessException("该竞赛尚未开始报名");
        }

        if (dataContest.getRegisterEndTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException("该竞赛已结束报名");
        }

        boolean exists = dataContestParticipantMapper.exists(new LambdaQueryWrapper<DataContestParticipant>()
                .eq(DataContestParticipant::getContestId, signUpParam.getContestId())
                .eq(DataContestParticipant::getUserId, signUpParam.getUserId())
        );
        if (exists) {
            throw new BusinessException("已报名");
        }

        DataContestParticipant dataContestParticipant = new DataContestParticipant();
        dataContestParticipant.setContestId(signUpParam.getContestId());
        dataContestParticipant.setUserId(signUpParam.getUserId());
        dataContestParticipant.setTeamId(signUpParam.getTeamId());
        dataContestParticipant.setRegisterTime(new Date());
        dataContestParticipantMapper.insert(dataContestParticipant);

        return null;
    }

}