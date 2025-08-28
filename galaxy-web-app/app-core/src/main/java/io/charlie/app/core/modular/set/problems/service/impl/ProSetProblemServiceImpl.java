package io.charlie.app.core.modular.set.problems.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.SetProblemPageParam;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.problems.mapper.ProSetProblemMapper;
import io.charlie.app.core.modular.set.problems.service.ProSetProblemService;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.sys.tag.entity.SysTag;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.pojo.CommonPageRequest;
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
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetProblemServiceImpl extends ServiceImpl<ProSetProblemMapper, ProSetProblem> implements ProSetProblemService {
    private final ProProblemTagService proProblemTagService;
    private final ProSetSolvedMapper proSetSolvedMapper;

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
}
