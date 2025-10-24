package io.charlie.web.oj.modular.data.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.entity.DataProblemCount;
import io.charlie.web.oj.modular.data.problem.param.*;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.problem.utils.ProblemBuildTool;
import io.charlie.web.oj.modular.data.problem.utils.ProblemImportTool;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.entity.ProblemOverallStats;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import io.charlie.web.oj.modular.task.judge.dto.TestCase;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题目表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProblemServiceImpl extends ServiceImpl<DataProblemMapper, DataProblem> implements DataProblemService {
    private final DataSolvedMapper dataSolvedMapper;
    private final DataProblemTagService dataProblemTagService;
    private final TransService transService;
    private final ProblemBuildTool problemBuildTool;
    private final ProblemImportTool problemImportTool;
    private final DataSolvedService dataSolvedService;

    private final UserCacheService userCacheService;
    private final ProblemSetCacheService problemSetCacheService;
    private final ProblemCacheService problemCacheService;

    @Override
    public Page<DataProblem> page(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 标签查询
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
            List<String> idsByTagId = dataProblemTagService.getProblemIdsByTagId(dataProblemPageParam.getTagId());
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(DataProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(DataProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(dataProblemPageParam.getSortField(), dataProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProblemPageParam.getSortField()));
        }

        Page<DataProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataProblemPageParam.getSize()).orElse(10),
                        null
                ),
                queryWrapper);
        problemBuildTool.buildProblems(page.getRecords());
        return page;
    }

    @Override
    public Page<DataProblem> pageClient(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(DataProblem::getIsVisible, true).eq(DataProblem::getIsPublic, true);

        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 标签查询
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
            List<String> idsByTagId = dataProblemTagService.getProblemIdsByTagId(dataProblemPageParam.getTagId());
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(DataProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(DataProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(dataProblemPageParam.getSortField(), dataProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProblemPageParam.getSortField()));
        }

        Page<DataProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataProblemPageParam.getSize()).orElse(10),
                        null
                ),
                queryWrapper);
        problemBuildTool.buildProblems(page.getRecords());
        return page;
    }

    @Override
    public Page<DataProblem> setPage(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        queryWrapper.lambda()
                .eq(DataProblem::getIsVisible, Boolean.TRUE);
        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 筛选上架的
        queryWrapper.lambda().eq(DataProblem::getIsVisible, true);
        // 标签查询
        if (GaStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
            List<String> idsByTagId = dataProblemTagService.getProblemIdsByTagId(dataProblemPageParam.getTagId());
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(DataProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(DataProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(dataProblemPageParam.getSortField(), dataProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProblemPageParam.getSortField()));
        }

        Page<DataProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataProblemPageParam.getSize()).orElse(10),
                        null
                ),
                queryWrapper);
        problemBuildTool.buildProblems(page.getRecords());
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataProblemAddParam dataProblemAddParam) {
        DataProblem bean = BeanUtil.toBean(dataProblemAddParam, DataProblem.class);
        this.save(bean);

        // 处理标签
        dataProblemTagService.addOrUpdate(bean.getId(), dataProblemAddParam.getTagIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataProblemEditParam dataProblemEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataProblem>().eq(DataProblem::getId, dataProblemEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataProblem bean = BeanUtil.toBean(dataProblemEditParam, DataProblem.class);
        BeanUtil.copyProperties(dataProblemEditParam, bean);
        this.updateById(bean);

        // 处理标签
        dataProblemTagService.addOrUpdate(bean.getId(), dataProblemEditParam.getTagIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataProblemIdParam> dataProblemIdParamList) {
        if (ObjectUtil.isEmpty(dataProblemIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataProblemIdParamList, DataProblemIdParam::getId));
    }

    @Override
    public DataProblem detail(DataProblemIdParam dataProblemIdParam) {
        DataProblem dataProblem = this.getById(dataProblemIdParam.getId());
        if (ObjectUtil.isEmpty(dataProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        problemBuildTool.buildProblem(dataProblem);
        return dataProblem;
    }

    @Override
    public List<DataProblem> latestN(int n) {
        List<DataProblem> list = this.list(new QueryWrapper<DataProblem>().checkSqlInjection()
                .lambda()
                .eq(DataProblem::getIsVisible, Boolean.TRUE)
                .eq(DataProblem::getIsPublic, Boolean.TRUE)
                .orderByDesc(DataProblem::getCreateTime)
                .last("LIMIT " + n));
        problemBuildTool.buildProblems(list);
        return list;
    }

    @Override
    public DataProblemCount getProblemCount() {
        DataProblemCount dataProblemCount = new DataProblemCount();
        long count = this.count(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, Boolean.TRUE)
                .eq(DataProblem::getIsPublic, Boolean.TRUE)
        );
        dataProblemCount.setTotal(count);

        // 获取当月第一天
        LocalDateTime firstDay = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        // 获取当月最后一天
        LocalDateTime lastDay = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(23, 59, 59);

        // 转换为 Date
        Date monthStart = Date.from(firstDay.atZone(ZoneId.systemDefault()).toInstant());
        Date monthEnd = Date.from(lastDay.atZone(ZoneId.systemDefault()).toInstant());

        long monthAdd = this.count(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, Boolean.TRUE)
                .eq(DataProblem::getIsPublic, Boolean.TRUE)
                .ge(DataProblem::getCreateTime, monthStart)
                .le(DataProblem::getCreateTime, monthEnd)
        );
        dataProblemCount.setMonthAdd(monthAdd);

        dataProblemCount.setLastAddTime(this.getOne(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, Boolean.TRUE)
                .eq(DataProblem::getIsPublic, Boolean.TRUE)
                .orderByDesc(DataProblem::getCreateTime)
                .last("LIMIT 1")
        ).getCreateTime());

        dataProblemCount.setGrowthRate(BigDecimal.valueOf(monthAdd / (double) count).setScale(4, RoundingMode.HALF_UP));

        try {
            String userId = StpUtil.getLoginIdAsString();
            Long userAcceptedCount = dataSolvedMapper.selectCount(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, userId)
                    .eq(DataSolved::getSolved, Boolean.TRUE)
                    .eq(DataSolved::getIsSet, Boolean.FALSE)
            );
//            Long userAcceptedCount = userCacheService.getUserAcceptedCount(userId);
            dataProblemCount.setSolved(userAcceptedCount);
        } catch (Exception e) {
            // 未登录
            dataProblemCount.setSolved(0L);
        }

        ProblemOverallStats problemOverallStats = dataSolvedService.getProblemOverallStats();
        dataProblemCount.setAvgPassRate(problemOverallStats.getAverageAcceptanceRate());

        return dataProblemCount;
    }

    @Override
    public List<DataProblem> getHotN(int n) {
//        List<RankItem> problemRankTopN = problemCacheService.getProblemParticipantRankTopN(n);
//        List<DataProblem> dataProblems = new ArrayList<>();
//        for (RankItem rankingInfo : problemRankTopN) {
//            DataProblem dataProblem = this.getById(rankingInfo.getId());
//            dataProblem.setRank(rankingInfo.getRank());
//            dataProblems.add(dataProblem);
//        }
        List<DataProblem> dataProblems = this.baseMapper.selectTopNBySubmitCount(n);
        problemBuildTool.buildProblems(dataProblems);
        transService.transBatch(dataProblems);
        return dataProblems;
    }

    @Override
    public List<DifficultyDistribution> difficultyDistribution() {
//        long totalCount = this.count();
//
//        // 统计难度分布-简单
//        long simpleCount = this.count(new LambdaQueryWrapper<DataProblem>().eq(DataProblem::getDifficulty, 1).eq(DataProblem::getIsVisible, true).eq(DataProblem::getIsPublic, true));
//        DifficultyDistribution simple = new DifficultyDistribution();
//        simple.setDifficulty(1);
//        simple.setCount(simpleCount);
//        simple.setDifficultyName("简单");
//        if (totalCount == 0) {
//            simple.setPercentage(BigDecimal.ZERO);
//        } else {
//            simple.setPercentage(new BigDecimal(simpleCount)
//                    .multiply(new BigDecimal(100))
//                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
//        }
//
//        // 统计难度分布-中等
//        long mediumCount = this.count(new LambdaQueryWrapper<DataProblem>().eq(DataProblem::getDifficulty, 2).eq(DataProblem::getIsVisible, true).eq(DataProblem::getIsPublic, true));
//        DifficultyDistribution medium = new DifficultyDistribution();
//        medium.setDifficulty(2);
//        medium.setCount(mediumCount);
//        medium.setDifficultyName("中等");
//        if (totalCount == 0) {
//            medium.setPercentage(BigDecimal.ZERO);
//        } else {
//            medium.setPercentage(new BigDecimal(mediumCount)
//                    .multiply(new BigDecimal(100))
//                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
//        }
//
//        // 统计难度分布-困难
//        long hardCount = this.count(new LambdaQueryWrapper<DataProblem>().eq(DataProblem::getDifficulty, 3).eq(DataProblem::getIsVisible, true).eq(DataProblem::getIsPublic, true));
//        DifficultyDistribution hard = new DifficultyDistribution();
//        hard.setDifficulty(3);
//        hard.setCount(hardCount);
//        hard.setDifficultyName("困难");
//        if (totalCount == 0) {
//            hard.setPercentage(BigDecimal.ZERO);
//        } else {
//            hard.setPercentage(new BigDecimal(hardCount)
//                    .multiply(new BigDecimal(100))
//                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
//        }
//
//        return List.of(simple, medium, hard);

        return this.baseMapper.selectDifficultyDistribution();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importProblems(MultipartFile file) {
        problemImportTool.importProblems(file);
    }

    @Override
    public String llmGetDescription(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        return dataProblem.getDescription();
    }

    @Override
    public String llmGetTestCase(String id) {
        DataProblem dataProblem = this.getById(id);

        if (dataProblem == null) {
            return "该题目不存在";
        }
        // 获得测试用例
        if (ObjectUtil.isEmpty(dataProblem.getTestCase())) {
            return "测试用例为空";
        }

        List<TestCase> testCases = dataProblem.getTestCase();
        // 随机获取一个测试用例
        TestCase testCase = testCases.get(new Random().nextInt(testCases.size()));
        return JSONUtil.toJsonPrettyStr(testCase);
    }

    @Override
    public String llmGetResourceConstraints(String id) {
        DataProblem dataProblem = this.getById(id);

        if (dataProblem == null) {
            return "该题目不存在";
        }

        class Constraints {
            String time;
            String memory;
        }
        Constraints constraints = new Constraints();
        constraints.time = dataProblem.getMaxTime() + "ms";
        constraints.memory = dataProblem.getMaxMemory() + "KB";
        return JSONUtil.toJsonPrettyStr(constraints);
    }

    @Override
    public String llmGetExample(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }

        if (ObjectUtil.isEmpty(dataProblem.getTestCase())) {
            return "无示例";
        }

        List<TestCase> testCases = dataProblem.getTestCase();
        // 获得第一个测试用例
        TestCase testCase = testCases.getFirst();
        return JSONUtil.toJsonPrettyStr(testCase);
    }

    @Override
    public String getDifficulty(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        Integer difficultyLevel = dataProblem.getDifficulty();
        return switch (difficultyLevel) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        };
    }

    @Override
    public String llmGetSource(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        return dataProblem.getSource();
    }

    @Override
    public String llmGetTags(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        List<String> tagNamesById = dataProblemTagService.getTagNamesById(dataProblem.getId());
        if (tagNamesById == null) {
            return "无标签";
        }
        return JSONUtil.toJsonPrettyStr(tagNamesById);
    }

    @Override
    public String llmGetCategory(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        transService.transOne(dataProblem);
        return dataProblem.getCategoryName();
    }

    @Override
    public String llmGetOpenLanguage(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        List<String> allowedLanguages = dataProblem.getAllowedLanguages();
        if (allowedLanguages == null) {
            return "未设置提交语言";
        }
        return JSONUtil.toJsonPrettyStr(allowedLanguages);
    }

}