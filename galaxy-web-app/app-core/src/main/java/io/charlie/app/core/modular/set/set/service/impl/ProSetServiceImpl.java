package io.charlie.app.core.modular.set.set.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.set.problems.entity.ProSetProblem;
import io.charlie.app.core.modular.set.problems.mapper.ProSetProblemMapper;
import io.charlie.app.core.modular.set.set.entity.ProSet;
import io.charlie.app.core.modular.set.set.enums.SetTypeEnum;
import io.charlie.app.core.modular.set.set.param.*;
import io.charlie.app.core.modular.set.set.mapper.ProSetMapper;
import io.charlie.app.core.modular.set.set.service.ProSetService;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.mapper.ProSetSubmitMapper;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.galaxy.utils.str.GaStringUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 题集 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProSetServiceImpl extends ServiceImpl<ProSetMapper, ProSet> implements ProSetService {
    private final ProSetSolvedMapper proSetSolvedMapper;
    private final ProSetProblemMapper proSetProblemMapper;
    private final ProSetSubmitMapper proSetSubmitMapper;

    @Override
    public Page<ProSet> page(ProSetPageParam proSetPageParam) {
        QueryWrapper<ProSet> queryWrapper = new QueryWrapper<ProSet>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(proSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProSet::getTitle, proSetPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(proSetPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(ProSet::getCategoryId, proSetPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(proSetPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(ProSet::getDifficulty, proSetPageParam.getDifficulty());
        }
        if (GaStringUtil.isNotEmpty(proSetPageParam.getSetType())) {
            queryWrapper.lambda().eq(ProSet::getSetType, proSetPageParam.getSetType());
        }
        if (ObjectUtil.isAllNotEmpty(proSetPageParam.getSortField(), proSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(proSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    proSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(proSetPageParam.getSortField()));
        }

        Page<ProSet> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(proSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(proSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(proSet -> {
            // 如果是限时题集，判断是否正在运行
            if (Objects.equals(proSet.getSetType(), SetTypeEnum.LIMIT_TIME_SET.getValue())) {
                proSet.setIsRunning(new Date().after(proSet.getStartTime()) && new Date().before(proSet.getEndTime()));
            } else {
                proSet.setIsRunning(false);
            }

            // 计算题目数量
            Long l = proSetProblemMapper.selectCount(new LambdaQueryWrapper<ProSetProblem>().eq(ProSetProblem::getProblemSetId, proSet.getId()));
            proSet.setProblemCount(String.valueOf(l));

            // 总提交次数
            Long l1 = proSetSubmitMapper.selectCount(new QueryWrapper<ProSetSubmit>().lambda().eq(ProSetSubmit::getSetId, proSet.getId()));
            proSet.setSubmitCount(String.valueOf(l1));

            // TODO 平均通过率
            proSet.setAvgPassRate(BigDecimal.ZERO);

            // 参与人数
            Long l2 = proSetSolvedMapper.selectCount(new QueryWrapper<ProSetSolved>().lambda().eq(ProSetSolved::getProblemSetId, proSet.getId()));
            proSet.setParticipantCount(String.valueOf(l2));
        });
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProSetAddParam proSetAddParam) {
        ProSet bean = BeanUtil.toBean(proSetAddParam, ProSet.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProSetEditParam proSetEditParam) {
        if (!this.exists(new LambdaQueryWrapper<ProSet>().eq(ProSet::getId, proSetEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ProSet bean = BeanUtil.toBean(proSetEditParam, ProSet.class);
        BeanUtil.copyProperties(proSetEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<ProSetIdParam> proSetIdParamList) {
        if (ObjectUtil.isEmpty(proSetIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(proSetIdParamList, ProSetIdParam::getId));
    }

    @Override
    public ProSet detail(ProSetIdParam proSetIdParam) {
        ProSet proSet = this.getById(proSetIdParam.getId());
        if (ObjectUtil.isEmpty(proSet)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        // 计算题目数量
        Long l = proSetProblemMapper.selectCount(new LambdaQueryWrapper<ProSetProblem>().eq(ProSetProblem::getProblemSetId, proSet.getId()));
        proSet.setProblemCount(String.valueOf(l));

        // 总提交次数
        Long l1 = proSetSubmitMapper.selectCount(new QueryWrapper<ProSetSubmit>().lambda().eq(ProSetSubmit::getSetId, proSet.getId()));
        proSet.setSubmitCount(String.valueOf(l1));

        // TODO 平均通过率
        proSet.setAvgPassRate(BigDecimal.ZERO);

        // 参与人数
        Long l2 = proSetSolvedMapper.selectCount(new QueryWrapper<ProSetSolved>().lambda().eq(ProSetSolved::getProblemSetId, proSet.getId()));
        proSet.setParticipantCount(String.valueOf(l2));
        return proSet;
    }

    @Override
    public List<ProSet> latestN(int n) {
        List<ProSet> list = this.list(new QueryWrapper<ProSet>().checkSqlInjection()
                .lambda()
                .orderByAsc(ProSet::getCreateTime)
                .last("LIMIT " + n)
        );
        for (ProSet proSet : list) {
            // 计算题目数量
            Long l = proSetProblemMapper.selectCount(new LambdaQueryWrapper<ProSetProblem>().eq(ProSetProblem::getProblemSetId, proSet.getId()));
            proSet.setProblemCount(String.valueOf(l));

            // 总提交次数
            Long l1 = proSetSubmitMapper.selectCount(new QueryWrapper<ProSetSubmit>().lambda().eq(ProSetSubmit::getSetId, proSet.getId()));
            proSet.setSubmitCount(String.valueOf(l1));

            // TODO 平均通过率
            proSet.setAvgPassRate(BigDecimal.ZERO);

            // 参与人数
            Long l2 = proSetSolvedMapper.selectCount(new QueryWrapper<ProSetSolved>().lambda().eq(ProSetSolved::getProblemSetId, proSet.getId()));
            proSet.setParticipantCount(String.valueOf(l2));
        }
        return list;
    }

    @Override
    public Page<ProSet> userRecentSolvedPage(UserSetPageParam userSetPageParam) {
        QueryWrapper<ProSet> queryWrapper = new QueryWrapper<ProSet>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(userSetPageParam.getKeyword())) {
            queryWrapper.lambda().like(ProSet::getTitle, userSetPageParam.getKeyword());
        }
        if (GaStringUtil.isNotEmpty(userSetPageParam.getCategoryId())) {
            queryWrapper.lambda().eq(ProSet::getCategoryId, userSetPageParam.getCategoryId());
        }
        if (GaStringUtil.isNotEmpty(userSetPageParam.getDifficulty())) {
            queryWrapper.lambda().eq(ProSet::getDifficulty, userSetPageParam.getDifficulty());
        }
        if (GaStringUtil.isNotEmpty(userSetPageParam.getSetType())) {
            queryWrapper.lambda().eq(ProSet::getSetType, userSetPageParam.getSetType());
        }
        if (ObjectUtil.isAllNotEmpty(userSetPageParam.getSortField(), userSetPageParam.getSortOrder()) && ISortOrderEnum.isValid(userSetPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    userSetPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(userSetPageParam.getSortField()));
        }

        // 用户提交记录查询
        List<String> setIds = proSetSolvedMapper.selectList(new LambdaQueryWrapper<ProSetSolved>()
                        .eq(ProSetSolved::getUserId, userSetPageParam.getUserId())
                        // 按时间倒序
                        .orderByDesc(ProSetSolved::getCreateTime)
                )
                .stream()
                .map(ProSetSolved::getProblemSetId)
                .distinct()
                .toList();
        if (ObjectUtil.isNotEmpty(setIds)) {
            queryWrapper.lambda().in(ProSet::getId, setIds);
        } else {
            queryWrapper.lambda().eq(ProSet::getId, "-1"); // 确保查询不到结果
        }

        Page<ProSet> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(userSetPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(userSetPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(proSet -> {
            // 如果是限时题集，判断是否正在运行
            if (Objects.equals(proSet.getSetType(), SetTypeEnum.LIMIT_TIME_SET.getValue())) {
                proSet.setIsRunning(new Date().after(proSet.getStartTime()) && new Date().before(proSet.getEndTime()));
            } else {
                proSet.setIsRunning(false);
            }

            // 计算题目数量
            Long l = proSetProblemMapper.selectCount(new LambdaQueryWrapper<ProSetProblem>().eq(ProSetProblem::getProblemSetId, proSet.getId()));
            proSet.setProblemCount(String.valueOf(l));

            // 总提交次数
            Long l1 = proSetSubmitMapper.selectCount(new QueryWrapper<ProSetSubmit>().lambda().eq(ProSetSubmit::getSetId, proSet.getId()));
            proSet.setSubmitCount(String.valueOf(l1));

            // TODO 平均通过率
            proSet.setAvgPassRate(BigDecimal.ZERO);

            // 参与人数
            Long l2 = proSetSolvedMapper.selectCount(new QueryWrapper<ProSetSolved>().lambda().eq(ProSetSolved::getProblemSetId, proSet.getId()));
            proSet.setParticipantCount(String.valueOf(l2));
        });
        return page;
    }

}