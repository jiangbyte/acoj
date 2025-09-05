package io.charlie.app.core.modular.judge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.app.core.modular.judge.config.ProblemJudgeMQConfig;
import io.charlie.app.core.modular.judge.config.ProblemJudgeResultMQConfig;
import io.charlie.app.core.modular.judge.dto.JudgeResultDto;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.judge.service.ProblemJudgeMessageService;
import io.charlie.app.core.modular.problem.sample.entity.ProSampleLibrary;
import io.charlie.app.core.modular.problem.sample.service.ProSampleLibraryService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.service.ProblemSimilarityMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/08/2025
 * @description 提交判题消息处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemJudgeMessageServiceImpl implements ProblemJudgeMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final ProSubmitMapper proSubmitMapper;
    private final ProSolvedMapper proSolvedMapper;

    private final ProSampleLibraryService problemSampleLibraryService;

    private final ProblemSimilarityMessageService problemSimilarityMessageService;

    @Override
    public void sendJudgeRequest(JudgeSubmitDto judgeSubmitDto) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(judgeSubmitDto));
        // 发送消息
//            amqpTemplate.convertAndSend(
//                    PReqMQConfig.EXCHANGE,
//                    PReqMQConfig.ROUTING_KEY,
//                    message,
//                    msg -> {
//                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                        return msg;
//                    }
//            );
        rabbitTemplate.convertAndSend(
                ProblemJudgeMQConfig.EXCHANGE,
                ProblemJudgeMQConfig.ROUTING_KEY,
                judgeSubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = ProblemJudgeResultMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleJudgeResult(JudgeResultDto judgeResultDto) {
        if (judgeResultDto.getIsSet()) {
            // 舍弃消息
            return;
        }

        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        ProSubmit bean = BeanUtil.toBean(judgeResultDto, ProSubmit.class);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(bean.getUserId());
        proSubmitMapper.updateById(bean);

        if (judgeResultDto.getSubmitType()) {
            if (bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
                // 正式执行才会更新已解决
                proSolvedMapper.update(new LambdaUpdateWrapper<ProSolved>()
                        .eq(ProSolved::getUserId, bean.getUserId())
                        .eq(ProSolved::getProblemId, bean.getProblemId())
                        .eq(ProSolved::getSubmitId, bean.getId())
                        .set(ProSolved::getSolved, true)
                );

                // 提交样本库
                // 先查询这次的提交
                ProSubmit proSubmit = proSubmitMapper.selectById(judgeResultDto.getId());
                // 增加到库
                problemSampleLibraryService.addLibrary(proSubmit);

                String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
                proSubmit.setTaskId(snowflakeNextIdStr);
                proSubmitMapper.updateById(proSubmit);

                SimilaritySubmitDto bean1 = BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class);
                bean1.setTaskId(snowflakeNextIdStr);
                bean1.setTaskType(false);
                bean1.setCreateTime(proSubmit.getCreateTime());
                // 正式执行才会计算相似度
                problemSimilarityMessageService.sendSimilarityRequest(bean1);
            }
            if (!bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
                proSolvedMapper.update(new LambdaUpdateWrapper<ProSolved>()
                        .eq(ProSolved::getUserId, bean.getUserId())
                        .eq(ProSolved::getProblemId, bean.getProblemId())
                        .eq(ProSolved::getSubmitId, bean.getId())
                        .set(ProSolved::getSolved, false)
                );
            }
        }

        log.info("接收到消息 更新成功：{}", JSONUtil.toJsonStr(judgeResultDto));
    }

    @Transactional
//    @RabbitListener(queues = ProblemJudgeMQConfig.QUEUE, concurrency = "5-10")
    public void handleMessage(JudgeSubmitDto message) {
        log.info("接收到消息：{}", JSONUtil.toJsonStr(message));
    }
}
