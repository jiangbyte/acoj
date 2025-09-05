package io.charlie.app.core.modular.judge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.app.core.modular.judge.config.SetJudgeMQConfig;
import io.charlie.app.core.modular.judge.config.SetJudgeResultMQConfig;
import io.charlie.app.core.modular.judge.dto.JudgeResultDto;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.judge.service.SetJudgeMessageService;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.set.sample.service.ProSetSampleLibraryService;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.mapper.ProSetSubmitMapper;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.service.SetSimilarityMessageService;
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
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SetJudgeMessageServiceImpl implements SetJudgeMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final ProSetSubmitMapper proSetSubmitMapper;
    private final ProSetSolvedMapper proSetSolvedMapper;

    private final SetSimilarityMessageService setSimilarityMessageService;

    private final ProSetSampleLibraryService proSetSampleLibraryService;

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
                SetJudgeMQConfig.EXCHANGE,
                SetJudgeMQConfig.ROUTING_KEY,
                judgeSubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = SetJudgeResultMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleJudgeResult(JudgeResultDto judgeResultDto) {
        if (judgeResultDto.getIsSet()) {
            // 舍弃消息
            return;
        }

        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        ProSetSubmit bean = BeanUtil.toBean(judgeResultDto, ProSetSubmit.class);
        bean.setUpdateTime(new Date());
        bean.setUpdateUser(bean.getUserId());
        proSetSubmitMapper.updateById(bean);

        if (judgeResultDto.getSubmitType()) {
            if (bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
                // 正式执行才会更新已解决
                proSetSolvedMapper.update(new LambdaUpdateWrapper<ProSetSolved>()
                        .eq(ProSetSolved::getUserId, bean.getUserId())
                        .eq(ProSetSolved::getProblemId, bean.getProblemId())
                        .eq(ProSetSolved::getSubmitId, bean.getId())
                        .set(ProSetSolved::getSolved, true)
                );

                // 提交样本库
                // 先查询这次的提交
                ProSetSubmit proSetSubmit = proSetSubmitMapper.selectById(judgeResultDto.getId());
                // 增加到库
                proSetSampleLibraryService.addLibrary(proSetSubmit);

                // 正式执行才会计算相似度
                setSimilarityMessageService.sendSimilarityRequest(BeanUtil.toBean(judgeResultDto, SimilaritySubmitDto.class));
            }
            if (!bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
                proSetSolvedMapper.update(new LambdaUpdateWrapper<ProSetSolved>()
                        .eq(ProSetSolved::getUserId, bean.getUserId())
                        .eq(ProSetSolved::getProblemId, bean.getProblemId())
                        .eq(ProSetSolved::getSubmitId, bean.getId())
                        .set(ProSetSolved::getSolved, false)
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
