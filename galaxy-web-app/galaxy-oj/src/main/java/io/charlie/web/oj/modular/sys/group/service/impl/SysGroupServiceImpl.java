package io.charlie.web.oj.modular.sys.group.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.context.DataScopeContext;
import io.charlie.web.oj.modular.context.DataScopeUtil;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.param.*;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.group.service.SysGroupService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.entity.SysRole;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 用户组表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysGroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements SysGroupService {
    private final DataScopeUtil dataScopeUtil;
    private final SysUserMapper sysUserMapper;

    @Override
    @DS("slave")
    public Page<SysGroup> page(SysGroupPageParam sysGroupPageParam) {
        QueryWrapper<SysGroup> queryWrapper = new QueryWrapper<SysGroup>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        queryWrapper.lambda().in(SysGroup::getId, accessibleGroupIds);

        // 关键字
        if (ObjectUtil.isNotEmpty(sysGroupPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysGroup::getName, sysGroupPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysGroupPageParam.getSortField(), sysGroupPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysGroupPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysGroupPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysGroupPageParam.getSortField()));
        }
        queryWrapper.lambda().orderByAsc(SysGroup::getSort);

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysGroupPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysGroupPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysGroupAddParam sysGroupAddParam) {
        SysGroup bean = BeanUtil.toBean(sysGroupAddParam, SysGroup.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysGroupEditParam sysGroupEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysGroup>().eq(SysGroup::getId, sysGroupEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysGroup bean = BeanUtil.toBean(sysGroupEditParam, SysGroup.class);
        BeanUtil.copyProperties(sysGroupEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysGroupIdParam> sysGroupIdParamList) {
        if (ObjectUtil.isEmpty(sysGroupIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<String> stringList = CollStreamUtil.toList(sysGroupIdParamList, SysGroupIdParam::getId);
        if (ObjectUtil.isNotEmpty(stringList)) {
            // 先查看有没有用户授权到
            boolean exists = sysUserMapper.exists(new LambdaQueryWrapper<SysUser>()
                    .in(SysUser::getGroupId, stringList)
            );
            if (exists) {
                throw new BusinessException("用户组正在被用户使用，删除前请将组内用户迁移至其他用户组");
            }
        }

        this.removeByIds(stringList);
    }

    @Override
    @DS("slave")
    public SysGroup detail(SysGroupIdParam sysGroupIdParam) {
        SysGroup sysGroup = this.getById(sysGroupIdParam.getId());
        if (ObjectUtil.isEmpty(sysGroup)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysGroup;
    }

    @Override
    @DS("slave")
    public List<LabelOption<String>> options(SysGroupOptionParam sysGroupOptionParam) {
        QueryWrapper<SysGroup> queryWrapper = new QueryWrapper<SysGroup>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysGroupOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysGroup::getName, sysGroupOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(sysGroup -> new LabelOption<>(sysGroup.getId(), sysGroup.getName())).toList();
    }

    @Override
    @DS("slave")
    public List<SysGroup> authTreeGroup(String keyword) {
        DataScopeContext dataScopeContext = dataScopeUtil.getDataScopeContext();
        List<String> accessibleGroupIds = dataScopeContext.getAccessibleGroupIds(); // 可访问的用户组ID列表
        // 构建查询条件
        LambdaQueryWrapper<SysGroup> queryWrapper = new LambdaQueryWrapper<SysGroup>()
                .orderByAsc(SysGroup::getSort);

        // 添加关键字过滤
        if (ObjectUtil.isNotEmpty(keyword)) {
            queryWrapper.like(SysGroup::getName, keyword);
        }

        // 添加权限过滤 - 只查询可访问的用户组
        if (ObjectUtil.isNotEmpty(accessibleGroupIds)) {
            queryWrapper.in(SysGroup::getId, accessibleGroupIds);
        }

        // 查询所有符合条件的用户组
        List<SysGroup> allGroups = this.list(queryWrapper);

        // 如果没有数据直接返回空列表
        if (CollectionUtil.isEmpty(allGroups)) {
            return List.of();
        }

        // 使用Map构建树结构
        Map<String, SysGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(SysGroup::getId, group -> group));

        // 存储根节点
        List<SysGroup> roots = new ArrayList<>();

        // 构建树形结构
        for (SysGroup group : allGroups) {
            String parentId = group.getParentId();

            // 如果父ID为空或者是自己（防止循环），或者父节点不存在于当前列表中，则作为根节点
            if (ObjectUtil.isEmpty(parentId) ||
                    group.getId().equals(parentId) ||
                    !groupMap.containsKey(parentId)) {
                roots.add(group);
            } else {
                // 找到父组并添加到其children中
                SysGroup parent = groupMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(group);
                }
            }
        }

        return roots;
    }


    @Override
    @DS("slave")
    public List<SysGroup> subGroup(String groupId) {
        // 获得某个用户组下的所有用户组（包含该用户组本身）（扁平列表）
        // 如果传入为 null 或者 "" 则返回空
        if (ObjectUtil.isEmpty(groupId)) {
            return List.of();
        }

        // 查询指定用户组
        SysGroup currentGroup = this.getById(groupId);
        if (currentGroup == null) {
            return List.of();
        }

        // 查询所有用户组
        List<SysGroup> allGroups = this.list(new LambdaQueryWrapper<SysGroup>()
                .orderByAsc(SysGroup::getSort));

        // 获取当前组及其所有子组的扁平列表
        return getFlatSubGroups(allGroups, groupId, true);
    }

    public List<SysGroup> subGroup(String groupId, boolean includeSelf) {
        // 获得某个用户组下的所有用户组（扁平列表）
        // 如果传入为 null 或者 "" 则返回空
        if (ObjectUtil.isEmpty(groupId)) {
            return List.of();
        }

        // 查询所有用户组
        List<SysGroup> allGroups = this.list(new LambdaQueryWrapper<SysGroup>()
                .orderByAsc(SysGroup::getSort));

        // 检查指定的组是否存在
        boolean groupExists = allGroups.stream()
                .anyMatch(group -> groupId.equals(group.getId()));

        if (!groupExists) {
            return List.of();
        }

        // 获取当前组及其所有子组的扁平列表
        return getFlatSubGroups(allGroups, groupId, includeSelf);
    }

    /**
     * 获取用户组的扁平子组列表
     *
     * @param allGroups   所有用户组列表
     * @param parentId    父组ID
     * @param includeSelf 是否包含自身
     * @return 扁平的用户组列表
     */
    private List<SysGroup> getFlatSubGroups(List<SysGroup> allGroups, String parentId, boolean includeSelf) {
        List<SysGroup> result = new ArrayList<>();

        // 如果包含自身，先添加当前组
        if (includeSelf) {
            SysGroup currentGroup = allGroups.stream()
                    .filter(group -> parentId.equals(group.getId()))
                    .findFirst()
                    .orElse(null);
            if (currentGroup != null) {
                result.add(currentGroup);
            }
        }

        // 递归获取所有子组
        getSubGroupsRecursive(allGroups, parentId, result);

        return result;
    }

    /**
     * 递归获取所有子组（不包含自身）
     */
    private void getSubGroupsRecursive(List<SysGroup> allGroups, String parentId, List<SysGroup> result) {
        // 查找直接子组
        List<SysGroup> directChildren = allGroups.stream()
                .filter(group -> parentId.equals(group.getParentId()))
                .toList();

        // 添加直接子组
        result.addAll(directChildren);

        // 递归获取子组的子组
        for (SysGroup child : directChildren) {
            getSubGroupsRecursive(allGroups, child.getId(), result);
        }
    }


    /**
     * 可选：添加排序方法，确保树结构有序
     */
    private void sortTree(List<SysGroup> tree) {
        if (tree == null || tree.isEmpty()) {
            return;
        }

        // 按sort字段排序
        tree.sort((g1, g2) -> {
            Byte sort1 = g1.getSort() != null ? g1.getSort() : 0;
            Byte sort2 = g2.getSort() != null ? g2.getSort() : 0;
            return sort1.compareTo(sort2);
        });

        // 递归排序子节点
        for (SysGroup group : tree) {
            if (group.getChildren() != null && !group.getChildren().isEmpty()) {
                sortTree(group.getChildren());
            }
        }
    }

}