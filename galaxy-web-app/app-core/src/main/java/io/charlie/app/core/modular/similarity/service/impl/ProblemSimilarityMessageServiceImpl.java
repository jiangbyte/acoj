package io.charlie.app.core.modular.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityMQConfig;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityResultMQConfig;
import io.charlie.app.core.modular.similarity.dto.SimilarityResult;
import io.charlie.app.core.modular.similarity.dto.SimilarityResultDto;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.service.ProblemSimilarityMessageService;
import io.charlie.app.core.modular.similarity.utils.CodeSimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 题目克隆检服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSimilarityMessageServiceImpl implements ProblemSimilarityMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final ProSubmitMapper proSubmitMapper;

    @Override
    public void sendSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));
        rabbitTemplate.convertAndSend(
                ProblemSimilarityMQConfig.EXCHANGE,
                ProblemSimilarityMQConfig.ROUTING_KEY,
                similaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = ProblemSimilarityMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {

        SimilarityResultDto bean = BeanUtil.toBean(similaritySubmitDto, SimilarityResultDto.class);

        // 获得样本集
        List<ProSubmit> proSubmits = proSubmitMapper.selectList(new LambdaQueryWrapper<ProSubmit>()
                .eq(ProSubmit::getProblemId, similaritySubmitDto.getProblemId()) // 这个ID的提交
                .eq(ProSubmit::getSubmitType, true) // 正式提交
                .eq(ProSubmit::getStatus, JudgeStatus.ACCEPTED) // 只有通过的才收入样本集
        );
        // 空直接忽略
        if (ObjectUtil.isEmpty(proSubmits)) {
            return;
        }

        List<CodeSimilarityCalculator.SimilarityDetail> similarityDetails = new ArrayList<>();

        AtomicReference<CodeSimilarityCalculator.SimilarityDetail> maxSimilarityDetail = new AtomicReference<>();

        proSubmits.forEach(proSubmit -> {
            // 非空计算
            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    proSubmit.getCode(),
                    similaritySubmitDto.getCode(),
                    10
            );

            similarityDetail.setOriginUser(proSubmit.getUserId());
            similarityDetail.setSubmitUser(similaritySubmitDto.getUserId());
            similarityDetails.add(similarityDetail);

            maxSimilarityDetail.updateAndGet(current ->
                    (current == null || similarityDetail.getSimilarity() > current.getSimilarity())
                            ? similarityDetail
                            : current
            );
        });

        SimilarityResult similarityResult = new SimilarityResult();
        similarityResult.setMaxSimilarity(maxSimilarityDetail.get());
        similarityResult.setSimilarityDetails(similarityDetails);

        bean.setResult(similarityResult);

        this.sendSimilarityResult(bean);
    }

    @Override
    public void sendSimilarityResult(SimilarityResultDto similarityResultDto) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(similarityResultDto));
        rabbitTemplate.convertAndSend(
                ProblemSimilarityResultMQConfig.EXCHANGE,
                ProblemSimilarityResultMQConfig.ROUTING_KEY,
                similarityResultDto
        );
    }

    @Transactional
    @RabbitListener(queues = ProblemSimilarityResultMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleSimilarityResult(SimilarityResultDto similarityResultDto) {
        log.info("handleSimilarityResult: {}", similarityResultDto);
        // TODO 处理结果
    }
}
