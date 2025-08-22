package io.charlie.app.core.modular.problem.problem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.ProProblemAddParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemEditParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemIdParam;
import io.charlie.app.core.modular.problem.problem.param.ProProblemPageParam;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.problem.service.ProProblemService;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
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
public class ProProblemServiceImpl extends ServiceImpl<ProProblemMapper, ProProblem> implements ProProblemService {
    private final ProProblemTagService proProblemTagService;
    private final ProSolvedMapper proSolvedMapper;

    @Override
    public Page<ProProblem> page(ProProblemPageParam proProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
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
        ProProblem proProblem = this.getById(proProblemIdParam.getId());
        if (ObjectUtil.isEmpty(proProblem)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<SysTag> tagsById = proProblemTagService.getTagsById(proProblemIdParam.getId());
        if (ObjectUtil.isNotEmpty(tagsById)) {
            proProblem.setTagIds(tagsById.stream().map(SysTag::getId).distinct().toList());
            proProblem.setTagNames(tagsById.stream().map(SysTag::getName).distinct().toList());
        }
        return proProblem;
    }

    @Override
    public ProProblem detailC(ProProblemIdParam proProblemIdParam) {
        ProProblem proProblem = this.getById(proProblemIdParam.getId());
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
        return proProblem;
    }

    @Override
    public Page<ProProblem> pageC(ProProblemPageParam proProblemPageParam) {
        QueryWrapper<ProProblem> queryWrapper = new QueryWrapper<ProProblem>().checkSqlInjection();
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

}