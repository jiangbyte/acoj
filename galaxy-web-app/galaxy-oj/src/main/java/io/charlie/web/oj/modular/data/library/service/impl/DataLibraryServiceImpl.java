package io.charlie.web.oj.modular.data.library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GalaxyStringUtil;
import io.charlie.web.oj.context.DataScopeContext;
import io.charlie.web.oj.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.*;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.library.entity.LibraryBatchCount;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import io.charlie.web.oj.modular.sys.user.utils.SysUserBuildUtil;
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
 * @description 提交样本库 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLibraryServiceImpl extends ServiceImpl<DataLibraryMapper, DataLibrary> implements DataLibraryService {
    private final SysUserBuildUtil sysUserBuildUtil;
    private final DataScopeUtil dataScopeUtil;

    private final SysUserService sysUserService;

    private final SysGroupMapper sysGroupMapper;

    @Override
    @DS("slave")
    public Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam) {
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();
        queryWrapper.lambda().orderByDesc(DataLibrary::getCreateTime);
        if (ObjectUtil.isAllNotEmpty(dataLibraryPageParam.getSortField(), dataLibraryPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataLibraryPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataLibraryPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataLibraryPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataLibraryPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataLibraryPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataLibraryAddParam dataLibraryAddParam) {
        DataLibrary bean = BeanUtil.toBean(dataLibraryAddParam, DataLibrary.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataLibraryEditParam dataLibraryEditParam) {
        if (!this.exists(new LambdaQueryWrapper<DataLibrary>().eq(DataLibrary::getId, dataLibraryEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DataLibrary bean = BeanUtil.toBean(dataLibraryEditParam, DataLibrary.class);
        BeanUtil.copyProperties(dataLibraryEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<DataLibraryIdParam> dataLibraryIdParamList) {
        if (ObjectUtil.isEmpty(dataLibraryIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(dataLibraryIdParamList, DataLibraryIdParam::getId));
    }

    @Override
    @DS("slave")
    public DataLibrary detail(DataLibraryIdParam dataLibraryIdParam) {
        DataLibrary dataLibrary = this.getById(dataLibraryIdParam.getId());
        if (ObjectUtil.isEmpty(dataLibrary)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return dataLibrary;
    }

    @Override
    public List<SysGroup> getLibraryUserGroupList(String keyword) {
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

        // 调整 exists 条件
//        queryWrapper.exists(
//                "SELECT 1 FROM data_library dl " +
//                        "JOIN sys_user su ON dl.user_id = su.id " +
//                        "WHERE su.group_id = sys_group.id " +
//                        "AND dl.is_set = 1 " +
//                        "AND dl.set_id IS NOT NULL " +
//                        "AND dl.user_id IS NOT NULL"  // 明确 user_id 不为空
//        );

        // 查询所有符合条件的用户组
        List<SysGroup> allGroups = sysGroupMapper.selectList(queryWrapper);

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
    public Page<SysUser> getLibraryUserPage(DataLibraryUserPageParam dataLibraryUserPageParam) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        queryWrapper.lambda().in(SysUser::getGroupId, accessibleGroupIds);

        // 统一的 EXISTS 条件构建
        StringBuilder existsSql = new StringBuilder("SELECT 1 FROM data_library dl WHERE dl.user_id = sys_user.id AND dl.is_set = 1 ");

        boolean hasCondition = false;

        // 添加 problemIds 条件
        if (ObjectUtil.isNotEmpty(dataLibraryUserPageParam.getProblemIds())) {
            existsSql.append(" AND dl.problem_id IN (")
                    .append(dataLibraryUserPageParam.getProblemIds().stream()
                            .map(id -> "'" + id + "'")
                            .collect(Collectors.joining(",")))
                    .append(")");
            hasCondition = true;
        }

        // 添加 setId 条件
        if (GalaxyStringUtil.isNotEmpty(dataLibraryUserPageParam.getSetId())) {
            existsSql.append(" AND dl.set_id = '").append(dataLibraryUserPageParam.getSetId()).append("'");
            hasCondition = true;
        }

        // 添加 language 条件
        if (GalaxyStringUtil.isNotEmpty(dataLibraryUserPageParam.getLanguage())) {
            if (!dataLibraryUserPageParam.getLanguage().equals("null")) {
                existsSql.append(" AND dl.language = '").append(dataLibraryUserPageParam.getLanguage()).append("'");
                hasCondition = true;
            }
        }

        // 只有当有具体条件时才添加 EXISTS，或者保持原样查询所有有 data_library 记录的用户
        if (hasCondition) {
            queryWrapper.exists(existsSql.toString());
        } else {
            // 如果没有特定条件，但想查询所有在 data_library 中有记录的用户
            queryWrapper.exists("SELECT 1 FROM data_library dl WHERE dl.user_id = sys_user.id AND dl.is_set = 1 ");
        }

        if (GalaxyStringUtil.isNotEmpty(dataLibraryUserPageParam.getGroupId())) {
            queryWrapper.lambda().eq(SysUser::getGroupId, dataLibraryUserPageParam.getGroupId());
        }
        if (ObjectUtil.isAllNotEmpty(dataLibraryUserPageParam.getSortField(), dataLibraryUserPageParam.getSortOrder()) && ISortOrderEnum.isValid(dataLibraryUserPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    dataLibraryUserPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(dataLibraryUserPageParam.getSortField()));
        }

        Page<SysUser> page = sysUserService.page(CommonPageRequest.Page(
                        Optional.ofNullable(dataLibraryUserPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(dataLibraryUserPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        sysUserBuildUtil.buildUsers(page.getRecords());
        return page;
    }

    @Override
    public QueryWrapper<DataLibrary> queryLibrary(BatchLibraryQueryParam libraryQueryParam) {
        log.debug("开始执行批量查询 {}", JSONUtil.toJsonStr(libraryQueryParam));
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();

        queryWrapper.lambda().eq(DataLibrary::getIsSet, Boolean.TRUE);
        queryWrapper.lambda().eq(DataLibrary::getSetId, libraryQueryParam.getSetId());

        if (ObjectUtil.isNotEmpty(libraryQueryParam.getProblemId())) {
            queryWrapper.lambda().eq(DataLibrary::getProblemId, libraryQueryParam.getProblemId());
        }

        if (ObjectUtil.isNotEmpty(libraryQueryParam.getLanguage())) {
            queryWrapper.lambda().eq(DataLibrary::getLanguage, libraryQueryParam.getLanguage().toLowerCase());
        }

        // 时间范围筛选
        if (ObjectUtil.isAllNotEmpty(libraryQueryParam.getStartTime(), libraryQueryParam.getEndTime())) {
            queryWrapper.lambda().between(DataLibrary::getUpdateTime, libraryQueryParam.getStartTime(), libraryQueryParam.getEndTime());
        }

        // 两两对比 PAIR_BY_PAIR，默认情况，不做处理
        if ("PAIR_BY_PAIR".equals(libraryQueryParam.getCompareMode())) {
            // 代码筛选
            if (ObjectUtil.isNotEmpty(libraryQueryParam.getCodeFilter())) {
                // 代码筛选时间窗口（分钟）
                if ("TIME_WINDOW".equals(libraryQueryParam.getCodeFilter())) {
                    queryWrapper.lambda().ge(DataLibrary::getUpdateTime, DateUtil.offsetMinute(new Date(), -libraryQueryParam.getCodeFilterTimeWindow()));
                }
            }
        } else if ("GROUP_BY_GROUP".equals(libraryQueryParam.getCompareMode())) {
            // 组内对比 GROUP_BY_GROUP
            if (ObjectUtil.isNotEmpty(libraryQueryParam.getGroupId())) {
                // 使用 EXISTS 子查询的方式（性能更好）
//                queryWrapper.exists("SELECT 1 FROM sys_user u WHERE u.id = user_id AND u.group_id = " + libraryQueryParam.getGroupId());
                queryWrapper.exists("SELECT 1 FROM sys_user u WHERE u.id = user_id AND u.group_id = {0}",
                        libraryQueryParam.getGroupId());
            }
        } else if ("MULTI_BY_MULTI".equals(libraryQueryParam.getCompareMode())) {
            // 多人对比 MULTI_BY_MULTI
            if (ObjectUtil.isNotEmpty(libraryQueryParam.getUserIds())) {
                queryWrapper.lambda().in(DataLibrary::getUserId, libraryQueryParam.getUserIds());
            }
        }

        return queryWrapper;
    }

    @Override
    public LibraryBatchCount batchQuery(BatchLibraryQueryParam libraryQueryParam) {
        QueryWrapper<DataLibrary> dataLibraryQueryWrapper = this.queryLibrary(libraryQueryParam);
        long count = this.count(dataLibraryQueryWrapper);

        long checkCount = count > 200 ? 200 : count;

        LibraryBatchCount batchCount = new LibraryBatchCount();
        batchCount.setTotalCount(String.valueOf(count)); // 获取有效记录代码总数量
        batchCount.setCheckCount(String.valueOf(checkCount));

        // 组合数计算（基于实际检查数量）
        long compareCount = checkCount > 1 ? checkCount * (checkCount - 1) / 2 : 0;
        batchCount.setCompareCount(String.valueOf(compareCount));

        return batchCount;
    }

    @Override
    public List<String> libraryIds(BatchLibraryQueryParam libraryQueryParam) {
        QueryWrapper<DataLibrary> dataLibraryQueryWrapper = this.queryLibrary(libraryQueryParam);
        List<DataLibrary> list = this.list(dataLibraryQueryWrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            return list.stream()
                    .map(DataLibrary::getId)
                    .distinct()
                    .toList();
        }

        return List.of();
    }
}