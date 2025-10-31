package io.charlie.web.oj.modular.data.submit.event.set;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.handle.JudgeHandleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SetSubmitEventListener {
    private final JudgeHandleMessage judgeHandleMessage;
    private final DataProblemMapper dataProblemMapper;

    private final UserActivityService userActivityService;

    private final DataSolvedMapper dataSolvedMapper;

    private final RedissonClient redissonClient;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSetSubmitEvent(SetSubmitEvent event) {
        log.info("处理题目提交事件，提交ID: {}", event.dataSubmit().getId());

        try {
            // 异步处理提交到判题系统
            asyncHandleSubmit(event.dataSubmit(), event.dataSubmitExeParam());

            handleSolvedRecord(event.dataSubmitExeParam(), event.dataSubmit());

            userActivityService.addActivity(event.dataSubmit().getUserId(), ActivityScoreCalculator.SUBMIT, Boolean.FALSE);
        } catch (Exception e) {
            log.error("处理题目提交事件失败，提交ID: {}", event.dataSubmit().getId(), e);
        }
    }

    @Async("taskExecutor")
    public void asyncHandleSubmit(DataSubmit dataSubmit, DataSubmitExeParam param) {
        try {
            DataProblem problem = dataProblemMapper.selectById(dataSubmit.getProblemId());

            JudgeSubmitDto message = new JudgeSubmitDto();

            message.setId(dataSubmit.getId());
            message.setUserId(dataSubmit.getUserId());
            message.setJudgeTaskId(param.getJudgeTaskId());
            message.setIsSet(Boolean.TRUE);
            message.setSetId(param.getSetId());
            message.setMaxTime(problem.getMaxTime());
            message.setMaxMemory(problem.getMaxMemory());
            message.setProblemId(param.getProblemId());
            message.setLanguage(param.getLanguage());
            message.setSubmitType(param.getSubmitType());
            message.setCode(param.getCode());

            judgeHandleMessage.sendJudge(message);

            log.info("题目提交消息已发送到队列，提交ID: {}", dataSubmit.getId());
        } catch (Exception e) {
            log.error("发送题目提交消息到队列失败，提交ID: {}", dataSubmit.getId(), e);
            throw new BusinessException("提交失败，请稍后重试");
        }
    }


    @Retryable(value = {DeadlockLoserDataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleSolvedRecord(DataSubmitExeParam dataSubmitExeParam, DataSubmit dataSubmit) {
        String userId = dataSubmit.getUserId();
        String lockKey = String.format("solved:lock:%s:%s",
                userId, dataSubmitExeParam.getSetId());

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(1, 30, TimeUnit.SECONDS)) {
                doHandleSolvedRecord(dataSubmitExeParam, Boolean.TRUE, dataSubmit);
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
        String userId = dataSubmit.getUserId();
        String problemId = dataSubmitExeParam.getProblemId();
        String setId = dataSubmitExeParam.getSetId();
        String submitId = dataSubmit.getId();

        try {
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
        } catch (Exception e) {
            log.error("处理解题记录失败，用户:{} 问题:{} 模式:{}", userId, problemId, isSet, e);
            throw new BusinessException("处理解题记录失败", e);
        }
    }
}
