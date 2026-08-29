package io.charlie.web.modular.task.solved.handle;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.modular.data.solved.entity.DataSolved;
import io.charlie.web.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.modular.task.solved.dto.SolvedMessage;
import io.charlie.web.modular.task.solved.mq.SolvedQueueProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description 解题记录消息处理（主库 upsert）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolvedHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final SolvedQueueProperties solvedQueueProperties;
    private final DataSolvedMapper dataSolvedMapper;

    public void sendSolved(SolvedMessage dataSubmit) {
        rabbitTemplate.convertAndSend(
                solvedQueueProperties.getCommon().getExchange(),
                solvedQueueProperties.getCommon().getRoutingKey(),
                dataSubmit
        );
        log.debug("发送解题记录消息成功");
    }

    @DS("master")
    @Transactional
    @RabbitListener(queues = "${oj.mq.solved.common.queue}", concurrency = "1")
    public void receiveSolved(SolvedMessage submit) {
        try {
            processSolvedRecord(submit);
            log.debug("处理解题记录成功, userId: {}, problemId: {}", submit.getUserId(), submit.getProblemId());
        } catch (Exception e) {
            log.error("处理解题记录失败, userId: {}, problemId: {}, submitId: {}", submit.getUserId(), submit.getProblemId(), submit.getSubmitId(), e);
            throw new RuntimeException("处理解题记录失败", e);
        }
    }

    /**
     * 可靠 upsert：先 update，0 行再 insert，撞 UK 再 update。
     * AC 单调：已 solved=1 不再降为 0。
     * 依赖唯一索引 uk_user_problem_module (user_id, problem_id, module_type, module_id)
     */
    @Transactional(rollbackFor = Exception.class)
    public void processSolvedRecord(SolvedMessage submit) {
        String userId = submit.getUserId();
        String problemId = submit.getProblemId();
        String submitId = submit.getSubmitId();
        String moduleType = submit.getModuleType();
        String moduleId = submit.getModuleId();
        boolean solved = Boolean.TRUE.equals(submit.getSolved());
        Date now = new Date();

        int updated = updateSolvedRecord(userId, problemId, moduleType, moduleId, submitId, solved, now);
        if (updated > 0) {
            return;
        }

        DataSolved dataSolved = new DataSolved();
        dataSolved.setUserId(userId);
        dataSolved.setProblemId(problemId);
        dataSolved.setSubmitId(submitId);
        dataSolved.setSolved(solved);
        dataSolved.setModuleType(moduleType);
        dataSolved.setModuleId(moduleId);
        dataSolved.setCreateTime(now);
        dataSolved.setUpdateTime(now);

        try {
            dataSolvedMapper.insert(dataSolved);
        } catch (Exception e) {
            if (isDuplicateKeyException(e)) {
                updateSolvedRecord(userId, problemId, moduleType, moduleId, submitId, solved, now);
            } else {
                throw e;
            }
        }
    }

    private int updateSolvedRecord(String userId, String problemId, String moduleType, String moduleId,
                                   String submitId, boolean solved, Date now) {
        return dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                .eq(DataSolved::getUserId, userId)
                .eq(DataSolved::getProblemId, problemId)
                .eq(DataSolved::getModuleType, moduleType)
                .eq(DataSolved::getModuleId, moduleId)
                .set(DataSolved::getSubmitId, submitId)
                .set(DataSolved::getUpdateTime, now)
                // 已 AC 则保持 solved=1，否则按本次结果更新
                .setSql("solved = CASE WHEN solved = 1 THEN 1 ELSE " + (solved ? 1 : 0) + " END")
        );
    }

    private boolean isDuplicateKeyException(Exception e) {
        Throwable cur = e;
        while (cur != null) {
            String message = cur.getMessage();
            if (message != null && (message.contains("Duplicate entry") || message.contains("1062"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
