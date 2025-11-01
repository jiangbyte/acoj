package io.charlie.web.oj.modular.sys.role.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
import io.charlie.web.oj.modular.sys.relation.entity.SysRoleMenu;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
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
import io.charlie.web.oj.modular.sys.role.utils.SysRoleBuildUtil;
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
    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleBuildUtil sysRoleBuildUtil;

    private final DataScopeUtil dataScopeUtil;

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    @DS("slave")
    public Page<SysRole> page(SysRolePageParam sysRolePageParam) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<SysRole>().checkSqlInjection();

        List<String> accessibleRoleIds = dataScopeUtil.getDataScopeContext().getAccessibleRoleIds();
        if (accessibleRoleIds.isEmpty()) {
            return new Page<>();
        }
        queryWrapper.lambda().in(SysRole::getId, accessibleRoleIds);

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

        Page<SysRole> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysRolePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysRolePageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        sysRoleBuildUtil.buildRoles(page.getRecords());
        return page;
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
        List<String> stringList = CollStreamUtil.toList(sysRoleIdParamList, SysRoleIdParam::getId);
        // 先查看有没有用户授权到
        boolean exists = sysUserRoleMapper.exists(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, stringList)
        );
        if (exists) {
            throw new BusinessException("当前角色正在被授权用户中，请先将授权用户授权为其他角色");
        }

        this.removeByIds(stringList);

        if (ObjectUtil.isNotEmpty(stringList)) {
            // 删除角色对应的菜单关联
            sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                    .in(SysRoleMenu::getRoleId, stringList));
        }
    }

    @Override
    @DS("slave")
    public SysRole detail(SysRoleIdParam sysRoleIdParam) {
        SysRole sysRole = this.getById(sysRoleIdParam.getId());
        if (ObjectUtil.isEmpty(sysRole)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        sysRoleBuildUtil.buildRole(sysRole);
        return sysRole;
    }

    @Override
    @DS("slave")
    public List<SysRole> authRoles() {
        List<String> accessibleRoleIds = dataScopeUtil.getDataScopeContext().getAccessibleRoleIds();
        if (accessibleRoleIds.isEmpty()) {
            return List.of();
        }
        return this.listByIds(accessibleRoleIds);
    }

    @Override
    @DS("slave")
    public List<SysRole> authRoles1() {
        List<String> accessibleRoleIds = dataScopeUtil.getDataScopeContext().getAccessibleRoleIds();
        if (accessibleRoleIds.isEmpty()) {
            return List.of();
        }
        return this.listByIds(accessibleRoleIds);
    }

    @Override
    @DS("slave")
    public List<String> getRoleNamesByUserId(String userId) {
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );
        if (ObjectUtil.isEmpty(sysUserRoles)) {
            return List.of();
        }
        List<SysRole> list = this.list(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, sysUserRoles.stream().map(SysUserRole::getRoleId).toList())
        );
        if (ObjectUtil.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(SysRole::getName).toList();
    }

}