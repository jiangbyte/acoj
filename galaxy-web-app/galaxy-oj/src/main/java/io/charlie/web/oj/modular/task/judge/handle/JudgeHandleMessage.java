package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.mq.CommonJudgeQueue;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.handle.SimilarityHandleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.trans.service.impl.TransService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 判题消息处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final DataLibraryService dataLibraryService;
    private final SimilarityHandleMessage similarityHandleMessage;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto, DataSubmit dataSubmit) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));
        rabbitTemplate.convertAndSend(
                CommonJudgeQueue.EXCHANGE,
                CommonJudgeQueue.ROUTING_KEY,
                judgeSubmitDto
        );
        log.info("发送消息成功");
    }

    @Transactional
    @RabbitListener(queues = CommonJudgeQueue.BACK_QUEUE, concurrency = "5-10")
    public void receiveJudge(JudgeResultDto judgeResultDto) {
        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        // 核心数据库更新
        DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
        BeanUtil.copyProperties(judgeResultDto, dataSubmit);
        dataSubmitMapper.updateById(dataSubmit);

        // 排行榜更新
//        handleRedisRecord(judgeResultDto, dataSubmit);

        // 正式提交
        if (judgeResultDto.getSubmitType()) {
            // ac情况
            if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
                log.info("正式提交 AC，进行额外处理");
                asyncHandleAdditionalTasks(judgeResultDto, dataSubmit);
            } else {
                // 非 ac 情况
                log.info("非 AC 提交，不进行额外处理");
                stopStatus(judgeResultDto.getId());
            }
        } else {
            // 测试提交
            log.info("测试提交，不进行额外处理");
            stopStatus(judgeResultDto.getId());
        }
    }

    private void stopStatus(String id) {
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, id)
                .set(DataSubmit::getIsFinish, Boolean.TRUE)
        );
    }

    /**
     * 异步处理附加任务
     */
    @Async("judgeTaskExecutor")
    public void asyncHandleAdditionalTasks(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        try {
            log.info("开始异步处理正式提交的附加任务");
            if (judgeResultDto.getIsSet()) {
                // 更新 solved
                dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, judgeResultDto.getUserId())
                        .eq(DataSolved::getProblemId, judgeResultDto.getProblemId())
                        .eq(DataSolved::getSetId, judgeResultDto.getSetId())
                        .eq(DataSolved::getSubmitId, judgeResultDto.getId())
                        .set(DataSolved::getSolved, Boolean.TRUE)
                );
            } else {
                // 更新 solved
                dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                        .eq(DataSolved::getUserId, judgeResultDto.getUserId())
                        .eq(DataSolved::getProblemId, judgeResultDto.getProblemId())
                        .eq(DataSolved::getSubmitId, judgeResultDto.getId())
                        .set(DataSolved::getSolved, Boolean.TRUE)
                );
            }

            // 从JudgeResultDto 中数据复制到 DataSubmit
            BeanUtil.copyProperties(judgeResultDto, dataSubmit);
            // 添加到 library
            dataLibraryService.addLibrary(dataSubmit);
            // 相似度检测
            handleSimilarityCheck(judgeResultDto, dataSubmit);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("异步处理附加任务失败：{}", e.getMessage());
        }
    }

    private void handleRedisRecord(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        Boolean isSet = judgeResultDto.getIsSet();

        String userId = judgeResultDto.getUserId();
        String problemId = judgeResultDto.getProblemId();
        String setId = judgeResultDto.getSetId();

        Boolean submitType = judgeResultDto.getSubmitType();

        boolean isAc = JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus());

        // 用户活跃度
        userCacheService.addUserActivity(userId, 0.01);

        // 题集模式
        if (isSet) {
            // 题集参与度
            problemSetCacheService.addProblemSetParticipant(setId, userId);
            // 正式提交
            if (submitType) {
                // 题集内题目的提交度
                problemSetCacheService.addProblemSetProblemSubmit(setId, problemId, userId);
                // 通过
                if (isAc) {
                    problemSetCacheService.addProblemSetProblemAccept(setId, problemId, userId);
                }
            } else {
                // 题集内题目的尝试度
                problemSetCacheService.addProblemSetProblemAttempt(setId, problemId, userId);
            }
        } else {
            // 题目参与度
            problemCacheService.addParticipantUser(problemId, userId);
            // 正式提交
            if (submitType) {
                // 提交度
                problemCacheService.addSubmitUser(problemId, userId);
                // 通过
                if (isAc) {
                    problemCacheService.addAcceptUser(problemId, userId);
                    userCacheService.addUserAcceptedProblem(userId, problemId);
                }
            } else {
                // 尝试度
                problemCacheService.addAttemptUser(problemId, userId);
            }
        }
    }

    /**
     * 处理相似度检测
     */
    private void handleSimilarityCheck(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        String taskId = IdUtil.getSnowflakeNextIdStr();

        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, judgeResultDto.getId())
                .set(DataSubmit::getTaskId, taskId) // 克隆检测任务ID
        );

        SimilaritySubmitDto similaritySubmitDto = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
        similaritySubmitDto.setTaskId(taskId);
        similaritySubmitDto.setTaskType(Boolean.FALSE);
        similaritySubmitDto.setJudgeTaskId(judgeResultDto.getJudgeTaskId());
        similaritySubmitDto.setCreateTime(dataSubmit.getCreateTime());

        similarityHandleMessage.sendSimilarity(similaritySubmitDto);
    }
}
