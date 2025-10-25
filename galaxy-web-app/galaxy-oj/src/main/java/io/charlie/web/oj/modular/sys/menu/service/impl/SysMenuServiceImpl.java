package io.charlie.web.oj.modular.sys.menu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.context.DataScopeUtil;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import io.charlie.web.oj.modular.sys.menu.param.*;
import io.charlie.web.oj.modular.sys.menu.mapper.SysMenuMapper;
import io.charlie.web.oj.modular.sys.menu.service.SysMenuService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.sys.relation.service.SysRoleMenuService;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 菜单表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    private final DataScopeUtil dataScopeUtil;

    @Override
    public Page<SysMenu> page(SysMenuPageParam sysMenuPageParam) {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<SysMenu>().checkSqlInjection();

        List<String> accessibleMenuIds = dataScopeUtil.getDataScopeContext().getAccessibleMenuIds();
        if (accessibleMenuIds.isEmpty()) {
            return new Page<>();
        }
        queryWrapper.lambda().in(SysMenu::getId, accessibleMenuIds);

        // 关键字
        if (ObjectUtil.isNotEmpty(sysMenuPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysMenu::getName, sysMenuPageParam.getKeyword());
        }
        // 关键字
        if (ObjectUtil.isNotEmpty(sysMenuPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysMenu::getTitle, sysMenuPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysMenuPageParam.getSortField(), sysMenuPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysMenuPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysMenuPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysMenuPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysMenuPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysMenuPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysMenuAddParam sysMenuAddParam) {
        SysMenu bean = BeanUtil.toBean(sysMenuAddParam, SysMenu.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysMenuEditParam sysMenuEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getId, sysMenuEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysMenu bean = BeanUtil.toBean(sysMenuEditParam, SysMenu.class);
        BeanUtil.copyProperties(sysMenuEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysMenuIdParam> sysMenuIdParamList) {
        if (ObjectUtil.isEmpty(sysMenuIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysMenuIdParamList, SysMenuIdParam::getId));
    }

    @Override
    public SysMenu detail(SysMenuIdParam sysMenuIdParam) {
        SysMenu sysMenu = this.getById(sysMenuIdParam.getId());
        if (ObjectUtil.isEmpty(sysMenu)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysMenu;
    }

    @Override
    public List<SysMenu> menuTree() {
        List<SysMenu> menuList = this.list();
        if (ObjectUtil.isEmpty(menuList)) {
            return List.of();
        }

        // 使用Map存储菜单，key为菜单ID，value为菜单对象
        Map<String, SysMenu> menuMap = menuList.stream()
                .collect(Collectors.toMap(SysMenu::getId, Function.identity()));

        List<SysMenu> rootMenus = new ArrayList<>();

        // 构建树形结构
        for (SysMenu menu : menuList) {
            String pid = menu.getPid();

            if ("0".equals(pid) || pid == null) {
                // 顶级菜单
                rootMenus.add(menu);
            } else {
                // 子菜单，找到父菜单并添加到children中
                SysMenu parentMenu = menuMap.get(pid);
                if (parentMenu != null) {
                    if (parentMenu.getChildren() == null) {
                        parentMenu.setChildren(new ArrayList<>());
                    }
                    parentMenu.getChildren().add(menu);
                }
            }
        }

        // 对顶级菜单和子菜单进行排序
        rootMenus.sort(Comparator.comparingInt(SysMenu::getSort));

        // 递归对子菜单排序
        for (SysMenu menu : menuList) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.getChildren().sort(Comparator.comparingInt(SysMenu::getSort));
            }
        }

        return rootMenus;
    }

    @Override
    public List<SysMenu> authMenu() {
        List<String> accessibleMenuIds = dataScopeUtil.getDataScopeContext().getAccessibleMenuIds();
        if (accessibleMenuIds.isEmpty()) {
            return List.of();
        }
        return this.listByIds(accessibleMenuIds);
    }

    @Override
    public void assignMenuPermission(SysMenuPermissionParam sysMenuPermissionParam) {
        SysMenu byId = this.getById(sysMenuPermissionParam.getId());
        byId.setExJson(sysMenuPermissionParam.getPermissions());
        this.updateById(byId);
    }
}