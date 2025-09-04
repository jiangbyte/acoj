package io.charlie.app.core.modular.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.mapper.ProSetSubmitMapper;
import io.charlie.app.core.modular.similarity.config.SetSimilarityMQConfig;
import io.charlie.app.core.modular.similarity.config.SetSimilarityResultMQConfig;
import io.charlie.app.core.modular.similarity.dto.SimilarityResult;
import io.charlie.app.core.modular.similarity.dto.SimilarityResultDto;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.service.SetSimilarityMessageService;
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
public class SetSimilarityMessageServiceImpl implements SetSimilarityMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final CodeSimilarityCalculator codeSimilarityCalculator;
    private final ProSetSubmitMapper proSetSubmitMapper;

    @Override
    public void sendSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 发送消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));
        rabbitTemplate.convertAndSend(
                SetSimilarityMQConfig.EXCHANGE,
                SetSimilarityMQConfig.ROUTING_KEY,
                similaritySubmitDto
        );
    }

    @Transactional
    @RabbitListener(queues = SetSimilarityMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

        SimilarityResultDto bean = BeanUtil.toBean(similaritySubmitDto, SimilarityResultDto.class);

        // 配置参数
        final int MIN_SAMPLE_SIZE = 50; // 最低样本数量限制
        final int RECENT_SAMPLE_SIZE = 100; // 想要获取的近期样本数量

        LambdaQueryWrapper<ProSetSubmit> queryWrapper = new LambdaQueryWrapper<ProSetSubmit>()
                .eq(ProSetSubmit::getProblemId, similaritySubmitDto.getProblemId()) // 这个ID的提交
                .eq(ProSetSubmit::getSubmitType, true) // 正式提交
                .eq(ProSetSubmit::getStatus, JudgeStatus.ACCEPTED);// 只有通过的才收入样本集

        // 先查询总数量
        long totalCount = proSetSubmitMapper.selectCount(queryWrapper);

        List<ProSetSubmit> proSubmits;

        if (totalCount > MIN_SAMPLE_SIZE) {
            // 如果总数超过最低限制，优先获取最近的RECENT_SAMPLE_SIZE条记录
            queryWrapper.orderByDesc(ProSetSubmit::getCreateTime)
                    .last("LIMIT " + RECENT_SAMPLE_SIZE);
            proSubmits = proSetSubmitMapper.selectList(queryWrapper);

            // 如果获取的近期样本不足最低限制，补充旧样本
            if (proSubmits.size() < MIN_SAMPLE_SIZE) {
                queryWrapper.clear();
                queryWrapper.eq(ProSetSubmit::getProblemId, similaritySubmitDto.getProblemId())
                        .eq(ProSetSubmit::getSubmitType, true)
                        .eq(ProSetSubmit::getStatus, JudgeStatus.ACCEPTED)
                        .orderByAsc(ProSetSubmit::getCreateTime)
                        .last("LIMIT " + (MIN_SAMPLE_SIZE - proSubmits.size()));
                List<ProSetSubmit> additionalSubmits = proSetSubmitMapper.selectList(queryWrapper);
                proSubmits.addAll(additionalSubmits);
            }
        } else {
            // 如果总数不足最低限制，获取全部
            log.info("总数不足最低限制，获取全部数据构建检测库");
            proSubmits = proSetSubmitMapper.selectList(queryWrapper);
        }

        // 空直接忽略
        if (ObjectUtil.isEmpty(proSubmits)) {
            log.info("代码克隆检测 -> 忽略空数据");
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
                SetSimilarityResultMQConfig.EXCHANGE,
                SetSimilarityResultMQConfig.ROUTING_KEY,
                similarityResultDto
        );
    }

    @Transactional
    @RabbitListener(queues = SetSimilarityResultMQConfig.QUEUE, concurrency = "5-10")
    @Override
    public void handleSimilarityResult(SimilarityResultDto similarityResultDto) {
        log.info("handleSimilarityResult: {}", similarityResultDto);
        // TODO 处理结果
    }
}
