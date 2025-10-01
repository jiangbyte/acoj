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
import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import io.charlie.web.oj.modular.websocket.data.WebSocketMessage;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
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
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final DataLibraryService dataLibraryService;
    private final SimilarityHandleMessage similarityHandleMessage;
    private final TransService transService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto, DataSubmit dataSubmit) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));

        WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
        message.setData(dataSubmit);

        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeSubmitDto.getJudgeTaskId(), message);

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
        // 核心数据库更新 - 同步处理确保数据一致性
        DataSubmit dataSubmit = updateSubmitData(judgeResultDto);

        // 立即响应客户端
        sendImmediateResponse(judgeResultDto, dataSubmit);

        // 只有在AC状态且为正式提交时，才注册事务同步处理器
        if (isAcAndFormalSubmission(judgeResultDto)) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.info("事务提交完成，开始异步处理AC提交的附加任务");
                            asyncHandleAdditionalTasks(judgeResultDto, dataSubmit);
                        }
                    }
            );
        } else {
            // 否则直接返回关闭
            // 立即响应关闭客户端
            sendImmediateCloseResponse(judgeResultDto, dataSubmit);
        }
    }

    /**
     * 判断是否为AC状态且为正式提交
     */
    private boolean isAcAndFormalSubmission(JudgeResultDto judgeResultDto) {
        return JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus()) && judgeResultDto.getSubmitType();
    }

    /**
     * 核心数据更新 - 同步处理
     */
    private DataSubmit updateSubmitData(JudgeResultDto judgeResultDto) {
        DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
        BeanUtil.copyProperties(judgeResultDto, dataSubmit);
        dataSubmitMapper.updateById(dataSubmit);
        return dataSubmit;
    }

    /**
     * 发送即时响应
     */
    private void sendImmediateResponse(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        transService.transOne(dataSubmit);
        WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
        message.setData(dataSubmit);

        log.info("向客户端发送消息：{}", JSONUtil.toJsonStr(message));
        webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeResultDto.getJudgeTaskId(), message);
    }

    private void sendImmediateCloseResponse(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        transService.transOne(dataSubmit);
        WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
        message.setData(dataSubmit);
        log.info("向客户端发送消息：{}", JSONUtil.toJsonStr(message));
        webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeResultDto.getJudgeTaskId());
    }

    /**
     * 异步处理附加任务
     */
    @Async("judgeTaskExecutor")
    public void asyncHandleAdditionalTasks(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        try {
            if (judgeResultDto.getSubmitType()) {
                log.info("开始异步处理正式提交的附加任务");
                handleFormalSubmissionAsync(judgeResultDto, dataSubmit);
            } else {
                log.info("测试提交无需异步处理");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("异步处理附加任务失败：{}", e.getMessage());
        }
    }

    /**
     * 异步处理正式提交
     */
    private void handleFormalSubmissionAsync(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        if (judgeResultDto.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
            // 2. 排行榜更新
            handleRedisRecord(judgeResultDto, dataSubmit);

            // 3. 更新 solved
            updateSolvedRecord(judgeResultDto, dataSubmit);

            // 4. 添加到 library
            dataLibraryService.addLibrary(dataSubmit);

            // 5. 相似度检测
            handleSimilarityCheck(judgeResultDto, dataSubmit);
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

        if (isSet) {
            // 题集参与度
            problemSetCacheService.addProblemSetParticipant(setId, userId);
            if (submitType) {
                // 题集内题目的提交度
                problemSetCacheService.addProblemSetProblemSubmit(setId, problemId, userId);
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
            if (submitType) {
                // 提交度
                problemCacheService.addSubmitUser(problemId, userId);
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
     * 更新 solved 记录
     */
    private void updateSolvedRecord(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                .eq(DataSolved::getUserId, dataSubmit.getUserId())
                .eq(DataSolved::getProblemId, dataSubmit.getProblemId())
                .eq(DataSolved::getSubmitId, judgeResultDto.getId())
                .eq(judgeResultDto.getIsSet(), DataSolved::getSetId, dataSubmit.getSetId())
                .set(DataSolved::getSolved, true)
        );
    }

    /**
     * 处理相似度检测
     */
    private void handleSimilarityCheck(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        String taskId = IdUtil.getSnowflakeNextIdStr();
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
                .eq(DataSubmit::getId, dataSubmit.getId())
                .set(DataSubmit::getTaskId, taskId)
        );

        SimilaritySubmitDto similaritySubmitDto = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
        similaritySubmitDto.setTaskId(taskId);
        similaritySubmitDto.setTaskType(false);
        similaritySubmitDto.setJudgeTaskId(judgeResultDto.getJudgeTaskId());
        similaritySubmitDto.setCreateTime(dataSubmit.getCreateTime());

        similarityHandleMessage.sendSimilarity(similaritySubmitDto);
    }
}
