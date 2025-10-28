package io.charlie.web.oj.modular.data.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.param.DifficultyDistribution;
import io.charlie.web.oj.modular.data.problem.utils.ProblemBuildTool;
import io.charlie.web.oj.modular.data.relation.set.entity.DataSetProblem;
import io.charlie.web.oj.modular.data.relation.set.mapper.DataSetProblemMapper;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import io.charlie.web.oj.modular.data.relation.tag.mapper.DataProblemTagMapper;
import io.charlie.web.oj.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.param.*;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.data.set.service.DataSetService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.set.utils.SetBuildTool;
import io.charlie.web.oj.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.data.testcase.mapper.DataTestCaseMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题集 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetServiceImpl extends ServiceImpl<DataSetMapper, DataSet> implements DataSetService {
    private final DataSetProblemService dataSetProblemService;
    private final TransService transService;
    private final DataProblemMapper dataProblemMapper;
    private final ProblemBuildTool problemBuildTool;
    private final SysUserMapper sysUserMapper;

    private final SetBuildTool setBuildTool;

    private final DataSubmitMapper dataSubmitMapper;

    private final DataProblemTagMapper dataProblemTagMapper; // 标签
    private final DataTestCaseMapper dataTestCaseMapper; // 测试用例
    private final DataSolvedMapper solvedMapper; // 提交记录
    private final DataSetProblemMapper dataSetProblemMapper; // 题集关系
    private final DataLibraryMapper dataLibraryMapper; // 检测代码库
    private final TaskReportsMapper taskReportsMapper; // 任务报告
    private final TaskSimilarityMapper taskSimilarityMapper; // 相似度详情

    @Override
    public Page<DataSet> page(DataSetPageParam dataSetPageParam) {
        QueryWrapper<DataSet> queryWrapper = new QueryWrapper<DataSet>().checkSqlInjection();
        // 关键字
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataSet::getTitle, dataSetPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataSet::getCategoryId, dataSetPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataSet::getDifficulty, dataSetPageParam.getDifficulty());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getSetType())) {
            queryWrapper.lambda().eq(DataSet::getSetType, dataSetPageParam.getSetType());
        }
        if (ObjectUtil.isAllNotEmpty(dataSetPageParam.getSortField(), dataSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSetPageParam.getSortField()));
        }

        Page<DataSet> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        setBuildTool.buildSets(page.getRecords());
        return page;
    }

    @Override
    public Page<DataSet> pageClient(DataSetPageParam dataSetPageParam) {
        QueryWrapper<DataSet> queryWrapper = new QueryWrapper<DataSet>().checkSqlInjection();
        queryWrapper.lambda().eq(DataSet::getIsVisible, Boolean.TRUE);

        // 关键字
        if (ObjectUtil.isNotEmpty(dataSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(DataSet::getTitle, dataSetPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(DataSet::getCategoryId, dataSetPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(DataSet::getDifficulty, dataSetPageParam.getDifficulty());
        }
        if (GaStringUtil.isNotEmpty(dataSetPageParam.getSetType())) {
            queryWrapper.lambda().eq(DataSet::getSetType, dataSetPageParam.getSetType());
        }
        if (ObjectUtil.isAllNotEmpty(dataSetPageParam.getSortField(), dataSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSetPageParam.getSortField()));
        }

        Page<DataSet> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        setBuildTool.buildSets(page.getRecords());
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSetAddParam dataSetAddParam) {
        DataSet bean = BeanUtil.toBean(dataSetAddParam, DataSet.class);
        this.save(bean);

        dataSetProblemService.addOrUpdate(bean.getId(), dataSetAddParam.getProblemIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSetEditParam dataSetEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataSet>().eq(DataSet::getId, dataSetEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataSet bean = BeanUtil.toBean(dataSetEditParam, DataSet.class);
        BeanUtil.copyProperties(dataSetEditParam, bean);
        this.updateById(bean);

        dataSetProblemService.addOrUpdate(bean.getId(), dataSetEditParam.getProblemIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataSetIdParam> dataSetIdParamList) {
        if (ObjectUtil.isEmpty(dataSetIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<String> dataSetIds = CollStreamUtil.toList(dataSetIdParamList, DataSetIdParam::getId);
        this.removeByIds(dataSetIds);

//        if (ObjectUtil.isNotEmpty(dataSetIds)) {
//            // 移除提交记录
//            dataSubmitMapper.delete(new LambdaQueryWrapper<DataSubmit>()
//                    .in(DataSubmit::getSetId, dataSetIds)
//            );
//        }

        // 移除关联题目
        if (ObjectUtil.isNotEmpty(dataSetIds)) {
            dataSetProblemMapper.delete(new LambdaQueryWrapper<DataSetProblem>()
                    .in(DataSetProblem::getSetId, dataSetIds)
            );
        }
    }

    @Override
    public DataSet detail(DataSetIdParam dataSetIdParam) {
        DataSet dataSet = this.getById(dataSetIdParam.getId());
        if (ObjectUtil.isEmpty(dataSet)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        setBuildTool.buildSet(dataSet);
        return dataSet;
    }

    @Override
    public List<DataSet> latestN(int n) {
        List<DataSet> list = this.list(new QueryWrapper<DataSet>().checkSqlInjection()
                .lambda()
                .eq(DataSet::getIsVisible, true)
                .orderByAsc(DataSet::getCreateTime)
                .last("LIMIT " + n)
        );
        setBuildTool.buildSets(list);
        return list;
    }

    @Override
    public List<DataProblem> getSetProblem(DataSetProblemParam dataSetProblemParam) {
        List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemParam.getId());
        if (ObjectUtil.isEmpty(problemIdsBySetId)) {
            return List.of();
        }
        List<DataProblem> dataProblems = dataProblemMapper.selectByIds(problemIdsBySetId);
        transService.transBatch(dataProblems);
        problemBuildTool.buildSetProblems(dataSetProblemParam.getId(), dataProblems);
        return dataProblems;
    }

    @Override
    public List<DataProblem> getSetProblemWithSearch(DataSetProblemSearchParam dataSetProblemParam) {
        List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemParam.getId());
        if (ObjectUtil.isEmpty(problemIdsBySetId)) {
            return List.of();
        }

        List<DataProblem> dataProblems = dataProblemMapper.selectList(new LambdaQueryWrapper<DataProblem>()
                .in(DataProblem::getId, problemIdsBySetId)
                .like(DataProblem::getTitle, dataSetProblemParam.getKeyword())
        );
//        transService.transBatch(dataProblems);
//        problemBuildTool.buildSetProblems(dataSetProblemParam.getId(), dataProblems);
        return dataProblems;
    }

    @Override
    public DataProblem getSetProblemDetail(DataSetProblemDetailParam dataSetProblemDetailParam) {
        List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemDetailParam.getId());
        if (ObjectUtil.isEmpty(problemIdsBySetId)) {
            throw new BusinessException("该题集没有题目");
        }

        // 判断problemIdsBySetId 中是否存在dataSetProblemDetailParam.getProblemId
        if (!problemIdsBySetId.contains(dataSetProblemDetailParam.getProblemId())) {
            throw new BusinessException("该题集没有该题目");
        }

        DataSet byId = this.getById(dataSetProblemDetailParam.getId());

        DataProblem dataProblem = dataProblemMapper.selectById(dataSetProblemDetailParam.getProblemId());
        transService.transOne(dataProblem);
        problemBuildTool.buildSetProblem(dataSetProblemDetailParam.getId(), dataProblem);
        dataProblem.setSetUseAi(byId.getUseAi());
        return dataProblem;
    }

    @Override
    public List<DataSet> getHotN(int n) {
        List<DataSet> dataSets = this.baseMapper.selectTopNBySubmitCount(10);
        setBuildTool.buildSets(dataSets);
        transService.transBatch(dataSets);
        return dataSets;
    }

    @Override
    public Page<SysUser> getSetUser(DataSetUserParam dataSetUserParam) {
        List<DataSubmit> dataSubmits = dataSubmitMapper.selectList(new LambdaQueryWrapper<DataSubmit>()
                .eq(DataSubmit::getSetId, dataSetUserParam.getSetId())
                .eq(DataSubmit::getIsSet, true)
        );
        if (ObjectUtil.isEmpty(dataSubmits)) {
            return Page.of(dataSetUserParam.getCurrent(), dataSetUserParam.getSize());
        }

        List<String> stringList = dataSubmits.stream().map(DataSubmit::getUserId).distinct().toList();

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();
        queryWrapper.lambda().in(SysUser::getId, stringList);
        if (ObjectUtil.isAllNotEmpty(dataSetUserParam.getSortField(), dataSetUserParam.getSortOrder()) && ISortOrderEnum.isValid(dataSetUserParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataSetUserParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataSetUserParam.getSortField()));
        }

        Page<SysUser> sysUserPage = sysUserMapper.selectPage(CommonPageRequest.Page(
                        Optional.ofNullable(dataSetUserParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataSetUserParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        sysUserPage.getRecords().forEach(sysUser -> {
            sysUser.setPassword(null);
            sysUser.setTelephone(null);
        });
        return sysUserPage;
    }

    @Override
    public List<LabelOption<String>> getSetProblemLanguages(DataSetProblemLanguageParam dataSetProblemLanguageParam) {
        if (ObjectUtil.isEmpty(dataSetProblemLanguageParam.getProblemIds())) {
            List<String> problemIdsBySetId = dataSetProblemService.getProblemIdsBySetId(dataSetProblemLanguageParam.getId());
            if (ObjectUtil.isEmpty(problemIdsBySetId)) {
                return List.of();
            }

            List<DataProblem> dataProblems = dataProblemMapper.selectList(new LambdaQueryWrapper<DataProblem>()
                    .in(DataProblem::getId, problemIdsBySetId)
            );

            // 获取这些题目中都有的语言（交集）
            List<String> commonLanguages = dataProblems.stream()
                    .map(DataProblem::getAllowedLanguages)
                    .filter(Objects::nonNull) // 过滤掉null值
                    .map(HashSet::new) // 转换为Set便于求交集
                    .reduce((set1, set2) -> {
                        set1.retainAll(set2); // 求交集
                        return set1;
                    })
                    .map(HashSet::stream)
                    .orElse(Stream.empty())
                    .toList();

            return commonLanguages.stream()
                    .map(language -> new LabelOption<>(language, language))
                    .toList();

        }

        List<DataProblem> dataProblems = dataProblemMapper.selectList(new LambdaQueryWrapper<DataProblem>()
                .in(DataProblem::getId, dataSetProblemLanguageParam.getProblemIds()));

        // 获取这些题目中都有的语言（交集）
        List<String> commonLanguages = dataProblems.stream()
                .map(DataProblem::getAllowedLanguages)
                .filter(Objects::nonNull) // 过滤掉null值
                .map(HashSet::new) // 转换为Set便于求交集
                .reduce((set1, set2) -> {
                    set1.retainAll(set2); // 求交集
                    return set1;
                })
                .map(HashSet::stream)
                .orElse(Stream.empty())
                .toList();

        return commonLanguages.stream()
                .map(language -> new LabelOption<>(language, language))
                .toList();
    }

    @Override
    public List<DifficultyDistribution> difficultyDistribution() {
        long totalCount = this.count();

        // 统计难度分布-简单
        long simpleCount = this.count(new LambdaQueryWrapper<DataSet>().eq(DataSet::getDifficulty, 1).eq(DataSet::getIsVisible, true));
        DifficultyDistribution simple = new DifficultyDistribution();
        simple.setDifficulty(1);
        simple.setCount(simpleCount);
        simple.setDifficultyName("简单");
        if (totalCount == 0) {
            simple.setPercentage(BigDecimal.ZERO);
        } else {
            simple.setPercentage(new BigDecimal(simpleCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
        }

        // 统计难度分布-中等
        long mediumCount = this.count(new LambdaQueryWrapper<DataSet>().eq(DataSet::getDifficulty, 2).eq(DataSet::getIsVisible, true));
        DifficultyDistribution medium = new DifficultyDistribution();
        medium.setDifficulty(2);
        medium.setCount(mediumCount);
        medium.setDifficultyName("中等");
        if (totalCount == 0) {
            medium.setPercentage(BigDecimal.ZERO);
        } else {
            medium.setPercentage(new BigDecimal(mediumCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
        }

        // 统计难度分布-困难
        long hardCount = this.count(new LambdaQueryWrapper<DataSet>().eq(DataSet::getDifficulty, 3).eq(DataSet::getIsVisible, true));
        DifficultyDistribution hard = new DifficultyDistribution();
        hard.setDifficulty(3);
        hard.setCount(hardCount);
        hard.setDifficultyName("困难");
        if (totalCount == 0) {
            hard.setPercentage(BigDecimal.ZERO);
        } else {
            hard.setPercentage(new BigDecimal(hardCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalCount), 2, RoundingMode.DOWN));
        }

        return List.of(simple, medium, hard);
    }

}