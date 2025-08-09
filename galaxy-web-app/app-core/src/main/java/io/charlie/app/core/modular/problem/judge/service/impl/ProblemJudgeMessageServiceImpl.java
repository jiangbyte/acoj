package io.charlie.app.core.modular.problem.judge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.app.core.modular.judge.config.ProblemJudgeMQConfig;
import io.charlie.app.core.modular.judge.config.ProblemResultMQConfig;
import io.charlie.app.core.modular.judge.dto.JudgeResultDto;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitDto;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitResultDto;
import io.charlie.app.core.modular.problem.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.problem.judge.service.ProblemJudgeMessageService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.problem.submit.service.ProSubmitService;
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
public class ProblemJudgeMessageServiceImpl implements ProblemJudgeMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final ProSubmitMapper proSubmitMapper;
    private final ProSolvedMapper proSolvedMapper;

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
    @RabbitListener(queues = ProblemResultMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleJudgeResult(JudgeResultDto judgeResultDto) {
        if (judgeResultDto.getIsSet()) {
            // 舍弃消息
            return;
        }

        System.out.println(JSONUtil.toJsonPrettyStr(judgeResultDto));

        log.info("接收到消息：{}", JSONUtil.toJsonStr(judgeResultDto));

        ProSubmit bean = BeanUtil.toBean(judgeResultDto, ProSubmit.class);
        bean.setUpdateTime(new Date());
        proSubmitMapper.updateById(bean);

        if (bean.getStatus().equals(JudgeStatus.ACCEPTED.getValue())) {
            proSolvedMapper.update(new LambdaUpdateWrapper<ProSolved>()
                    .eq(ProSolved::getUserId, bean.getUserId())
                    .eq(ProSolved::getProblemId, bean.getProblemId())
                    .eq(ProSolved::getSubmitId, bean.getId())
                    .set(ProSolved::getSolved, true)
            );
        } else if (bean.getStatus().equals(JudgeStatus.WRONG_ANSWER.getValue())) {
            proSolvedMapper.update(new LambdaUpdateWrapper<ProSolved>()
                    .eq(ProSolved::getUserId, bean.getUserId())
                    .eq(ProSolved::getProblemId, bean.getProblemId())
                    .eq(ProSolved::getSubmitId, bean.getId())
                    .set(ProSolved::getSolved, false)
            );
        }

        log.info("接收到消息 更新成功：{}", JSONUtil.toJsonStr(judgeResultDto));
    }

    @Transactional
//    @RabbitListener(queues = ProblemJudgeMQConfig.QUEUE, concurrency = "5-10")
    public void handleMessage(JudgeSubmitDto message) {
        log.info("接收到消息：{}", JSONUtil.toJsonStr(message));
    }
}
