package io.charlie.web.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.charlie.web.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.modular.data.ranking.service.UserActivityService;
import io.charlie.web.modular.data.submit.entity.DataSubmit;
import io.charlie.web.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.modular.task.judge.mq.JudgeQueueProperties;
import io.charlie.web.modular.task.library.dto.Library;
import io.charlie.web.modular.task.library.handle.LibraryHandleMessage;
import io.charlie.web.modular.task.solved.dto.SolvedMessage;
import io.charlie.web.modular.task.solved.handle.SolvedHandleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

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
    private static final int UPDATE_MAX_ATTEMPTS = 3;
    private static final long UPDATE_RETRY_DELAY_MS = 100L;

    private final RabbitTemplate rabbitTemplate;
    private final DataSubmitMapper dataSubmitMapper;
    private final LibraryHandleMessage libraryHandleMessage;

    private final UserActivityService userActivityService;

    private final JudgeQueueProperties judgeQueueProperties;

    private final SolvedHandleMessage solvedHandleMessage;

    public void sendJudge(JudgeSubmitDto judgeSubmitDto) {
        rabbitTemplate.convertAndSend(
                judgeQueueProperties.getCommon().getExchange(),
                judgeQueueProperties.getCommon().getRoutingKey(),
                judgeSubmitDto
        );
    }

    @DS("master")
    @RabbitListener(queues = "${oj.mq.judge.result.queue}", containerFactory = "judgeResultContainerFactory")
    public void receiveJudge(JudgeResultDto judgeResultDto) {
        // 1. 更新提交记录（短重试，应对事务刚提交/主从瞬时可见性）
        String submitRecord = updateSubmitRecordWithRetry(judgeResultDto);
        if (submitRecord == null) {
            // 抛错走 listener 重试，避免静默 ACK 导致 submit 卡 PENDING、下游永不写
            throw new IllegalStateException("更新提交记录失败：id=" + judgeResultDto.getId());
        }

        // 2. 根据提交类型处理业务逻辑
        processBusinessLogic(judgeResultDto, submitRecord);
    }

    /**
     * 带短重试的提交记录更新
     */
    private String updateSubmitRecordWithRetry(JudgeResultDto judgeResultDto) {
        for (int attempt = 1; attempt <= UPDATE_MAX_ATTEMPTS; attempt++) {
            String submitId = updateSubmitRecord(judgeResultDto);
            if (submitId != null) {
                return submitId;
            }
            if (attempt < UPDATE_MAX_ATTEMPTS) {
                try {
                    Thread.sleep(UPDATE_RETRY_DELAY_MS * attempt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                log.warn("提交记录未找到，重试 {}/{}：id={}", attempt, UPDATE_MAX_ATTEMPTS, judgeResultDto.getId());
            }
        }
        return null;
    }

    /**
     * 更新提交记录（主库）
     */
    @DS("master")
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
            return judgeResultDto.getId();
        } else {
            log.warn("未找到对应的提交记录：id={}", judgeResultDto.getId());
            return null;
        }
    }

    /**
     * 处理业务逻辑：正式提交同步发 solved/library，保证模块上下文人队
     */
    public void processBusinessLogic(JudgeResultDto judgeResultDto, String id) {
        if (!Boolean.TRUE.equals(judgeResultDto.getSubmitType())) {
            log.info("测试提交，跳过业务处理：id={}", judgeResultDto.getId());
            return;
        }

        if (StrUtil.isBlank(judgeResultDto.getModuleType()) || StrUtil.isBlank(judgeResultDto.getModuleId())) {
            log.error("判题结果缺少模块字段，跳过 solved/library：id={}, moduleType={}, moduleId={}",
                    judgeResultDto.getId(), judgeResultDto.getModuleType(), judgeResultDto.getModuleId());
            return;
        }

        SolvedMessage solvedMessage = BeanUtil.copyProperties(judgeResultDto, SolvedMessage.class);
        solvedMessage.setSubmitId(id);

        if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
            solvedMessage.setSolved(Boolean.TRUE);

            try {
                Library library = BeanUtil.copyProperties(judgeResultDto, Library.class);
                library.setSubmitId(id);
                libraryHandleMessage.sendLibrary(library);
            } catch (Exception e) {
                log.error("发送样本库消息失败：userId={}, moduleType={}, moduleId={}",
                        judgeResultDto.getUserId(), judgeResultDto.getModuleType(), judgeResultDto.getModuleId(), e);
                throw e;
            }

            if ("PROBLEM".equals(judgeResultDto.getModuleType())) {
                userActivityService.addActivity(judgeResultDto.getUserId(),
                        ActivityScoreCalculator.SUBMIT, Boolean.TRUE);
            }
        } else {
            solvedMessage.setSolved(Boolean.FALSE);

            if ("PROBLEM".equals(judgeResultDto.getModuleType())) {
                userActivityService.addActivity(judgeResultDto.getUserId(),
                        ActivityScoreCalculator.SUBMIT, Boolean.FALSE);
            }
        }

        try {
            solvedHandleMessage.sendSolved(solvedMessage);
        } catch (Exception e) {
            log.error("发送解题记录消息失败：userId={}, moduleType={}, moduleId={}",
                    judgeResultDto.getUserId(), judgeResultDto.getModuleType(), judgeResultDto.getModuleId(), e);
            throw e;
        }
    }
}
