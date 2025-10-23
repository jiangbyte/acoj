package io.charlie.web.oj.modular.sys.banner.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.data.set.mapper.DataSetMapper;
import io.charlie.web.oj.modular.sys.banner.entity.SysBanner;
import io.charlie.web.oj.modular.sys.banner.param.*;
import io.charlie.web.oj.modular.sys.banner.mapper.SysBannerMapper;
import io.charlie.web.oj.modular.sys.banner.service.SysBannerService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.sys.notice.entity.SysNotice;
import io.charlie.web.oj.modular.sys.notice.mapper.SysNoticeMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 横幅表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysBannerServiceImpl extends ServiceImpl<SysBannerMapper, SysBanner> implements SysBannerService {
    private final SysUserMapper sysUserMapper;
    private final SysNoticeMapper sysNoticeMapper;
    private final DataProblemMapper dataProblemMapper;
    private final DataSetMapper dataSetMapper;

    @Override
    public Page<SysBanner> page(SysBannerPageParam sysBannerPageParam) {
        QueryWrapper<SysBanner> queryWrapper = new QueryWrapper<SysBanner>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysBannerPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysBanner::getTitle, sysBannerPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysBannerPageParam.getSortField(), sysBannerPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysBannerPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysBannerPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysBannerPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByDesc(SysBanner::getCreateTime);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysBannerPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysBannerPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysBannerAddParam sysBannerAddParam) {
        SysBanner bean = BeanUtil.toBean(sysBannerAddParam, SysBanner.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysBannerEditParam sysBannerEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysBanner>().eq(SysBanner::getId, sysBannerEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysBanner bean = BeanUtil.toBean(sysBannerEditParam, SysBanner.class);
        BeanUtil.copyProperties(sysBannerEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysBannerIdParam> sysBannerIdParamList) {
        if (ObjectUtil.isEmpty(sysBannerIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysBannerIdParamList, SysBannerIdParam::getId));
    }

    @Override
    public SysBanner detail(SysBannerIdParam sysBannerIdParam) {
        SysBanner sysBanner = this.getById(sysBannerIdParam.getId());
        if (ObjectUtil.isEmpty(sysBanner)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysBanner;
    }

    @Override
    public List<SysBanner> latestN(int n) {
        return this.list(new QueryWrapper<SysBanner>().checkSqlInjection()
                .lambda()
                .eq(SysBanner::getIsVisible, true)
                .orderByDesc(SysBanner::getCreateTime)
                .last("LIMIT " + n)
        );
    }

    @Override
    public void visibleToggle(SysBannerIdParam sysBannerIdParam) {
        SysBanner sysBanner = this.getById(sysBannerIdParam.getId());
        if (ObjectUtil.isEmpty(sysBanner)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        sysBanner.setIsVisible(!sysBanner.getIsVisible());
        this.updateById(sysBanner);
    }

    @Override
    public List<SysBannerJumpTargetResult> jumpTargetList(SysBannerJumpTargetParam sysBannerJumpTargetParam) {
        String jumpModule = sysBannerJumpTargetParam.getJumpModule();
        String keyword = sysBannerJumpTargetParam.getKeyword();

        if (GaStringUtil.isEmpty(jumpModule)) {
            return List.of();
        }

        switch (jumpModule) {
            // 公告
            case "NOTICE":
                List<SysNotice> sysNotices = sysNoticeMapper.selectList(new QueryWrapper<SysNotice>().checkSqlInjection()
                        .lambda()
                        .like(GaStringUtil.isNotEmpty(keyword), SysNotice::getTitle, keyword)
                        .eq(SysNotice::getIsVisible, Boolean.TRUE)
                );
                if (ObjectUtil.isEmpty(sysNotices)) {
                    return List.of();
                }
                return sysNotices.stream().map(sysNotice -> {
                    SysBannerJumpTargetResult sysBannerJumpTargetResult = new SysBannerJumpTargetResult();
                    sysBannerJumpTargetResult.setId(sysNotice.getId());
                    sysBannerJumpTargetResult.setName(sysNotice.getTitle());
                    return sysBannerJumpTargetResult;
                }).toList();
            //  个人
            case "PERSONAL":
                List<SysUser> sysUsers = sysUserMapper.selectList(new QueryWrapper<SysUser>().checkSqlInjection()
                        .lambda()
                        .like(GaStringUtil.isNotEmpty(keyword), SysUser::getUsername, keyword)
                );
                if (ObjectUtil.isEmpty(sysUsers)) {
                    return List.of();
                }
                return sysUsers.stream().map(sysUser -> {
                    SysBannerJumpTargetResult sysBannerJumpTargetResult = new SysBannerJumpTargetResult();
                    sysBannerJumpTargetResult.setId(sysUser.getId());
                    sysBannerJumpTargetResult.setName(sysUser.getNickname());
                    return sysBannerJumpTargetResult;
                }).toList();
            // 题目
            case "PROBLEM":
                List<DataProblem> dataProblems = dataProblemMapper.selectList(new QueryWrapper<DataProblem>().checkSqlInjection()
                        .lambda()
                        .like(GaStringUtil.isNotEmpty(keyword), DataProblem::getTitle, keyword)
                        .eq(DataProblem::getIsVisible, Boolean.TRUE)
                        .eq(DataProblem::getIsPublic, Boolean.TRUE)
                );
                if (ObjectUtil.isEmpty(dataProblems)) {
                    return List.of();
                }

                return dataProblems.stream().map(dataProblem -> {
                    SysBannerJumpTargetResult sysBannerJumpTargetResult = new SysBannerJumpTargetResult();
                    sysBannerJumpTargetResult.setId(dataProblem.getId());
                    sysBannerJumpTargetResult.setName(dataProblem.getTitle());
                    return sysBannerJumpTargetResult;
                }).toList();
            // 集
            case "SET":
                List<DataSet> dataSets = dataSetMapper.selectList(new QueryWrapper<DataSet>().checkSqlInjection()
                        .lambda()
                        .like(GaStringUtil.isNotEmpty(keyword), DataSet::getTitle, keyword)
                        .eq(DataSet::getIsVisible, Boolean.TRUE)
                );

                if (ObjectUtil.isEmpty(dataSets)) {
                    return List.of();
                }
                return dataSets.stream().map(dataSet -> {
                    SysBannerJumpTargetResult sysBannerJumpTargetResult = new SysBannerJumpTargetResult();
                    sysBannerJumpTargetResult.setId(dataSet.getId());
                    sysBannerJumpTargetResult.setName(dataSet.getTitle());
                    return sysBannerJumpTargetResult;
                }).toList();
            default:
                return List.of();
        }
    }

}