package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.oj.modular.data.judgecase.mapper.DataJudgeCaseMapper;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;
import io.charlie.web.oj.modular.task.judge.dto.JudgeSubmitDto;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import io.charlie.web.oj.modular.task.judge.mq.JudgeQueueProperties;
import io.charlie.web.oj.modular.task.library.handle.LibraryHandleMessage;
import io.charlie.web.oj.modular.task.similarity.dto.SimilaritySubmitDto;
import io.charlie.web.oj.modular.task.similarity.handle.SingleSimilarityHandleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final SingleSimilarityHandleMessage similarityHandleMessage;
    private final LibraryHandleMessage libraryHandleMessage;

    private final UserActivityService userActivityService;

    private final JudgeQueueProperties judgeQueueProperties;


    public void sendJudge(JudgeSubmitDto judgeSubmitDto, DataSubmit dataSubmit) {
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
        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        // 核心数据库更新
        DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
        BeanUtil.copyProperties(judgeResultDto, dataSubmit);
        dataSubmitMapper.updateById(dataSubmit);

//        if (ObjectUtil.isNotEmpty(dataSubmit.getTestCase())) {
//            List<DataJudgeCase> dataJudgeCases = dataSubmit.getTestCase().stream().map(testCase -> {
//                DataJudgeCase dataJudgeCase = new DataJudgeCase();
//                dataJudgeCase.setSubmitId(judgeResultDto.getId());
//                dataJudgeCase.setInputData(testCase.getInput());
//                dataJudgeCase.setOutputData(testCase.getOutput());
//                dataJudgeCase.setExpectedOutput(testCase.getExcept());
//                dataJudgeCase.setMaxTime(testCase.getMaxTime());
//                dataJudgeCase.setMaxMemory(testCase.getMaxMemory());
//                dataJudgeCase.setStatus(testCase.getStatus());
//                dataJudgeCase.setMessage(testCase.getMessage());
//                dataJudgeCase.setExitCode(testCase.getExitCode());
//                dataJudgeCase.setScore(BigDecimal.ZERO);
//                return dataJudgeCase;
//            }).toList();
//            dataJudgeCaseMapper.insert(dataJudgeCases);
//        }

        // 正式提交
        if (judgeResultDto.getSubmitType()) {
            // ac情况
            if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
                log.info("正式提交 AC，进行额外处理");
                userActivityService.addActivity(judgeResultDto.getUserId(), ActivityScoreCalculator.SUBMIT, true);
                stopStatus(judgeResultDto.getId()); // 流转结束
                handleAdditionalTasks(judgeResultDto, dataSubmit);
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
    public void handleAdditionalTasks(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        try {
            log.info("开始处理正式提交的附加任务");
            updateSolved(judgeResultDto);

            // library 保存
            BeanUtil.copyProperties(judgeResultDto, dataSubmit);
            libraryHandleMessage.sendLibrary(dataSubmit);

            // similarity 异步任务
            similarityTask(judgeResultDto, dataSubmit);
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

    public void similarityTask(JudgeResultDto judgeResultDto, DataSubmit dataSubmit) {
        // 相似度检测
        String taskId = IdUtil.objectId();
        dataSubmitMapper.update(new LambdaUpdateWrapper<DataSubmit>().eq(DataSubmit::getId, judgeResultDto.getId()).set(DataSubmit::getTaskId, taskId));

        SimilaritySubmitDto similaritySubmitDto = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
        similaritySubmitDto.setTaskId(taskId);
        similaritySubmitDto.setTaskType(Boolean.FALSE);
        similaritySubmitDto.setJudgeTaskId(judgeResultDto.getJudgeTaskId());
        similaritySubmitDto.setCreateTime(dataSubmit.getCreateTime());
        similarityHandleMessage.sendSimilarity(similaritySubmitDto);
    }
}
