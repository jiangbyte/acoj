package io.charlie.web.oj.modular.data.submit.handle.set;

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
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
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
public class SetSubmitHandle {
    private final JudgeHandleMessage judgeHandleMessage;
    private final DataProblemMapper dataProblemMapper;

    public void handle(SetSubmitMessage message1) {
        log.info("处理提交，提交ID: {}", message1.dataSubmit().getId());
        try {
            // 异步处理提交到判题系统
            DataSubmit dataSubmit = message1.dataSubmit();
            DataSubmitExeParam param = message1.dataSubmitExeParam();

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
        } catch (Exception e) {
            log.error("处理提交失败，提交ID: {}", message1.dataSubmit().getId(), e);
            throw new BusinessException("处理被中断", e);
        }
    }
}
