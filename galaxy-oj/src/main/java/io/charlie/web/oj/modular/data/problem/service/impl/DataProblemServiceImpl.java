package io.charlie.web.oj.modular.data.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.charlie.galaxy.utils.str.GalaxyStringUtil;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.entity.DataProblemCount;
import io.charlie.web.oj.modular.data.problem.entity.ProblemSourceLimit;
import io.charlie.web.oj.modular.data.problem.param.*;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.problem.utils.ProblemBuildTool;
import io.charlie.web.oj.modular.data.relation.set.entity.DataSetProblem;
import io.charlie.web.oj.modular.data.relation.set.mapper.DataSetProblemMapper;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;
import io.charlie.web.oj.modular.data.relation.tag.mapper.DataProblemTagMapper;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.entity.ProblemOverallStats;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import io.charlie.web.oj.modular.data.testcase.mapper.DataTestCaseMapper;
import io.charlie.web.oj.modular.task.judge.dto.TestCase;
import org.dromara.trans.service.impl.TransService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final DataSolvedService dataSolvedService;

    private final DataProblemTagMapper dataProblemTagMapper; // 标签
    private final DataTestCaseMapper dataTestCaseMapper; // 测试用例
    private final DataSubmitMapper dataSubmitMapper; // 提交记录
    private final DataSolvedMapper solvedMapper; // 提交记录
    private final DataSetProblemMapper dataSetProblemMapper; // 题集关系
    private final DataLibraryMapper dataLibraryMapper; // 检测代码库
    private final TaskReportsMapper taskReportsMapper; // 任务报告
    private final TaskSimilarityMapper taskSimilarityMapper; // 相似度详情

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "problem_source_limit:";
    private static final long CACHE_EXPIRE_HOURS = 24L;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    @DS("slave")
    public Page<DataProblem> page(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 标签查询
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
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
    @DS("slave") // 从库读取
    public Page<DataProblem> pageClient(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        queryWrapper.lambda()
                .eq(DataProblem::getIsVisible, Boolean.TRUE) // 仅显示可见的
                .eq(DataProblem::getIsPublic, Boolean.TRUE); // 公开性题目

        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 标签查询
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
            List<String> idsByTagId = dataProblemTagService.getProblemIdsByTagId(dataProblemPageParam.getTagId()); // 缓存读取
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(DataProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(DataProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(dataProblemPageParam.getSortField(), dataProblemPageParam.getSortOrder()) &&
                ISortOrderEnum.isValid(dataProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataProblemPageParam.getSortField()));
        }

        Page<DataProblem> page = this.page(CommonPageRequest.Page(
                Optional.ofNullable(dataProblemPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(dataProblemPageParam.getSize()).orElse(10),
                null
        ), queryWrapper);
        problemBuildTool.buildProblems(page.getRecords()); // 题目批量构建工具
        return page;
    }


    @Override
    @DS("slave")
    public Page<DataProblem> setPage(DataProblemPageParam dataProblemPageParam) {
        QueryWrapper<DataProblem> queryWrapper = new QueryWrapper<DataProblem>().checkSqlInjection();
        queryWrapper.lambda()
                .eq(DataProblem::getIsVisible, Boolean.TRUE);
        // 关键字
        if (ObjectUtil.isNotEmpty(dataProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataProblem::getTitle, dataProblemPageParam.getKeyword());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataProblem::getCategoryId, dataProblemPageParam.getCategoryId());
        }
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataProblem::getDifficulty, dataProblemPageParam.getDifficulty());
        }
        // 标签查询
        if (GalaxyStringUtil.isNotEmpty(dataProblemPageParam.getTagId())) {
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
        List<String> problemIds = CollStreamUtil.toList(dataProblemIdParamList, DataProblemIdParam::getId);
        this.removeByIds(problemIds);
        if (ObjectUtil.isNotEmpty(problemIds)) {
            // 移除对应的标签、提交记录、测试用例、解题记录
            // 移除标签
            dataProblemTagMapper.delete(new LambdaQueryWrapper<DataProblemTag>()
                    .in(DataProblemTag::getProblemId, problemIds)
            );
            // 移除测试用例
            dataTestCaseMapper.delete(new LambdaQueryWrapper<DataTestCase>()
                    .in(DataTestCase::getProblemId, problemIds)
            );
            // 移除提交记录
//            dataSubmitMapper.delete(new LambdaQueryWrapper<DataSubmit>()
//                    .in(DataSubmit::getProblemId, problemIds)
//            );
//            // 移除解题记录
//            solvedMapper.delete(new LambdaQueryWrapper<DataSolved>()
//                    .in(DataSolved::getProblemId, problemIds)
//            );
            // 移除数据集题目
            dataSetProblemMapper.delete(new LambdaQueryWrapper<DataSetProblem>()
                    .in(DataSetProblem::getProblemId, problemIds)
            );
//            // 移除代码库
//            dataLibraryMapper.delete(new LambdaQueryWrapper<DataLibrary>()
//                    .in(DataLibrary::getProblemId, problemIds)
//            );
//            // 移除任务报告
//            taskReportsMapper.delete(new LambdaQueryWrapper<TaskReports>()
//                    .in(TaskReports::getProblemId, problemIds)
//            );
//            // 移除任务相似度
//            taskSimilarityMapper.delete(new LambdaQueryWrapper<TaskSimilarity>()
//                    .in(TaskSimilarity::getProblemId, problemIds)
//            );
        }
    }

    @Override
    @DS("slave")
    public DataProblem detail(DataProblemIdParam dataProblemIdParam) {
        DataProblem dataProblem = this.getById(dataProblemIdParam.getId());
        if (ObjectUtil.isEmpty(dataProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        problemBuildTool.buildProblem(dataProblem);
        return dataProblem;
    }

    @Override
    @DS("slave")
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
    @DS("slave")
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
        LocalDateTime lastDay = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
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

        // 添加空值检查
        DataProblem lastProblem = this.getOne(new LambdaQueryWrapper<DataProblem>()
                .eq(DataProblem::getIsVisible, Boolean.TRUE)
                .eq(DataProblem::getIsPublic, Boolean.TRUE)
                .orderByDesc(DataProblem::getCreateTime)
                .last("LIMIT 1")
        );
        if (lastProblem != null) {
            dataProblemCount.setLastAddTime(lastProblem.getCreateTime());
        } else {
            dataProblemCount.setLastAddTime(null);
        }

        // 计算增长率，避免除零错误
        if (count > 0) {
            dataProblemCount.setGrowthRate(BigDecimal.valueOf(monthAdd / (double) count).setScale(4, RoundingMode.HALF_UP));
        } else {
            dataProblemCount.setGrowthRate(BigDecimal.ZERO);
        }

        try {
            String userId = StpUtil.getLoginIdAsString();
            Long userAcceptedCount = dataSolvedMapper.selectCount(new LambdaQueryWrapper<DataSolved>()
                    .eq(DataSolved::getUserId, userId)
                    .eq(DataSolved::getSolved, Boolean.TRUE)
                    .eq(DataSolved::getModuleType, "PROBLEM")
            );
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
    @DS("slave")
    public List<DataProblem> getHotN(int n) {
        List<DataProblem> dataProblems = this.baseMapper.selectTopNBySubmitCount(n);
        problemBuildTool.buildProblems(dataProblems);
        transService.transBatch(dataProblems);
        return dataProblems;
    }

    @Override
    @DS("slave")
    public List<DifficultyDistribution> difficultyDistribution() {
        return this.baseMapper.selectDifficultyDistribution();
    }

    @Override
    @DS("slave")
    public String llmGetDescription(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        return dataProblem.getDescription();
    }

    @Override
    @DS("slave")
    public String llmGetResourceConstraints(String id) {
        DataProblem dataProblem = this.getById(id);

        if (dataProblem == null) {
            return "该题目不存在";
        }

        return "{\n" +
                "  \"时间限制\": \"" + dataProblem.getMaxTime() + "ms\",\n" +
                "  \"内存限制\": \"" + dataProblem.getMaxMemory() + "KB\"\n" +
                "}";
    }

    @Override
    @DS("slave")
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
    @DS("slave")
    public String llmGetSource(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        return dataProblem.getSource();
    }

    @Override
    @DS("slave")
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
    @DS("slave")
    public String llmGetCategory(String id) {
        DataProblem dataProblem = this.getById(id);
        if (dataProblem == null) {
            return "该题目不存在";
        }
        transService.transOne(dataProblem);
        return dataProblem.getCategoryName();
    }

    @Override
    @DS("slave")
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