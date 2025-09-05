package io.charlie.app.core.modular.set.problems.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.problem.param.SetProblemListParam;
import io.charlie.app.core.modular.problem.problem.param.SetProblemPageParam;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.problems.mapper.ProSetProblemMapper;
import io.charlie.app.core.modular.set.problems.param.ProSetProblemIdParam;
import io.charlie.app.core.modular.set.problems.service.ProSetProblemService;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.sys.tag.entity.SysTag;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description 题集题目服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetProblemServiceImpl extends ServiceImpl<ProSetProblemMapper, ProSetProblem> implements ProSetProblemService {
    private final ProProblemTagService proProblemTagService;
    private final ProSetSolvedMapper proSetSolvedMapper;
    private final ProProblemMapper proProblemMapper;

    @Override
    public Page<ProProblem> setProblemPage(SetProblemPageParam setProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setProblemPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, setProblemPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(setProblemPageParam.getSortField(), setProblemPageParam.getSortOrder()) && ISortOrderEnum.isValid(setProblemPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setProblemPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setProblemPageParam.getSortField()));
        }

        if (StrUtil.isBlank(setProblemPageParam.getSetId())) {
            // 空返回空
            return new Page<>();
        }

        Page<ProProblem> page = (Page<ProProblem>) this.baseMapper.selectSetProblemPage(CommonPageRequest.Page(
                Optional.ofNullable(setProblemPageParam.getCurrent()).orElse(1),
                Optional.ofNullable(setProblemPageParam.getSize()).orElse(20),
                null
        ), queryWrapper, setProblemPageParam.getSetId());

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
                ProSetSolved proSolved = proSetSolvedMapper.selectOne(new QueryWrapper<ProSetSolved>().lambda()
                        .eq(ProSetSolved::getUserId, loginIdAsString)
                        .eq(ProSetSolved::getProblemSetId, setProblemPageParam.getSetId())
                        .eq(ProSetSolved::getProblemId, item.getId()));
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
            Long proSolvedCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, item.getId()).eq(ProSetSolved::getSolved, true));
            Long proSolvedTotalCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, item.getId()));
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
    public List<ProProblem> setProblemList(SetProblemListParam setProblemListParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(setProblemListParam.getKeyword())) {
            queryWrapper.lambda().like(ProProblem::getTitle, setProblemListParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(setProblemListParam.getSortField(), setProblemListParam.getSortOrder()) && ISortOrderEnum.isValid(setProblemListParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    setProblemListParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(setProblemListParam.getSortField()));
        }

        if (StrUtil.isBlank(setProblemListParam.getSetId())) {
            // 空返回空
            return List.of();
        }

        List<ProProblem> proProblemList = this.baseMapper.selectSetProblemList(queryWrapper, setProblemListParam.getSetId());

        if (ObjectUtil.isEmpty(proProblemList)) {
            return List.of();
        }

        proProblemList.forEach(item -> {
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
                ProSetSolved proSolved = proSetSolvedMapper.selectOne(new QueryWrapper<ProSetSolved>().lambda()
                        .eq(ProSetSolved::getUserId, loginIdAsString)
                        .eq(ProSetSolved::getProblemSetId, setProblemListParam.getSetId())
                        .eq(ProSetSolved::getProblemId, item.getId()));
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
            Long proSolvedCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, item.getId()).eq(ProSetSolved::getSolved, true));
            Long proSolvedTotalCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, item.getId()));
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

        return proProblemList;
    }

    // 获得题集的题目详情（含当前题目的序号、上下题目的ID号）
    @Override
    public ProProblem getSetProblemDetail(ProSetProblemIdParam proSetProblemIdParam) {
        // 查询当前题集的题目集合
        List<ProSetProblem> list = this.list(new QueryWrapper<ProSetProblem>().lambda()
                .eq(ProSetProblem::getProblemSetId, proSetProblemIdParam.getProblemSetId())
                .orderByAsc(ProSetProblem::getSort)
        );
        if (ObjectUtil.isEmpty(list)) {
            throw new BusinessException("该题集没有题目");
        }
        // 根据题目ID查找对应的题目
        String problemId = proSetProblemIdParam.getProblemId();
        ProSetProblem setProblem = list.stream()
                .filter(item -> item.getProblemId().equals(problemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("题目不存在于该题集中"));
        // 查询题目详情
        ProProblem proProblem = proProblemMapper.selectById(problemId);
        if (ObjectUtil.isNull(proProblem)) {
            throw new BusinessException("题目不存在");
        }
        // 获取当前题目在列表中的索引
        int currentIndex = list.indexOf(setProblem);
        // 设置当前题目的所在索引（序号从1开始）
        proProblem.setSort(currentIndex + 1);
        // 设置上一个题目的ID
        if (currentIndex > 0) {
            ProSetProblem prevProblem = list.get(currentIndex - 1);
            proProblem.setPreviousProblemId(prevProblem.getProblemId());
        } else {
            proProblem.setPreviousProblemId(null); // 第一个题目没有上一题
        }
        // 设置下一个题目的ID
        if (currentIndex < list.size() - 1) {
            ProSetProblem nextProblem = list.get(currentIndex + 1);
            proProblem.setNextProblemId(nextProblem.getProblemId());
        } else {
            proProblem.setNextProblemId(null); // 最后一个题目没有下一题
        }
        // 设置题目总数
        proProblem.setTotalProblemCount(list.size());
        // 设置是否是最后一个题目
        proProblem.setIsLastProblem(currentIndex == list.size() - 1);


        proProblem.setTestCase(List.of());
        if (ObjectUtil.isNotEmpty(proProblem.getCodeTemplate())) {
            proProblem.getCodeTemplate().forEach(template -> {
                template.setPrefix(null);
                template.setSuffix(null);
            });
        }
        List<SysTag> tagsById = proProblemTagService.getTagsById(problemId);
        if (ObjectUtil.isNotEmpty(tagsById)) {
            proProblem.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
            proProblem.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
        }

        // 通过率计算（缓存读取）
        Long proSolvedCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, proProblem.getId()).eq(ProSetSolved::getSolved, true));
        Long proSolvedTotalCount = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getProblemId, proProblem.getId()));
        if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
            proProblem.setAcceptance(BigDecimal.ZERO);
        } else {
            proProblem.setAcceptance(new BigDecimal(proSolvedCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(proSolvedTotalCount), 2, RoundingMode.DOWN));
        }
        return proProblem;
    }
}
