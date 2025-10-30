package io.charlie.web.oj.modular.data.submit.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.event.ProblemSubmitEvent;
import io.charlie.web.oj.modular.data.submit.param.*;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.data.submit.service.DataSubmitService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.testcase.service.DataTestCaseService;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.handle.JudgeHandleMessage;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    private final UserActivityService userActivityService;

    private final DataScopeUtil dataScopeUtil;

    private final DataTestCaseService dataTestCaseService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Page<DataSubmit> page(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        // 在最后拼接 sql，查询 user_id 的 sys_user 表的 group_id 在 accessibleGroupIds 中
//        queryWrapper.inSql("user_id",
//                "SELECT id FROM sys_user WHERE group_id IN (" +
//                        accessibleGroupIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")) +
//                        ")");

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
    public Page<DataSubmit> problemPage(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();
        queryWrapper.lambda().eq(DataSubmit::getIsSet, false);
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }

        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(DataSubmit::getLanguage, dataSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getStatus())) {
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
    public Page<DataSubmit> setPage(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();
        queryWrapper.lambda().eq(DataSubmit::getIsSet, true);

        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getSetId())) {
            queryWrapper.lambda().eq(DataSubmit::getSetId, dataSubmitPageParam.getSetId());
        }
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getLanguage())) {
            queryWrapper.lambda().eq(DataSubmit::getLanguage, dataSubmitPageParam.getLanguage());
        }
        if (GaStringUtil.isNotEmpty(dataSubmitPageParam.getStatus())) {
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
        DataSubmit dataSubmit = this.handleSubmit(dataSubmitExeParam, false);
        String loginIdAsString = StpUtil.getLoginIdAsString();

        applicationEventPublisher.publishEvent(new ProblemSubmitEvent(dataSubmitExeParam, dataSubmit, Boolean.FALSE, loginIdAsString));

//        this.handleSolvedRecord(dataSubmitExeParam, false, dataSubmit);
//        this.asyncHandleSubmit(dataSubmit, dataSubmitExeParam);
//        try {
//            String loginIdAsString = StpUtil.getLoginIdAsString();
//            if (GaStringUtil.isNotEmpty(loginIdAsString)) {
//                userActivityService.addActivity(loginIdAsString, ActivityScoreCalculator.SUBMIT, false);
//            }
//        } catch (Exception e) {
//            log.error("用户活动记录失败", e);
//        }

        return dataSubmit.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handleSetSubmit(DataSubmitExeParam dataSubmitExeParam) {
        String setId = dataSubmitExeParam.getSetId();
        if (GaStringUtil.isEmpty(setId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        DataSet dataSet = dataSetMapper.selectById(setId);

        // 时间判断
        // 限时题集
        if (dataSet.getSetType().equals(2)) {
            // 时间状态计算
            Date now = new Date();
            // 只有当开始时间和结束时间都存在时才计算
            if (dataSet.getStartTime() != null && dataSet.getEndTime() != null) {
                if (now.before(dataSet.getStartTime())) {
//                    dataSet.setTimeStatus(1);  // 未开始
                    throw new BusinessException("题集未开始，不能提交");
                } else if (now.after(dataSet.getEndTime())) {
//                    dataSet.setTimeStatus(3);  // 已结束
                    throw new BusinessException("题集已结束，不能提交");
                }
//                else {
////                    dataSet.setTimeStatus(2); // 进行中
//                }
            }
//            else {
//                // 如果时间不完整，可以设置一个默认状态或保持null
////                dataSet.setTimeStatus(0); // 或 null，表示时间状态未知
//            }
        }

        DataSubmit dataSubmit = this.handleSubmit(dataSubmitExeParam, true);
        this.handleSolvedRecord(dataSubmitExeParam, true, dataSubmit);
        this.asyncHandleSubmit(dataSubmit, dataSubmitExeParam);
        return dataSubmit.getId();
    }

    @Override
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

    public DataSubmit handleSubmit(DataSubmitExeParam dataSubmitExeParam, Boolean isSet) {
        DataSubmit submit = BeanUtil.toBean(dataSubmitExeParam, DataSubmit.class);
        submit.setIsSet(isSet);
        submit.setUserId(StpUtil.getLoginIdAsString());
        submit.setStatus(JudgeStatus.PENDING.getValue()); // 待处理
        submit.setIsFinish(false); // 流程流转未结束
        submit.setCodeLength(dataSubmitExeParam.getCode().length());
        this.save(submit);
        return submit;
    }

    @Retryable(value = {DeadlockLoserDataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleSolvedRecord(DataSubmitExeParam dataSubmitExeParam, Boolean isSet, DataSubmit dataSubmit) {
        String userId = StpUtil.getLoginIdAsString();
        String lockKey = String.format("solved:lock:%s:%s",
                userId, isSet ? dataSubmitExeParam.getSetId() : dataSubmitExeParam.getProblemId());

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(1, 30, TimeUnit.SECONDS)) {
                doHandleSolvedRecord(dataSubmitExeParam, isSet, dataSubmit);
            } else {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("处理被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void doHandleSolvedRecord(DataSubmitExeParam dataSubmitExeParam, Boolean isSet, DataSubmit dataSubmit) {
        String userId = StpUtil.getLoginIdAsString();
        String problemId = dataSubmitExeParam.getProblemId();
        String setId = dataSubmitExeParam.getSetId();
        String submitId = dataSubmit.getId();

        try {
            if (isSet) {
                if (dataSolvedMapper.exists(new LambdaQueryWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, userId)
                        .eq(DataSolved::getSetId, setId)
                        .eq(DataSolved::getProblemId, problemId)
                        .eq(DataSolved::getIsSet, Boolean.TRUE)
                )) {
                    // 更新
                    dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                            .eq(DataSolved::getUserId, userId)
                            .eq(DataSolved::getSetId, setId)
                            .eq(DataSolved::getProblemId, problemId)
                            .eq(DataSolved::getIsSet, Boolean.TRUE)
                            .set(DataSolved::getSubmitId, submitId)
                            .set(DataSolved::getUpdateTime, new Date())
                    );
                } else {
                    DataSolved dataSolved = new DataSolved();
                    dataSolved.setUserId(userId);
                    dataSolved.setSetId(setId);
                    dataSolved.setProblemId(problemId);
                    dataSolved.setIsSet(Boolean.TRUE);
                    dataSolved.setSubmitId(submitId);
                    dataSolved.setCreateTime(new Date());
                    dataSolved.setUpdateTime(new Date());
                    dataSolvedMapper.insert(dataSolved);
                }
            } else {
                if (dataSolvedMapper.exists(new LambdaQueryWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, userId)
                        .eq(DataSolved::getProblemId, problemId)
                        .eq(DataSolved::getIsSet, Boolean.FALSE)
                )) {
                    dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                            .eq(DataSolved::getUserId, userId)
                            .eq(DataSolved::getProblemId, problemId)
                            .eq(DataSolved::getIsSet, Boolean.FALSE)
                            .set(DataSolved::getSubmitId, submitId)
                            .set(DataSolved::getUpdateTime, new Date())
                    );
                } else {
                    DataSolved dataSolved = new DataSolved();
                    dataSolved.setUserId(userId);
                    dataSolved.setProblemId(problemId);
                    dataSolved.setIsSet(Boolean.FALSE);
                    dataSolved.setSubmitId(submitId);
                    dataSolved.setCreateTime(new Date());
                    dataSolved.setUpdateTime(new Date());
                    dataSolvedMapper.insert(dataSolved);
                }
            }
        } catch (Exception e) {
            log.error("处理解题记录失败，用户:{} 问题:{} 模式:{}", userId, problemId, isSet, e);
            throw new BusinessException("处理解题记录失败", e);
        }
    }

    @Async("taskExecutor")
    public void asyncHandleSubmit(DataSubmit dataSubmit, DataSubmitExeParam param) {
        try {
            DataProblem problem = dataProblemMapper.selectById(dataSubmit.getProblemId());

//            List<DataTestCase> testCaseByProblemId = dataTestCaseService.getTestCaseByProblemId(dataSubmit.getProblemId());
//            List<TestCase> testCases = testCaseByProblemId.stream().map(testCase -> {
//                TestCase testCase1 = new TestCase();
//                testCase1.setInput(testCase.getInputData());
//                testCase1.setOutput(testCase.getExpectedOutput());
//                return testCase1;
//            }).toList();

            JudgeSubmitDto message = new JudgeSubmitDto();
            // ======================= 任务参数 =======================
            message.setId(dataSubmit.getId());
            message.setUserId(dataSubmit.getUserId());
            message.setJudgeTaskId(param.getJudgeTaskId());
            message.setIsSet(dataSubmit.getIsSet());
            // ======================= 题目参数 =======================
            message.setMaxTime(problem.getMaxTime());
            message.setMaxMemory(problem.getMaxMemory());
//            message.setTestCase(testCases);
            // ======================= 用户提交参数 =======================
            message.setProblemId(param.getProblemId());
            if (param.getSetId() != null) {
                message.setSetId(param.getSetId());
            }
            message.setLanguage(param.getLanguage());
            message.setSubmitType(param.getSubmitType());

            // 处理代码模板
//            if (problem.getUseTemplate()) {
//                problem.getCodeTemplate().stream()
//                        .filter(t -> t.getLanguage().equals(param.getLanguage().toLowerCase()))
//                        .findFirst()
//                        .ifPresent(template -> {
//                            if (ObjectUtil.isNotEmpty(template.getPrefix()) && ObjectUtil.isNotEmpty(template.getSuffix())) {
//                                String code = template.getPrefix() + param.getCode() + template.getSuffix();
//                                log.info("处理代码模板成功，提交ID: {} 代码内容: {}", dataSubmit.getId(), code);
//                                message.setCode(code);
//                            }
//                        });
//            } else {
//                log.info("未使用代码模板，提交ID: {} 提交内容: {}", dataSubmit.getId(), param.getCode());
//                message.setCode(param.getCode());
//            }

            message.setCode(param.getCode());

            judgeHandleMessage.sendJudge(message);

            log.info("题目提交消息已发送到队列，提交ID: {}", dataSubmit.getId());
        } catch (Exception e) {
            log.error("发送题目提交消息到队列失败，提交ID: {}", dataSubmit.getId(), e);
            throw new BusinessException("提交失败，请稍后重试");
        }
    }

}