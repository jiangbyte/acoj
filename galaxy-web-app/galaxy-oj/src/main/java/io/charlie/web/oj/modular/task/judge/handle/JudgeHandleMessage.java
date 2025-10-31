package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.mq.JudgeQueueProperties;
import io.charlie.web.oj.modular.task.library.DTO.Library;
import io.charlie.web.oj.modular.task.library.handle.LibraryHandleMessage;
import io.charlie.web.oj.modular.task.similarity.handle.SimilarityHandleMessage;
import io.charlie.web.oj.modular.task.similarity.msg.SimilarityMessage;
import io.charlie.web.oj.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.oj.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

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
    private final LibraryHandleMessage libraryHandleMessage;

    private final UserActivityService userActivityService;

    private final JudgeQueueProperties judgeQueueProperties;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));
        rabbitTemplate.convertAndSend(
                judgeQueueProperties.getCommon().getExchange(),
                judgeQueueProperties.getCommon().getRoutingKey(),
                judgeSubmitDto
        );
        log.info("发送消息成功");
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.judge.result.queue}", concurrency = "10-20")
    public void receiveJudge(JudgeResultDto judgeResultDto) {
        log.info("接收到判题结果消息：id={}, status={}",
                judgeResultDto.getId(), judgeResultDto.getStatus());

        // 1. 更新提交记录
        String s = updateSubmitRecord(judgeResultDto);

        // 2. 根据提交类型处理业务逻辑
        processBusinessLogic(judgeResultDto, s);
    }

    /**
     * 处理业务逻辑
     */
    private void processBusinessLogic(JudgeResultDto judgeResultDto, String id) {
        if (!judgeResultDto.getSubmitType()) {
            log.debug("测试提交，跳过业务处理：id={}", judgeResultDto.getId());
            return;
        }

        if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
            log.info("正式提交 AC，进行额外处理：id={}", judgeResultDto.getId());
            handleAcceptedSubmission(judgeResultDto, id);
        } else {
            log.debug("非AC正式提交：id={}, status={}", judgeResultDto.getId(), judgeResultDto.getStatus());
        }
    }

    /**
     * 处理AC提交的额外任务
     */
    private void handleAcceptedSubmission(JudgeResultDto judgeResultDto, String sid) {
        // 异步处理耗时操作，避免阻塞消息消费
        CompletableFuture.runAsync(() -> {
            try {
                // 添加用户活动
                userActivityService.addActivity(judgeResultDto.getUserId(),
                        ActivityScoreCalculator.SUBMIT, true);
                // 处理额外任务
                handleAdditionalTasks(judgeResultDto, sid);
            } catch (Exception e) {
                log.error("处理AC提交额外任务失败：userId={}", judgeResultDto.getUserId(), e);
            }
        });
    }

    /**
     * 更新提交记录
     */
    private String updateSubmitRecord(JudgeResultDto judgeResultDto) {
        DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
        if (dataSubmit == null) {
            log.warn("未找到对应的提交记录：id={}", judgeResultDto.getId());
            return null;
        }

        BeanUtil.copyProperties(judgeResultDto, dataSubmit);
        dataSubmit.setIsFinish(Boolean.TRUE);
        dataSubmitMapper.updateById(dataSubmit);
        log.info("更新提交记录成功：id={}", judgeResultDto.getId());

        return dataSubmit.getId();
    }

    /**
     * 异步处理附加任务
     */
    public void handleAdditionalTasks(JudgeResultDto judgeResultDto, String id) {
        try {
            log.info("开始处理正式提交的附加任务");
            updateSolved(judgeResultDto);

            if (id != null) {
                Library library = BeanUtil.copyProperties(judgeResultDto, Library.class);
                library.setSubmitId(id);
                libraryHandleMessage.sendLibrary(library);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("异步处理附加任务失败：{}", e.getMessage());
        }
    }

    public void updateSolved(JudgeResultDto judgeResultDto) {
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
    }
}
