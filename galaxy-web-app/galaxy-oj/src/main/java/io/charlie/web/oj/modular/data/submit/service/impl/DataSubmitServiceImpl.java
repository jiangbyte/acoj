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
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    public Page<DataSubmit> page(DataSubmitPageParam dataSubmitPageParam) {
        QueryWrapper<DataSubmit> queryWrapper = new QueryWrapper<DataSubmit>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(dataSubmitPageParam.getSortField(), dataSubmitPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSubmitPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSubmitPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSubmitPageParam.getSortField()));
        }
        // 默认按时间降序
        queryWrapper.lambda().orderByDesc(DataSubmit::getCreateTime);

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
        // 默认按时间降序
        queryWrapper.lambda().orderByDesc(DataSubmit::getCreateTime);

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

        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataSubmit::getProblemId, dataSubmitPageParam.getProblemId());
        }
        if (ObjectUtil.isNotEmpty(dataSubmitPageParam.getSetId())) {
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
        // 默认按时间降序
        queryWrapper.lambda().orderByDesc(DataSubmit::getCreateTime);

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
        this.handleSolvedRecord(dataSubmitExeParam, false, dataSubmit);
        this.asyncHandleSubmit(dataSubmit, dataSubmitExeParam);
        return dataSubmit.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handleSetSubmit(DataSubmitExeParam dataSubmitExeParam) {
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

    public void handleSolvedRecord(DataSubmitExeParam dataSubmitExeParam, Boolean isSet, DataSubmit dataSubmit) {
        String userId = StpUtil.getLoginIdAsString();
        String problemId = dataSubmitExeParam.getProblemId();
        String setId = dataSubmitExeParam.getSetId();
        String submitId = dataSubmit.getId();

        // 先模式
        if (isSet) {
            // 当前是否有解决状态
            LambdaQueryWrapper<DataSolved> queryWrapper = new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, userId)
                    .eq(DataSolved::getSetId, setId)
                    .eq(DataSolved::getProblemId, problemId)
                    .eq(DataSolved::getIsSet, Boolean.TRUE);
            boolean existingRecord = dataSolvedMapper.exists(queryWrapper);
            // 如果存在
            if (existingRecord) {
                // 存在就更新提交ID和更新时间
                dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, userId)
                        .eq(DataSolved::getSetId, setId)
                        .eq(DataSolved::getProblemId, problemId)
                        .eq(DataSolved::getIsSet, Boolean.TRUE)
                        .set(DataSolved::getSubmitId, submitId)
                        .set(DataSolved::getUpdateTime, new Date())
                );
            } else {
                // 不存在就插入
                DataSolved dataSolved = new DataSolved();
                dataSolved.setUserId(userId);
                dataSolved.setSetId(setId);
                dataSolved.setProblemId(problemId);
                dataSolved.setIsSet(Boolean.TRUE);
                dataSolved.setSubmitId(submitId);
                dataSolvedMapper.insert(dataSolved);
            }
        } else {
            // 当前是否有解决状态
            LambdaQueryWrapper<DataSolved> queryWrapper = new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, userId)
                    .eq(DataSolved::getProblemId, problemId)
                    .eq(DataSolved::getIsSet, Boolean.FALSE);
            boolean existingRecord = dataSolvedMapper.exists(queryWrapper);
            // 如果存在
            if (existingRecord) {
                // 存在就更新提交ID和更新时间
                dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, userId)
                        .eq(DataSolved::getProblemId, problemId)
                        .eq(DataSolved::getIsSet, Boolean.FALSE)
                        .set(DataSolved::getSubmitId, submitId)
                        .set(DataSolved::getUpdateTime, new Date())
                );
            } else {
                // 不存在就插入
                DataSolved dataSolved = new DataSolved();
                dataSolved.setUserId(userId);
                dataSolved.setProblemId(problemId);
                dataSolved.setIsSet(Boolean.TRUE);
                dataSolved.setSubmitId(submitId);
                dataSolvedMapper.insert(dataSolved);
            }
        }
    }

    @Async("taskExecutor")
    public void asyncHandleSubmit(DataSubmit dataSubmit, DataSubmitExeParam param) {
        try {
            DataProblem problem = dataProblemMapper.selectById(dataSubmit.getProblemId());

            JudgeSubmitDto message = new JudgeSubmitDto();
            // ======================= 任务参数 =======================
            message.setId(dataSubmit.getId());
            message.setUserId(dataSubmit.getUserId());
            message.setJudgeTaskId(param.getJudgeTaskId());
            message.setIsSet(dataSubmit.getIsSet());
            // ======================= 题目参数 =======================
            message.setMaxTime(problem.getMaxTime());
            message.setMaxMemory(problem.getMaxMemory());
            message.setTestCase(problem.getTestCase());
            // ======================= 用户提交参数 =======================
            message.setProblemId(param.getProblemId());
            if (param.getSetId() != null) {
                message.setSetId(param.getSetId());
            }
            message.setLanguage(param.getLanguage());
            message.setSubmitType(param.getSubmitType());

            // 处理代码模板
            if (problem.getUseTemplate()) {
                problem.getCodeTemplate().stream()
                        .filter(t -> t.getLanguage().equals(param.getLanguage().toLowerCase()))
                        .findFirst()
                        .ifPresent(template -> {
                            if (ObjectUtil.isNotEmpty(template.getPrefix()) && ObjectUtil.isNotEmpty(template.getSuffix())) {
                                String code = template.getPrefix() + param.getCode() + template.getSuffix();
                                log.info("处理代码模板成功，提交ID: {} 代码内容: {}", dataSubmit.getId(), code);
                                message.setCode(code);
                            }
                        });
            } else {
                log.info("未使用代码模板，提交ID: {} 提交内容: {}", dataSubmit.getId(), param.getCode());
                message.setCode(param.getCode());
            }

            judgeHandleMessage.sendJudge(message, dataSubmit);

            log.info("题目提交消息已发送到队列，提交ID: {}", dataSubmit.getId());
        } catch (Exception e) {
            log.error("发送题目提交消息到队列失败，提交ID: {}", dataSubmit.getId(), e);
            throw new BusinessException("提交失败，请稍后重试");
        }
    }

}