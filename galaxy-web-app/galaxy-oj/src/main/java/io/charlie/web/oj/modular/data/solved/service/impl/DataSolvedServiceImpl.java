package io.charlie.web.oj.modular.data.solved.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.entity.ProblemOverallStats;
import io.charlie.web.oj.modular.data.solved.entity.ProblemStatistics;
import io.charlie.web.oj.modular.data.solved.entity.SetStatistics;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedAddParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedEditParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedIdParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedPageParam;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 用户解决表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSolvedServiceImpl extends ServiceImpl<DataSolvedMapper, DataSolved> implements DataSolvedService {

    private final DataScopeUtil dataScopeUtil;

    @Override
    @DS("slave")
    public Page<DataSolved> page(DataSolvedPageParam dataSolvedPageParam) {
        QueryWrapper<DataSolved> queryWrapper = new QueryWrapper<DataSolved>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        // 使用 EXISTS 子查询的方式（性能更好）
        queryWrapper.exists("SELECT 1 FROM sys_user u WHERE u.id = user_id AND u.group_id IN (" +
                accessibleGroupIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")) +
                ")");

        if (ObjectUtil.isAllNotEmpty(dataSolvedPageParam.getSortField(), dataSolvedPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSolvedPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSolvedPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSolvedPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSolvedPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSolvedPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSolvedAddParam dataSolvedAddParam) {
        DataSolved bean = BeanUtil.toBean(dataSolvedAddParam, DataSolved.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSolvedEditParam dataSolvedEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSolved>().eq(DataSolved::getId, dataSolvedEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSolved bean = BeanUtil.toBean(dataSolvedEditParam, DataSolved.class);
        BeanUtil.copyProperties(dataSolvedEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSolvedIdParam> dataSolvedIdParamList) {
        if (ObjectUtil.isEmpty(dataSolvedIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataSolvedIdParamList, DataSolvedIdParam::getId));
    }

    @Override
    @DS("slave")
    public DataSolved detail(DataSolvedIdParam dataSolvedIdParam) {
        DataSolved dataSolved = this.getById(dataSolvedIdParam.getId());
        if (ObjectUtil.isEmpty(dataSolved)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataSolved;
    }

    /**
     * 获取系统整体通过率统计
     */
    @Override
    @DS("slave")
    public ProblemOverallStats getProblemOverallStats() {
        try {
            Map<String, Object> stats = this.baseMapper.selectAllProblemAcceptanceStats();

            ProblemOverallStats overallStats = new ProblemOverallStats();
//            overallStats.setTotalProblems((BigDecimal) stats.get("totalProblems"));
//            overallStats.setProblemsWithSubmissions((BigDecimal) stats.get("problemsWithSubmissions"));
//            overallStats.setTotalParticipants((BigDecimal) stats.get("totalParticipants"));
//            overallStats.setTotalAcceptedParticipants((BigDecimal) stats.get("totalAcceptedParticipants"));
//            overallStats.setAverageAcceptanceRate((BigDecimal) stats.get("averageAcceptanceRate"));
//            overallStats.setMinAcceptanceRate((BigDecimal) stats.get("minAcceptanceRate"));
//            overallStats.setMaxAcceptanceRate((BigDecimal) stats.get("maxAcceptanceRate"));

            // 根据实际返回类型进行转换
            overallStats.setTotalProblems(toBigDecimal(stats.get("totalProblems")));
            overallStats.setProblemsWithSubmissions(toBigDecimal(stats.get("problemsWithSubmissions")));
            overallStats.setTotalParticipants(toBigDecimal(stats.get("totalParticipants")));
            overallStats.setTotalAcceptedParticipants(toBigDecimal(stats.get("totalAcceptedParticipants")));
            overallStats.setAverageAcceptanceRate(toBigDecimal(stats.get("averageAcceptanceRate")));
            overallStats.setMinAcceptanceRate(toBigDecimal(stats.get("minAcceptanceRate")));
            overallStats.setMaxAcceptanceRate(toBigDecimal(stats.get("maxAcceptanceRate")));

//            // 计算有提交题目的比例
//            if (overallStats.getTotalProblems() > 0) {
//                BigDecimal submissionRatio = BigDecimal.valueOf(overallStats.getProblemsWithSubmissions())
//                        .divide(BigDecimal.valueOf(overallStats.getTotalProblems()), 4, RoundingMode.HALF_UP)
//                        .multiply(BigDecimal.valueOf(100))
//                        .setScale(2, RoundingMode.HALF_UP);
//                overallStats.setSubmissionRatio(submissionRatio);
//            } else {
//                overallStats.setSubmissionRatio(BigDecimal.ZERO);
//            }
            // 计算有提交题目的比例
//            if (overallStats.getTotalProblems() != null && overallStats.getTotalProblems().compareTo(BigDecimal.ZERO) > 0) {
//
//                BigDecimal submissionRatio = overallStats.getProblemsWithSubmissions()
//                        .divide(overallStats.getTotalProblems(), 4, RoundingMode.HALF_UP)
//                        .multiply(BigDecimal.valueOf(100))
//                        .setScale(2, RoundingMode.HALF_UP);
//                overallStats.setSubmissionRatio(submissionRatio);
//            } else {
//                overallStats.setSubmissionRatio(BigDecimal.ZERO);
//            }

            if (overallStats.getTotalProblems() != null &&
                    overallStats.getTotalProblems().compareTo(BigDecimal.ZERO) > 0 &&
                    overallStats.getProblemsWithSubmissions() != null) {

                BigDecimal submissionRatio = overallStats.getProblemsWithSubmissions()
                        .divide(overallStats.getTotalProblems(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                overallStats.setSubmissionRatio(submissionRatio);
            } else {
                overallStats.setSubmissionRatio(BigDecimal.ZERO);
            }

            return overallStats;
        } catch (Exception e) {
            log.error("获取题目整体统计失败", e);
            return new ProblemOverallStats(); // 返回空对象
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Long) return new BigDecimal((Long) value);
        if (value instanceof Integer) return new BigDecimal((Integer) value);
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        return BigDecimal.ZERO;
    }

    /**
     * 获取批量题目的通过率统计
     */
    @Override
    @DS("slave")
    public Map<String, ProblemStatistics> getBatchProblemStatistics(List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<Map<String, Object>> statsList = this.baseMapper.selectProblemAcceptanceStats(problemIds);
            Map<String, ProblemStatistics> result = new HashMap<>();

            for (Map<String, Object> stat : statsList) {
                String problemId = (String) stat.get("problemId");
                Long totalParticipants = (Long) stat.get("totalParticipants");
                Long acceptedParticipants = (Long) stat.get("acceptedParticipants");
                BigDecimal acceptanceRate = (BigDecimal) stat.get("acceptanceRate");

                result.put(problemId, new ProblemStatistics(
                        acceptanceRate,
                        totalParticipants.intValue(),
                        acceptedParticipants.intValue()
                ));
            }

            // 为没有统计数据的题目设置默认值
            for (String problemId : problemIds) {
                if (!result.containsKey(problemId)) {
                    result.put(problemId, new ProblemStatistics(BigDecimal.ZERO, 0, 0));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("获取批量题目统计失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    @DS("slave")
    public Map<String, ProblemStatistics> getBatchSetProblemStatistics(String setId, List<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<Map<String, Object>> statsList = this.baseMapper.selectSetProblemAcceptanceStats(setId, problemIds);
            Map<String, ProblemStatistics> result = new HashMap<>();

            for (Map<String, Object> stat : statsList) {
                String problemId = (String) stat.get("problemId");
                Long totalParticipants = (Long) stat.get("totalParticipants");
                Long acceptedParticipants = (Long) stat.get("acceptedParticipants");
                BigDecimal acceptanceRate = (BigDecimal) stat.get("acceptanceRate");

                result.put(problemId, new ProblemStatistics(
                        acceptanceRate,
                        totalParticipants.intValue(),
                        acceptedParticipants.intValue()
                ));
            }

            // 为没有统计数据的题目设置默认值
            for (String problemId : problemIds) {
                if (!result.containsKey(problemId)) {
                    result.put(problemId, new ProblemStatistics(BigDecimal.ZERO, 0, 0));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("获取批量题目统计失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    @DS("slave")
    public Map<String, SetStatistics> getBatchSetStatistics(List<String> setIds) {
        if (setIds == null || setIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<Map<String, Object>> statsList = this.baseMapper.selectSetAcceptanceStatsBatch(setIds);
            Map<String, SetStatistics> result = new HashMap<>();

            for (Map<String, Object> stat : statsList) {
                String setId = (String) stat.get("setId");
                Long totalParticipants = (Long) stat.get("totalParticipants");
                Long acceptedParticipants = (Long) stat.get("acceptedParticipants");
                BigDecimal acceptanceRate = (BigDecimal) stat.get("acceptanceRate");
                Long submitCount = (Long) stat.get("submitCount");

                result.put(setId, new SetStatistics(
                        acceptanceRate,
                        totalParticipants.intValue(),
                        acceptedParticipants.intValue(),
                        submitCount.intValue()
                ));
            }

            // 为没有统计数据的题目设置默认值
            for (String setId : setIds) {
                if (!result.containsKey(setId)) {
                    result.put(setId, new SetStatistics(BigDecimal.ZERO, 0, 0, 0));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("获取批量题目统计失败", e);
            return Collections.emptyMap();
        }
    }


}