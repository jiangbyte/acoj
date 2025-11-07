package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.mq.JudgeQueueProperties;
import io.charlie.web.oj.modular.task.library.dto.Library;
import io.charlie.web.oj.modular.task.library.handle.LibraryHandleMessage;
import io.charlie.web.oj.modular.task.solved.dto.SolvedMessage;
import io.charlie.web.oj.modular.task.solved.handle.SolvedHandleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
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
    private final LibraryHandleMessage libraryHandleMessage;

    private final UserActivityService userActivityService;

    private final JudgeQueueProperties judgeQueueProperties;

    private final SolvedHandleMessage solvedHandleMessage;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto) {
        log.debug("发送判题消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));
        rabbitTemplate.convertAndSend(
                judgeQueueProperties.getCommon().getExchange(),
                judgeQueueProperties.getCommon().getRoutingKey(),
                judgeSubmitDto
        );
        log.debug("发送消息成功");
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.judge.result.queue}", containerFactory = "judgeResultContainerFactory")
    public void receiveJudge(JudgeResultDto judgeResultDto) {
        log.debug("接收到判题结果消息：id={}, status={}", judgeResultDto.getId(), judgeResultDto.getStatus());

        // 1. 更新提交记录
        String submitRecord = updateSubmitRecord(judgeResultDto);

        // 2. 根据提交类型处理业务逻辑
        processBusinessLogic(judgeResultDto, submitRecord);
    }

    /**
     * 更新提交记录
     */
    public String updateSubmitRecord(JudgeResultDto judgeResultDto) {
        LambdaUpdateWrapper<DataSubmit> lambda = new UpdateWrapper<DataSubmit>().checkSqlInjection().lambda();
        lambda.eq(DataSubmit::getId, judgeResultDto.getId())
                .set(DataSubmit::getIsFinish, Boolean.TRUE)
                .set(DataSubmit::getMaxMemory, judgeResultDto.getMaxMemory())
                .set(DataSubmit::getMaxTime, judgeResultDto.getMaxTime())
                .set(DataSubmit::getMessage, judgeResultDto.getMessage())
                .set(DataSubmit::getStatus, judgeResultDto.getStatus())
                .set(DataSubmit::getJudgeTaskId, judgeResultDto.getJudgeTaskId())
        ;

        int updated = dataSubmitMapper.update(lambda);

        if (updated > 0) {
            log.debug("更新提交记录成功：id={}", judgeResultDto.getId());
            return judgeResultDto.getId();
        } else {
            log.warn("未找到对应的提交记录：id={}", judgeResultDto.getId());
            return null;
        }
    }

    /**
     * 处理业务逻辑
     */
    public void processBusinessLogic(JudgeResultDto judgeResultDto, String id) {
        if (!judgeResultDto.getSubmitType()) {
            log.debug("测试提交，跳过业务处理：id={}", judgeResultDto.getId());
            return;
        }

        SolvedMessage solvedMesage = BeanUtil.copyProperties(judgeResultDto, SolvedMessage.class);
        solvedMesage.setSubmitId(id);

        if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
            log.debug("正式提交 AC，进行额外处理：id={}", judgeResultDto.getId());
            solvedMesage.setSolved(Boolean.TRUE);

            CompletableFuture.runAsync(() -> {
                try {
                    if (id != null) {
                        Library library = BeanUtil.copyProperties(judgeResultDto, Library.class);
                        library.setSubmitId(id);
                        libraryHandleMessage.sendLibrary(library);
                    }
                } catch (Exception e) {
                    log.error("处理AC提交额外任务失败：userId={}", judgeResultDto.getUserId(), e);
                }
            });

            if (judgeResultDto.getModuleType().equals("PROBLEM")) {
                // 添加用户活动
                userActivityService.addActivity(judgeResultDto.getUserId(),
                        ActivityScoreCalculator.SUBMIT, Boolean.TRUE);
            }
        } else {
            log.debug("非AC正式提交：id={}, status={}", judgeResultDto.getId(), judgeResultDto.getStatus());
            solvedMesage.setSolved(Boolean.FALSE);

            if (judgeResultDto.getModuleType().equals("PROBLEM")) {
                // 添加用户活动
                userActivityService.addActivity(judgeResultDto.getUserId(),
                        ActivityScoreCalculator.SUBMIT, Boolean.FALSE);
            }
        }

        CompletableFuture.runAsync(() -> {
            try {
                solvedHandleMessage.sendSolved(solvedMesage);
            } catch (Exception e) {
                log.error("更改解决记录失败：userId={}", judgeResultDto.getUserId(), e);
            }
        });
    }
}
