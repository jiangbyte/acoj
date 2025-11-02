package io.charlie.web.oj.modular.task.solved.handle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.task.solved.dto.SolvedMessage;
import io.charlie.web.oj.modular.task.solved.mq.SolvedQueueProperties;
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
 * @description TODO
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
        log.debug("发送样本库消息成功");
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.solved.common.queue}", concurrency = "1")
    public void receiveSolved(SolvedMessage submit) {
        try {
            processSolvedRecord(submit);
            log.debug("处理解题记录成功, userId: {}, problemId: {}",
                    submit.getUserId(), submit.getProblemId());
        } catch (Exception e) {
            log.error("处理解题记录失败, userId: {}, problemId: {}, submitId: {}",
                    submit.getUserId(), submit.getProblemId(), submit.getSubmitId(), e);
            throw new RuntimeException("处理解题记录失败", e); // 必须抛出异常
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSolvedRecord(SolvedMessage submit) {
        String userId = submit.getUserId();
        String problemId = submit.getProblemId();
        String submitId = submit.getSubmitId();
        String setId = submit.getSetId();
        Boolean isSet = submit.getIsSet();

        // 检查记录是否存在
        LambdaQueryWrapper<DataSolved> eq = new LambdaQueryWrapper<DataSolved>()
                .eq(DataSolved::getUserId, userId)
                .eq(DataSolved::getProblemId, problemId)
                .eq(DataSolved::getIsSet, isSet)
                .eq(isSet != null && isSet, DataSolved::getSetId, setId);

        DataSolved dataSolved1 = dataSolvedMapper.selectOne(eq);

        if (dataSolved1 != null) {
            // 更新现有记录
            dataSolvedMapper.update(new LambdaUpdateWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, userId)
                    .eq(DataSolved::getProblemId, problemId)
                    .eq(DataSolved::getIsSet, isSet)
                    .eq(isSet != null && isSet, DataSolved::getSetId, setId)
                    .set(DataSolved::getSubmitId, submitId)
                    .set(DataSolved::getUpdateTime, new Date())
                    .set(!dataSolved1.getSolved(), DataSolved::getSolved, submit.getSolved()) // 如果已解决，则不更新
            );
        } else {
            // 插入新记录
            DataSolved dataSolved = new DataSolved();
            dataSolved.setUserId(userId);
            dataSolved.setProblemId(problemId);
            dataSolved.setSubmitId(submitId);
            dataSolved.setSolved(submit.getSolved());
            dataSolved.setIsSet(isSet);
            dataSolved.setSetId(setId);
            dataSolved.setCreateTime(new Date());
            dataSolved.setUpdateTime(new Date());
            dataSolvedMapper.insert(dataSolved);
        }
    }
}
