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
    private final SimilarityHandleMessage similarityHandleMessage;
    private final LibraryHandleMessage libraryHandleMessage;

    private final UserActivityService userActivityService;

    private final JudgeQueueProperties judgeQueueProperties;

    private final CodeTokenUtil codeTokenUtil;
    private final DataProblemMapper dataProblemMapper;


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
        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        // 核心数据库更新
        DataSubmit dataSubmit = dataSubmitMapper.selectById(judgeResultDto.getId());
        BeanUtil.copyProperties(judgeResultDto, dataSubmit);
        dataSubmit.setIsFinish(Boolean.TRUE); // 流转结束
        dataSubmitMapper.updateById(dataSubmit);

        // 正式提交
        if (judgeResultDto.getSubmitType()) {
            // ac情况
            if (JudgeStatus.ACCEPTED.getValue().equals(judgeResultDto.getStatus())) {
                log.info("正式提交 AC，进行额外处理");
                userActivityService.addActivity(judgeResultDto.getUserId(), ActivityScoreCalculator.SUBMIT, true);
                handleAdditionalTasks(judgeResultDto, dataSubmit);
            } else {
                // 非 ac 情况
                log.info("非 AC 提交，不进行额外处理");
            }
        } else {
            // 测试提交
            log.info("测试提交，不进行额外处理");
        }
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

        // 方法
        DataProblem dataProblem = dataProblemMapper.selectById(dataSubmit.getProblemId());
        TokenDetail codeTokensDetail = codeTokenUtil.getCodeTokensDetail(judgeResultDto.getLanguage().toLowerCase(), judgeResultDto.getCode());

        SimilarityMessage similarityMessage = new SimilarityMessage();
        similarityMessage.setTaskId(taskId);
        similarityMessage.setSubmitId(dataSubmit.getId());
        similarityMessage.setSetId(dataSubmit.getSetId());
        similarityMessage.setProblemId(dataSubmit.getProblemId());
        similarityMessage.setIsSet(judgeResultDto.getIsSet());
        similarityMessage.setLanguage(judgeResultDto.getLanguage());
        similarityMessage.setThreshold(dataProblem.getThreshold());
        similarityMessage.setTaskType(Boolean.FALSE);
        similarityMessage.setCode(judgeResultDto.getCode());
        similarityMessage.setCodeLength(judgeResultDto.getCode().length());
        similarityMessage.setMinMatchLength(5);
        similarityMessage.setCodeTokens(codeTokensDetail.getTokens());
        similarityMessage.setCodeTokenNames(codeTokensDetail.getTokenNames());
        similarityMessage.setCodeTokenTexts(codeTokensDetail.getTokenTexts());
        similarityMessage.setUserId(dataSubmit.getUserId());
        similarityHandleMessage.sendSimilarity(similarityMessage);
    }
}
