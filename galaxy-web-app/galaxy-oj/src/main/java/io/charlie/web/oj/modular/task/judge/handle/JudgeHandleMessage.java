package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.ranking.service.SetUserRankingService;
import io.charlie.web.oj.modular.data.ranking.service.UserRankingService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final WebSocketUtil webSocketUtil;
    private final DataSubmitMapper dataSubmitMapper;
    private final DataSolvedMapper dataSolvedMapper;
    private final RankingUtil rankingUtil;
    private final DataLibraryService dataLibraryService;
    private final SimilarityHandleMessage similarityHandleMessage;
    private final UserRankingService userRankingService;
    private final SetUserRankingService setUserRankingService;
    private final TransService transService;

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

        // 事务提交后，再触发异步处理
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 确保主事务提交后再执行异步任务
                        if (judgeResultDto.getSubmitType()) {
                            log.info("事务提交完成，开始异步处理");
                            asyncHandleAdditionalTasks(judgeResultDto, dataSubmit);
                        }
                    }
                }
        );

//        try {


//            log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));
//
//            // 查询这个ID的提交
//            DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
//
//            // 复制属性
//            BeanUtil.copyProperties(judgeResultDto, dataSubmit);
//            dataSubmitMapper.updateById(dataSubmit);
//
//            System.out.println(JSONUtil.toJsonPrettyStr(dataSubmit));
//
//            // 正式提交
//            if (judgeResultDto.getSubmitType()) {
//                log.info("正式提交");
//                // 通过
//                if (judgeResultDto.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
//                    // 排行榜更新
//                    userRankingService.processSubmission(dataSubmit);
//
//                    // 更新 solved
//                    dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
//                            .eq(DataSolved::getUserId, dataSubmit.getUserId())
//                            .eq(DataSolved::getProblemId, dataSubmit.getProblemId())
//                            .eq(DataSolved::getSubmitId, judgeResultDto.getId())
//                            .set(DataSolved::getSolved, true)
//                    );
//
//                    // 添加到 library
//                    DataSubmit dataSubmit1 = dataSubmitMapper.selectById(judgeResultDto.getId());
//                    dataLibraryService.addLibrary(dataSubmit1);
//
//                    // 本次提交相似报告
//                    String taskId = IdUtil.getSnowflakeNextIdStr();
//                    dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>()
//                            .eq(DataSubmit::getId, dataSubmit.getId())
//                            .set(DataSubmit::getTaskId, taskId)
//                    );
//
//                    SimilaritySubmitDto similaritySubmitDto = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
//                    similaritySubmitDto.setTaskId(taskId);
//                    similaritySubmitDto.setTaskType(false);
//                    similaritySubmitDto.setCreateTime(dataSubmit.getCreateTime());
//                    similarityHandleMessage.sendSimilarity(similaritySubmitDto);
//                }
//            } else {
//                log.info("测试提交");
//            }
//
//            transService.transOne(dataSubmit);
//            WebSocketMessage<DataSubmit> message = new WebSocketMessage<>();
//            message.setData(dataSubmit);
//
//            log.info("向客户端发送消息：{}", JSONUtil.toJsonStr(message));
//
//            webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeResultDto.getJudgeTaskId(), message);
//            webSocketUtil.sendToTopicClose(WebSocketConfig.TOPIC_JUDGE_STATUS, judgeResultDto.getJudgeTaskId());
//            log.info("向客户端 发送关闭指令");


//        } catch (Exception e) {
//            log.error("处理消息失败：{}", e.getMessage());
//        }
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
            log.error("异步处理附加任务失败：{}", e.getMessage());
        }
    }

    /**
     * 异步处理正式提交
     */
    private void handleFormalSubmissionAsync(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        if (judgeResultDto.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
            // 1. 排行榜更新
            userRankingService.processSubmission(dataSubmit);
            if (dataSubmit.getIsSet()) {
                setUserRankingService.processSubmission(dataSubmit);
            }

            // 2. 更新 solved
            updateSolvedRecord(judgeResultDto, dataSubmit);

            // 3. 添加到 library
            dataLibraryService.addLibrary(dataSubmit);

            // 4. 相似度检测
            handleSimilarityCheck(judgeResultDto, dataSubmit);
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
        similaritySubmitDto.setCreateTime(dataSubmit.getCreateTime());
        similarityHandleMessage.sendSimilarity(similaritySubmitDto);
    }
}
