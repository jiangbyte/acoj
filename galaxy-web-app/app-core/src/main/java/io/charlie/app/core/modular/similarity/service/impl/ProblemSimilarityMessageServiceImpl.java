package io.charlie.app.core.modular.similarity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.charlie.app.core.modular.judge.enums.JudgeStatus;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.sample.entity.ProSampleLibrary;
import io.charlie.app.core.modular.problem.sample.mapper.ProSampleLibraryMapper;
import io.charlie.app.core.modular.problem.sample.service.ProSampleLibraryService;
import io.charlie.app.core.modular.problem.similarity.entity.ProSimilarityDetail;
import io.charlie.app.core.modular.problem.similarity.mapper.ProSimilarityDetailMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityMQConfig;
import io.charlie.app.core.modular.similarity.config.ProblemSimilarityResultMQConfig;
import io.charlie.app.core.modular.similarity.dto.SimilarityResult;
import io.charlie.app.core.modular.similarity.dto.SimilarityResultDto;
import io.charlie.app.core.modular.similarity.dto.SimilaritySubmitDto;
import io.charlie.app.core.modular.similarity.enums.CloneLevelEnum;
import io.charlie.app.core.modular.similarity.service.ProblemSimilarityMessageService;
import io.charlie.app.core.modular.similarity.utils.CodeSimilarityCalculator;
import io.charlie.app.core.modular.similarity.utils.DynamicCloneLevelDetector;
import io.charlie.app.core.modular.sys.config.entity.SysConfig;
import io.charlie.app.core.modular.sys.config.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
    private final SysConfigMapper sysConfigMapper;
    private final ProSampleLibraryMapper proSampleLibraryMapper;
    private final ProProblemMapper proProblemMapper;
    private final ProSimilarityDetailMapper proSimilarityDetailMapper;

    @Override
    public void sendSimilarityRequest(SimilaritySubmitDto similaritySubmitDto) {
        log.info("代码克隆检测 -> 发送消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));
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
        log.info("代码克隆检测 -> 接收检测消息：{}", JSONUtil.toJsonStr(similaritySubmitDto));

        SimilarityResultDto bean = BeanUtil.toBean(similaritySubmitDto, SimilarityResultDto.class);

        int MIN_SAMPLE_SIZE = 0;
        SysConfig sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_SAMPLE_SIZE_MIN"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            MIN_SAMPLE_SIZE = Integer.parseInt(sysConfig.getValue());
        }

        int RECENT_SAMPLE_SIZE = 0;
        sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_SAMPLE_SIZE_RECENT"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            RECENT_SAMPLE_SIZE = Integer.parseInt(sysConfig.getValue());
        }

        int MIN_MATCH_LENGTH;
        sysConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCode, "APP_DEFAULT_MATCH_LENGTH"));
        if (ObjectUtil.isNotEmpty(sysConfig)) {
            MIN_MATCH_LENGTH = Integer.parseInt(sysConfig.getValue());
        } else {
            MIN_MATCH_LENGTH = 0;
        }

        // 如果数量其中一个为0或者都为0，则忽略
        if (MIN_SAMPLE_SIZE <= 0 || RECENT_SAMPLE_SIZE <= 0 || MIN_MATCH_LENGTH <= 0) {
            log.info("代码克隆检测 -> 忽略空数据");
            ProSubmit proSubmit = proSubmitMapper.selectById(similaritySubmitDto.getId());
            if (proSubmit == null) {
                return;
            }
            proSubmit.setSimilarity(BigDecimal.ZERO);
            proSubmit.setSimilarityBehavior(CloneLevelEnum.NOT_DETECTED.getValue());
            proSubmitMapper.updateById(proSubmit);
            return;
        }

        // 抽取样本库（排除当前提交用户自身的）
        LambdaQueryWrapper<ProSampleLibrary> queryWrapper = new LambdaQueryWrapper<ProSampleLibrary>()
                .ne(ProSampleLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                .eq(ProSampleLibrary::getProblemId, similaritySubmitDto.getProblemId());

        // 先查询总数量
        long totalCount = proSampleLibraryMapper.selectCount(queryWrapper);

        List<ProSampleLibrary> proSampleLibraries;

        if (totalCount > MIN_SAMPLE_SIZE) {
            // 如果总数超过最低限制，优先获取最近的RECENT_SAMPLE_SIZE条记录
            queryWrapper.orderByDesc(ProSampleLibrary::getSubmitTime)
                    .last("LIMIT " + RECENT_SAMPLE_SIZE);
            proSampleLibraries = proSampleLibraryMapper.selectList(queryWrapper);

            // 如果获取的近期样本不足最低限制，补充旧样本
            if (proSampleLibraries.size() < MIN_SAMPLE_SIZE) {
                queryWrapper.clear();
                queryWrapper.ne(ProSampleLibrary::getUserId, similaritySubmitDto.getUserId()) // 排除提交用户自身的提交
                        .eq(ProSampleLibrary::getProblemId, similaritySubmitDto.getProblemId())
                        .orderByAsc(ProSampleLibrary::getSubmitTime)
                        .last("LIMIT " + (MIN_SAMPLE_SIZE - proSampleLibraries.size()));
                List<ProSampleLibrary> additionalSubmits = proSampleLibraryMapper.selectList(queryWrapper);
                proSampleLibraries.addAll(additionalSubmits);
            }
        } else {
            // 如果总数不足最低限制，获取全部
            log.info("总数不足最低限制，获取全部数据构建检测库");
            proSampleLibraries = proSampleLibraryMapper.selectList(queryWrapper);
        }

        // 空直接忽略
        if (ObjectUtil.isEmpty(proSampleLibraries)) {
            log.info("代码克隆检测 -> 忽略空数据");
            ProSubmit proSubmit = proSubmitMapper.selectById(similaritySubmitDto.getId());
            if (proSubmit == null) {
                return;
            }
            proSubmit.setSimilarity(BigDecimal.ZERO);
            proSubmit.setSimilarityBehavior(CloneLevelEnum.NOT_DETECTED.getValue());
            proSubmitMapper.updateById(proSubmit);
            return;
        }

        List<ProSimilarityDetail> proSimilarityDetails = new ArrayList<>();
        proSampleLibraries.forEach(proSampleLibrary -> {
            // 使用一次样本库，那么样本库的访问次数加1
            proSampleLibrary.setAccessCount(proSampleLibrary.getAccessCount() + 1);

            CodeSimilarityCalculator.SimilarityDetail similarityDetail = codeSimilarityCalculator.getSimilarityDetail(
                    similaritySubmitDto.getLanguage().toLowerCase(),
                    proSampleLibrary.getCode(),
                    similaritySubmitDto.getCode(),
                    MIN_MATCH_LENGTH
            );

            ProSimilarityDetail proSimilarityDetail = new ProSimilarityDetail();
            // 基础信息
            proSimilarityDetail.setTaskId(similaritySubmitDto.getTaskId());
            proSimilarityDetail.setTaskType(similaritySubmitDto.getTaskType());
            proSimilarityDetail.setProblemId(similaritySubmitDto.getProblemId());
            proSimilarityDetail.setLanguage(proSampleLibrary.getLanguage());
            proSimilarityDetail.setSimilarity(BigDecimal.valueOf(similarityDetail.getSimilarity()));
            // 提交用户
            proSimilarityDetail.setSubmitUser(similaritySubmitDto.getUserId());
            proSimilarityDetail.setSubmitCode(similaritySubmitDto.getCode());
            proSimilarityDetail.setSubmitCodeLength(similaritySubmitDto.getCode().length());
            proSimilarityDetail.setSubmitId(similaritySubmitDto.getId());
            proSimilarityDetail.setSubmitTime(similaritySubmitDto.getCreateTime());
            proSimilarityDetail.setSubmitTokenName(similarityDetail.getTokenNames1());
            proSimilarityDetail.setSubmitTokenTexts(similarityDetail.getTokenTexts1());

            // 样本信息
            proSimilarityDetail.setOriginUser(proSampleLibrary.getUserId());
            proSimilarityDetail.setOriginCode(proSampleLibrary.getCode());
            proSimilarityDetail.setOriginCodeLength(proSampleLibrary.getCodeLength());
            proSimilarityDetail.setOriginId(proSampleLibrary.getSubmitId());
            proSimilarityDetail.setOriginTime(proSampleLibrary.getCreateTime());
            proSimilarityDetail.setOriginTokenName(similarityDetail.getTokenNames2());
            proSimilarityDetail.setOriginTokenTexts(similarityDetail.getTokenTexts2());

            proSimilarityDetails.add(proSimilarityDetail);
        });
        proSampleLibraryMapper.updateById(proSampleLibraries); // 更新访问次数

        proSimilarityDetailMapper.insert(proSimilarityDetails);

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
        // 在任务中找到相似度最大的任务
        ProSimilarityDetail proSimilarityDetail = proSimilarityDetailMapper.selectOne(
                new QueryWrapper<ProSimilarityDetail>().lambda()
                        .eq(ProSimilarityDetail::getTaskId, similarityResultDto.getTaskId())
                        .orderByDesc(ProSimilarityDetail::getSimilarity)
                        .last("LIMIT 1")
        );
        if (proSimilarityDetail == null) {
            return;
        }
        ProProblem proProblem = proProblemMapper.selectById(similarityResultDto.getProblemId());
        if (proProblem == null) {
            return;
        }
        ProSubmit proSubmit = proSubmitMapper.selectById(similarityResultDto.getId());
        if (proSubmit == null) {
            return;
        }
        // 获取阈值
        BigDecimal threshold = proProblem.getThreshold();
        // 比较阈值和相似度,划分等级
        DynamicCloneLevelDetector dynamicCloneLevelDetector = new DynamicCloneLevelDetector(threshold);
        BigDecimal similarity = proSimilarityDetail.getSimilarity();
        proSubmit.setSimilarity(similarity);
        CloneLevelEnum detect = dynamicCloneLevelDetector.detect(similarity);
        proSubmit.setSimilarityBehavior(detect.getValue());
        proSubmitMapper.updateById(proSubmit);
        log.info("更新成功：{}", JSONUtil.toJsonStr(similarityResultDto));
    }
}
