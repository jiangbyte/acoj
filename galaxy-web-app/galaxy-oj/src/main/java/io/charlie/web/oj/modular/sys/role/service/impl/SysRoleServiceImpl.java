package io.charlie.web.oj.modular.sys.role.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import io.charlie.web.oj.modular.sys.role.param.SysRoleAddParam;
import io.charlie.web.oj.modular.sys.role.param.SysRoleEditParam;
import io.charlie.web.oj.modular.sys.role.param.SysRoleIdParam;
import io.charlie.web.oj.modular.sys.role.param.SysRolePageParam;
import io.charlie.web.oj.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.oj.modular.sys.role.service.SysRoleService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 角色表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    private final SysUserRoleService sysUserRoleService;

    @Override
    public Page<SysRole> page(SysRolePageParam sysRolePageParam) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<SysRole>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysRolePageParam.getKeyword())) {
            queryWrapper.lambda().like(SysRole::getName, sysRolePageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysRolePageParam.getSortField(), sysRolePageParam.getSortOrder()) && ISortOrderEnum.isValid(sysRolePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysRolePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysRolePageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysRolePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysRolePageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysRoleAddParam sysRoleAddParam) {
        SysRole bean = BeanUtil.toBean(sysRoleAddParam, SysRole.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysRoleEditParam sysRoleEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysRole>().eq(SysRole::getId, sysRoleEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysRole bean = BeanUtil.toBean(sysRoleEditParam, SysRole.class);
        BeanUtil.copyProperties(sysRoleEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysRoleIdParam> sysRoleIdParamList) {
        if (ObjectUtil.isEmpty(sysRoleIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysRoleIdParamList, SysRoleIdParam::getId));
    }

    @Override
    public SysRole detail(SysRoleIdParam sysRoleIdParam) {
        SysRole sysRole = this.getById(sysRoleIdParam.getId());
        if (ObjectUtil.isEmpty(sysRole)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysRole;
    }

    @Override
    public List<SysRole> authRoles() {
        String loginIdAsString = StpUtil.getLoginIdAsString();
        SysRole heightLevelRole = sysUserRoleService.getHeightLevelRole(loginIdAsString);
        if (heightLevelRole == null) {
            return List.of();
        }

        Integer level = heightLevelRole.getLevel();
        // 获得比最高层级低的角色列表（包含最高角色本身）
        return this.list(new LambdaQueryWrapper<SysRole>()
                .le(SysRole::getLevel, level)  // 查询层级小于等于当前最高层级的角色
                .orderByAsc(SysRole::getLevel) // 按层级升序排列
        );
    }

}