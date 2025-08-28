package io.charlie.app.core.modular.problem.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.entity.TestCase;
import io.charlie.app.core.modular.problem.problem.param.*;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.problem.service.ProProblemService;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.problems.mapper.ProSetProblemMapper;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.sys.tag.entity.SysTag;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.galaxy.utils.GaStringUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题目表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProProblemServiceImpl extends ServiceImpl<ProProblemMapper, ProProblem> implements ProProblemService {
    private final ProProblemTagService proProblemTagService;
    private final ProSolvedMapper proSolvedMapper;
    private final ProSubmitMapper proSubmitMapper;

    private final ProSetSolvedMapper proSetSolvedMapper;
    private final ProSetProblemMapper proSetProblemMapper;

    @Override
    public Page<ProProblem> page(ProProblemPageParam proProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblem::getIsPublic, true);
        // 关键字
        if (ObjectUtil.isNotEmpty(proProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, proProblemPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(proProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(ProProblem::getCategoryId, proProblemPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(proProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(ProProblem::getDifficulty, proProblemPageParam.getDifficulty());
        }

        // 标签查询
        if (GaStringUtil.isNotEmpty(proProblemPageParam.getTagId())) {
            List<String> idsByTagId = proProblemTagService.getIdsByTagId(proProblemPageParam.getTagId());
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(ProProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(ProProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(proProblemPageParam.getSortField(), proProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(proProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proProblemPageParam.getSortField()));
        }

        Page<ProProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proProblemPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(item -> {
            List<SysTag> tagsById = proProblemTagService.getTagsById(item.getId());
            if (ObjectUtil.isNotEmpty(tagsById)) {
                item.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
                item.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
            }

            // 解决记录
            try {
                String loginIdAsString = StpUtil.getLoginIdAsString();
                // 缓存取出解决记录
                ProSolved proSolved = proSolvedMapper.selectOne(new QueryWrapper<ProSolved>().lambda()
                        .eq(ProSolved::getUserId, loginIdAsString)
                        .eq(ProSolved::getProblemId, item.getId()));
                if (proSolved.getSolved()) {
                    item.setCurrentUserSolved(true);
                } else {
                    item.setCurrentUserSolved(false);
                }
            } catch (Exception ignored) {
                item.setCurrentUserSolved(false);
            }

            // 通过率计算（缓存读取）
            Long proSolvedCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()).eq(ProSolved::getSolved, true));
            Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()));
            if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
                item.setAcceptance(BigDecimal.ZERO);
            } else {
                item.setAcceptance(new BigDecimal(proSolvedCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
            }
            // 参与人数
            item.setParticipantCount(String.valueOf(proSolvedTotalCount));
        });

        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProProblemAddParam proProblemAddParam) {
        ProProblem bean = BeanUtil.toBean(proProblemAddParam, ProProblem.class);
        this.save(bean);
        // 新增关联
        if (ObjectUtil.isNotEmpty(proProblemAddParam.getTagIds())) {
            proProblemTagService.updateProblemTags(bean.getId(), proProblemAddParam.getTagIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProProblemEditParam proProblemEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getId, proProblemEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProProblem bean = BeanUtil.toBean(proProblemEditParam, ProProblem.class);
        BeanUtil.copyProperties(proProblemEditParam, bean);
        this.updateById(bean);
        // 更新关联
        proProblemTagService.updateProblemTags(bean.getId(), proProblemEditParam.getTagIds());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProProblemIdParam> proProblemIdParamList) {
        if (ObjectUtil.isEmpty(proProblemIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        // 移除这里面的标签关联
        proProblemIdParamList.forEach(item -> proProblemTagService.updateProblemTags(item.getId(), null));
        this.removeByIds(CollStreamUtil.toList(proProblemIdParamList, ProProblemIdParam::getId));
    }

    @Override
    public ProProblem detail(ProProblemIdParam proProblemIdParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblem::getIsPublic, true);
        queryWrapper.lambda().eq(ProProblem::getId, proProblemIdParam.getId());
        ProProblem proProblem = this.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(proProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<SysTag> tagsById = proProblemTagService.getTagsById(proProblemIdParam.getId());
        if (ObjectUtil.isNotEmpty(tagsById)) {
            proProblem.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
            proProblem.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
        }
        // 通过率计算（缓存读取）
        Long proSolvedCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, proProblem.getId()).eq(ProSolved::getSolved, true));
        Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, proProblem.getId()));
        if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
            proProblem.setAcceptance(BigDecimal.ZERO);
        } else {
            proProblem.setAcceptance(new BigDecimal(proSolvedCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
        }
        return proProblem;
    }

    @Override
    public ProProblem appDetail(ProProblemIdParam proProblemIdParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblem::getIsPublic, true);
        queryWrapper.lambda().eq(ProProblem::getId, proProblemIdParam.getId());
        ProProblem proProblem = this.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(proProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        proProblem.setTestCase(List.of());
        if (ObjectUtil.isNotEmpty(proProblem.getCodeTemplate())) {
            proProblem.getCodeTemplate().forEach(template -> {
                template.setPrefix(null);
                template.setSuffix(null);
            });
        }
        List<SysTag> tagsById = proProblemTagService.getTagsById(proProblemIdParam.getId());
        if (ObjectUtil.isNotEmpty(tagsById)) {
            proProblem.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
            proProblem.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
        }

        // 通过率计算（缓存读取）
        Long proSolvedCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, proProblem.getId()).eq(ProSolved::getSolved, true));
        Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, proProblem.getId()));
        if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
            proProblem.setAcceptance(BigDecimal.ZERO);
        } else {
            proProblem.setAcceptance(new BigDecimal(proSolvedCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
        }
        return proProblem;
    }

    @Override
    public Page<ProProblem> appPage(ProProblemPageParam proProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblem::getIsPublic, true);
        // 关键字
        if (ObjectUtil.isNotEmpty(proProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, proProblemPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(proProblemPageParam.getSortField(), proProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(proProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proProblemPageParam.getSortField()));
        }

        Page<ProProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proProblemPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(item -> {
            // 缓存取出标签列表
            List<SysTag> tagsById = proProblemTagService.getTagsById(item.getId());
            if (ObjectUtil.isNotEmpty(tagsById)) {
                item.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
                item.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
            }
            // 测试用例脱敏
            item.setTestCase(List.of());
            // 模板脱敏
            if (ObjectUtil.isNotEmpty(item.getCodeTemplate())) {
                item.getCodeTemplate().forEach(template -> {
                    template.setPrefix(null);
                    template.setSuffix(null);
                });
            }
            // 解决记录
            try {
                String loginIdAsString = StpUtil.getLoginIdAsString();
                // 缓存取出解决记录
                ProSolved proSolved = proSolvedMapper.selectOne(new QueryWrapper<ProSolved>().lambda()
                        .eq(ProSolved::getUserId, loginIdAsString)
                        .eq(ProSolved::getProblemId, item.getId()));
                if (ObjectUtil.isNotNull(proSolved)) {
                    if (proSolved.getSolved()) {
                        item.setCurrentUserSolved(true);
                    } else {
                        item.setCurrentUserSolved(false);
                    }
                }
            } catch (Exception e) {
                item.setCurrentUserSolved(false);
                e.printStackTrace();
            }

            // 通过率计算（缓存读取）
            Long proSolvedCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()).eq(ProSolved::getSolved, true));
            Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()));
            if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
                item.setAcceptance(BigDecimal.ZERO);
            } else {
                item.setAcceptance(new BigDecimal(proSolvedCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
            }
            // 参与人数
            item.setParticipantCount(String.valueOf(proSolvedTotalCount));
        });
        return page;
    }


    @Override
    public List<ProProblem> latestN(int n) {
        List<ProProblem> list = this.list(new QueryWrapper<ProProblem>().checkSqlInjection().lambda().orderByDesc(ProProblem::getCreateTime).last("LIMIT " + n));
        list.forEach(item -> {
            List<SysTag> tagsById = proProblemTagService.getTagsById(item.getId());
            if (ObjectUtil.isNotEmpty(tagsById)) {
                item.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
                item.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
            }
            item.setTestCase(List.of());
            if (ObjectUtil.isNotEmpty(item.getCodeTemplate())) {
                item.getCodeTemplate().forEach(template -> {
                    template.setPrefix(null);
                    template.setSuffix(null);
                });
            }

            try {
                String loginIdAsString = StpUtil.getLoginIdAsString();
                ProSolved proSolved = proSolvedMapper.selectOne(new QueryWrapper<ProSolved>().lambda()
                        .eq(ProSolved::getUserId, loginIdAsString)
                        .eq(ProSolved::getProblemId, item.getId()));
                item.setCurrentUserSolved(proSolved.getSolved());
            } catch (Exception ignored) {
                item.setCurrentUserSolved(false);
            }
        });
        return list;
    }

    @Override
    public String getDescription(String problemId) {
        return this.getById(problemId).getDescription();
    }

    @Override
    public String getTestCase(String problemId) {
        try {
            List<TestCase> testCases = this.getById(problemId).getTestCase();

            if (testCases == null || testCases.isEmpty()) {
                return "该题目暂无测试用例";
            }

            // 随机选择一个测试用例
            TestCase selectedCase = testCases.get(ThreadLocalRandom.current().nextInt(testCases.size()));

            return String.format("""
                            输入:
                            %s

                            预期输出:
                            %s
                            """,
                    selectedCase.getInput(),
                    selectedCase.getOutput());
        } catch (Exception e) {
            log.error("获取测试用例失败，题目ID: {}", problemId, e);
            return "无法获取测试用例";
        }
    }

    @Override
    public List<DifficultyDistribution> difficultyDistribution() {
        long totalCount = this.count();

        // 统计难度分布-简单
        long simpleCount = this.count(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getDifficulty, 1));
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
        long mediumCount = this.count(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getDifficulty, 2));
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
        long hardCount = this.count(new LambdaQueryWrapper<ProProblem>().eq(ProProblem::getDifficulty, 3));
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

    @Override
    public ProblemCountAndIncreasedPercentage getProblemCountAndPercentage() {
        ProblemCountAndIncreasedPercentage problemCountAndIncreasedPercentage = new ProblemCountAndIncreasedPercentage();
        problemCountAndIncreasedPercentage.setCount(String.valueOf(this.count()));
        // 相比上个月增长了题目占用百分比
        long count = this.count(new QueryWrapper<ProProblem>().checkSqlInjection().lambda().gt(ProProblem::getCreateTime, DateUtil.offsetMonth(new Date(), -1)));
        if (count == 0) {
            problemCountAndIncreasedPercentage.setIncreasedPercentage("0.00");
        } else {
            problemCountAndIncreasedPercentage.setIncreasedPercentage(String.valueOf(new BigDecimal(count)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(this.count()), 2, RoundingMode.DOWN)));
        }
        return problemCountAndIncreasedPercentage;
    }

    @Override
    public TodayProblemCount getTodayProblemCount() {
        TodayProblemCount todayProblemCount = new TodayProblemCount();
        todayProblemCount.setTodayProblemCount(String.valueOf(this.count(new QueryWrapper<ProProblem>().checkSqlInjection().lambda().gt(ProProblem::getCreateTime, DateUtil.beginOfDay(new Date())))));
        // 最近新增题目的时间
        ProProblem latestProblem = this.getOne(new QueryWrapper<ProProblem>()
                .checkSqlInjection()
                .lambda()
                .orderByDesc(ProProblem::getCreateTime)
                .last("LIMIT 1"));
        if (ObjectUtil.isNotEmpty(latestProblem)) {
            todayProblemCount.setLatestCreateTime(latestProblem.getCreateTime());
        } else {
            todayProblemCount.setLatestCreateTime(null);
        }
        return todayProblemCount;
    }

    @Override
    public Page<ProProblem> userRecentSolvedPage(UserProblemPageParam userProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblem::getIsPublic, true);
        // 关键字
        if (ObjectUtil.isNotEmpty(userProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, userProblemPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(userProblemPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(ProProblem::getCategoryId, userProblemPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(userProblemPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(ProProblem::getDifficulty, userProblemPageParam.getDifficulty());
        }

        // 用户提交记录查询
        List<String> problemIds = proSubmitMapper.selectList(new LambdaQueryWrapper<ProSubmit>()
                        .eq(ProSubmit::getUserId, userProblemPageParam.getUserId())
                        // 按时间倒序
                        .orderByDesc(ProSubmit::getCreateTime)
                )
                .stream()
                .map(ProSubmit::getProblemId)
                .distinct()
                .toList();
        if (ObjectUtil.isNotEmpty(problemIds)) {
            queryWrapper.lambda().in(ProProblem::getId, problemIds);
            // 结果的顺序和用户提交记录的顺序一致
//            queryWrapper.lambda().orderBy(true, false, submit -> problemIds.indexOf(submit.getId()));
        } else {
            queryWrapper.lambda().eq(ProProblem::getId, "-1"); // 确保查询不到结果
        }

        // 标签查询
        if (GaStringUtil.isNotEmpty(userProblemPageParam.getTagId())) {
            List<String> idsByTagId = proProblemTagService.getIdsByTagId(userProblemPageParam.getTagId());
            // 如果TagId有值但关联的ID列表为空，则设置一个不可能满足的条件
            if (ObjectUtil.isEmpty(idsByTagId)) {
                queryWrapper.lambda().eq(ProProblem::getId, "-1"); // 确保查询不到结果
            } else {
                queryWrapper.lambda().in(ProProblem::getId, idsByTagId);
            }
        }

        if (ObjectUtil.isAllNotEmpty(userProblemPageParam.getSortField(), userProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(userProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    userProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(userProblemPageParam.getSortField()));
        }

        Page<ProProblem> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(userProblemPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(userProblemPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(item -> {
            List<SysTag> tagsById = proProblemTagService.getTagsById(item.getId());
            if (ObjectUtil.isNotEmpty(tagsById)) {
                item.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
                item.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
            }

            // 解决记录
            try {
                String loginIdAsString = StpUtil.getLoginIdAsString();
                // 缓存取出解决记录
                ProSolved proSolved = proSolvedMapper.selectOne(new QueryWrapper<ProSolved>().lambda()
                        .eq(ProSolved::getUserId, loginIdAsString)
                        .eq(ProSolved::getProblemId, item.getId()));
                if (proSolved.getSolved()) {
                    item.setCurrentUserSolved(true);
                } else {
                    item.setCurrentUserSolved(false);
                }
            } catch (Exception ignored) {
                item.setCurrentUserSolved(false);
            }

            // 通过率计算（缓存读取）
            Long proSolvedCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()).eq(ProSolved::getSolved, true));
            Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, item.getId()));
            if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
                item.setAcceptance(BigDecimal.ZERO);
            } else {
                item.setAcceptance(new BigDecimal(proSolvedCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
            }
            // 参与人数
            item.setParticipantCount(String.valueOf(proSolvedTotalCount));
        });

        return page;
    }

}