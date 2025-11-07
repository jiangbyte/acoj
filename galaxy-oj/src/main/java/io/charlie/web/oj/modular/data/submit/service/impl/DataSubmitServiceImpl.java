package io.charlie.web.oj.modular.data.submit.service.impl;

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
import io.charlie.galaxy.utils.str.GalaxyStringUtil;
import io.charlie.web.oj.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.*;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.data.submit.service.DataSubmitService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.handle.JudgeHandleMessage;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 提交表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSubmitServiceImpl extends ServiceImpl<DataSubmitMapper, DataSubmit> implements DataSubmitService {
    private final DataSolvedMapper dataSolvedMapper;
    private final JudgeHandleMessage judgeHandleMessage;
    private final DataProblemMapper dataProblemMapper;
    private final DataSetMapper dataSetMapper;

    private final RedissonClient redissonClient;

    private final DataScopeUtil dataScopeUtil;

    private final DataContestMapper dataContestMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @DS("slave")
    public Page<DataSubmit> page(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        // 使用 EXISTS 子查询的方式（性能更好）
        queryWrapper.exists("SELECT 1 FROM sys_user u WHERE u.id = user_id AND u.group_id IN (" +
                accessibleGroupIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")) +
                ")");

        if (ObjectUtil.isAllNotEmpty(dataSubmitPageParam.getSortField(), dataSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Override
    @DS("slave")
    public Page<DataSubmit> problemPage(DataSubmitPageParam dataSubmitPageParam) {

        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();


        if (dataSubmitPageParam.getIsAuth()) {
            String userId = null;
            try {
                if (StpUtil.isLogin()) {
                    userId = StpUtil.getLoginIdAsString();
                }
            } catch (Exception e) {
                log.debug("未登录");
            }
            if (userId != null) {
                queryWrapper.lambda().eq(DataSubmit::getUserId, userId);
            }
        }

        queryWrapper.lambda().eq(DataSubmit::getModuleType, "PROBLEM");
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }

        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(DataSubmit::getLanguage, dataSubmitPageParam.getLanguage());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(DataSubmit::getStatus, dataSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(DataSubmit::getSubmitType, dataSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(dataSubmitPageParam.getSortField(), dataSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Override
    @DS("slave")
    public Page<DataSubmit> setPage(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();

        if (dataSubmitPageParam.getIsAuth()) {
            String userId = null;
            try {
                if (StpUtil.isLogin()) {
                    userId = StpUtil.getLoginIdAsString();
                }
            } catch (Exception e) {
                log.debug("未登录");
            }
            if (userId != null) {
                queryWrapper.lambda().eq(DataSubmit::getUserId, userId);
            }
        }

        queryWrapper.lambda().eq(DataSubmit::getModuleType, "SET");

        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getModuleId())) {
            queryWrapper.lambda().eq(DataSubmit::getModuleId, dataSubmitPageParam.getModuleId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(DataSubmit::getLanguage, dataSubmitPageParam.getLanguage());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(DataSubmit::getStatus, dataSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(DataSubmit::getSubmitType, dataSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(dataSubmitPageParam.getSortField(), dataSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Override
    public Page<DataSubmit> modulePage(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();

        if (dataSubmitPageParam.getIsAuth()) {
            String userId = null;
            try {
                if (StpUtil.isLogin()) {
                    userId = StpUtil.getLoginIdAsString();
                }
            } catch (Exception e) {
                log.debug("未登录");
            }
            if (userId != null) {
                queryWrapper.lambda().eq(DataSubmit::getUserId, userId);
            }
        }

        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getModuleType())) {
            queryWrapper.lambda().eq(DataSubmit::getModuleType, dataSubmitPageParam.getModuleType());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getModuleId())) {
            queryWrapper.lambda().eq(DataSubmit::getModuleId, dataSubmitPageParam.getModuleId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(DataSubmit::getLanguage, dataSubmitPageParam.getLanguage());
        }
        if (GalaxyStringUtil.isNotEmpty(dataSubmitPageParam.getStatus())) {
            queryWrapper.lambda().eq(DataSubmit::getStatus, dataSubmitPageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getSubmitType())) {
            queryWrapper.lambda().eq(DataSubmit::getSubmitType, dataSubmitPageParam.getSubmitType());
        }

        if (ObjectUtil.isAllNotEmpty(dataSubmitPageParam.getSortField(), dataSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSubmitPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSubmitPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSubmitPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSubmitAddParam dataSubmitAddParam) {
        DataSubmit bean = BeanUtil.toBean(dataSubmitAddParam, DataSubmit.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSubmitEditParam dataSubmitEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSubmit>().eq(DataSubmit::getId, dataSubmitEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSubmit bean = BeanUtil.toBean(dataSubmitEditParam, DataSubmit.class);
        BeanUtil.copyProperties(dataSubmitEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSubmitIdParam> dataSubmitIdParamList) {
        if (ObjectUtil.isEmpty(dataSubmitIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataSubmitIdParamList, DataSubmitIdParam::getId));
    }

    @Override
//    @DS("slave") // 需要立即返回，故去除从库
    public DataSubmit detail(DataSubmitIdParam dataSubmitIdParam) {
        DataSubmit dataSubmit = this.getById(dataSubmitIdParam.getId());
        if (ObjectUtil.isEmpty(dataSubmit)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataSubmit;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handleProblemSubmit(DataSubmitExeParam dataSubmitExeParam) {
        DataSubmit dataSubmit = this.handleSubmit(dataSubmitExeParam);

        DataProblem problem = dataProblemMapper.selectById(dataSubmit.getProblemId());

        JudgeSubmitDto message = new JudgeSubmitDto();

        message.setId(dataSubmit.getId());
        message.setUserId(dataSubmit.getUserId());
        message.setJudgeTaskId(dataSubmitExeParam.getJudgeTaskId());
        message.setModuleType(dataSubmitExeParam.getModuleType());
        message.setModuleId(dataSubmitExeParam.getModuleId());
        message.setMaxTime(problem.getMaxTime());
        message.setMaxMemory(problem.getMaxMemory());
        message.setProblemId(dataSubmitExeParam.getProblemId());
        message.setLanguage(dataSubmitExeParam.getLanguage());
        message.setSubmitType(dataSubmitExeParam.getSubmitType());
        message.setCode(dataSubmitExeParam.getCode());

        judgeHandleMessage.sendJudge(message);

        return dataSubmit.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handleSetSubmit(DataSubmitExeParam dataSubmitExeParam) {
        DataSet dataSet = dataSetMapper.selectById(dataSubmitExeParam.getModuleId());

        if (dataSet.getSetType().equals(2)) {
            Date now = new Date();
            if (dataSet.getStartTime() != null && dataSet.getEndTime() != null) {
                if (now.before(dataSet.getStartTime())) {
                    throw new BusinessException("题集未开始，不能提交");
                } else if (now.after(dataSet.getEndTime())) {
                    throw new BusinessException("题集已结束，不能提交");
                }
            }
        }

        DataSubmit dataSubmit = this.handleSubmit(dataSubmitExeParam);
        DataProblem problem = dataProblemMapper.selectById(dataSubmitExeParam.getProblemId());

        JudgeSubmitDto message = new JudgeSubmitDto();

        message.setId(dataSubmit.getId());
        message.setUserId(dataSubmit.getUserId());
        message.setJudgeTaskId(dataSubmitExeParam.getJudgeTaskId());
        message.setModuleType(dataSubmitExeParam.getModuleType());
        message.setModuleId(dataSubmitExeParam.getModuleId());
        message.setMaxTime(problem.getMaxTime());
        message.setMaxMemory(problem.getMaxMemory());
        message.setProblemId(dataSubmitExeParam.getProblemId());
        message.setLanguage(dataSubmitExeParam.getLanguage());
        message.setSubmitType(dataSubmitExeParam.getSubmitType());
        message.setCode(dataSubmitExeParam.getCode());

        judgeHandleMessage.sendJudge(message);

        return dataSubmit.getId();
    }

    @Override
    public String handleContestSubmit(DataSubmitExeParam dataSubmitExeParam) {
        DataContest dataContest = dataContestMapper.selectById(dataSubmitExeParam.getModuleId());

        DataSubmit dataSubmit = this.handleSubmit(dataSubmitExeParam);
        DataProblem problem = dataProblemMapper.selectById(dataSubmitExeParam.getProblemId());

        JudgeSubmitDto message = new JudgeSubmitDto();

        message.setId(dataSubmit.getId());
        message.setUserId(dataSubmit.getUserId());
        message.setJudgeTaskId(dataSubmitExeParam.getJudgeTaskId());
        message.setModuleType(dataSubmitExeParam.getModuleType());
        message.setModuleId(dataSubmitExeParam.getModuleId());
        message.setMaxTime(problem.getMaxTime());
        message.setMaxMemory(problem.getMaxMemory());
        message.setProblemId(dataSubmitExeParam.getProblemId());
        message.setLanguage(dataSubmitExeParam.getLanguage());
        message.setSubmitType(dataSubmitExeParam.getSubmitType());
        message.setCode(dataSubmitExeParam.getCode());

        judgeHandleMessage.sendJudge(message);

        return dataSubmit.getId();
    }

    @Override
    @DS("slave")
    public List<StatusCount> countStatusStatistics() {
        List<JudgeStatusCountDTO> countList = this.baseMapper.countByStatus();
        return countList.stream()
                .map(dto -> {
                    StatusCount statusCount = new StatusCount();
                    statusCount.setStatus(dto.getStatus());
                    statusCount.setStatusName(JudgeStatus.getDisplayName(dto.getStatus()));
                    statusCount.setCount(String.valueOf(dto.getCount()));
                    return statusCount;
                })
                .toList();
    }

    public DataSubmit handleSubmit(DataSubmitExeParam dataSubmitExeParam) {
        DataSubmit submit = BeanUtil.toBean(dataSubmitExeParam, DataSubmit.class);
        submit.setUserId(StpUtil.getLoginIdAsString());
        submit.setStatus(JudgeStatus.PENDING.getValue()); // 待处理
        submit.setIsFinish(Boolean.FALSE); // 流程流转未结束
        submit.setCodeLength(dataSubmitExeParam.getCode().length());
        this.save(submit);
        return submit;
    }

}