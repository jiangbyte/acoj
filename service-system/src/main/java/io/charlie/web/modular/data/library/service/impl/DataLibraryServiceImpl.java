package io.charlie.web.modular.data.library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.cores.utils.str.GalaxyStringUtil;
import io.charlie.web.context.DataScopeContext;
import io.charlie.web.context.DataScopeUtil;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.param.*;
import io.charlie.web.modular.data.library.param.*;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.modular.data.library.service.DataLibraryService;
import io.charlie.cores.enums.ISortOrderEnum;
import io.charlie.cores.exception.BusinessException;
import io.charlie.cores.pojo.CommonPageRequest;
import io.charlie.cores.result.ResultCode;
import io.charlie.web.modular.data.library.entity.LibraryBatchCount;
import io.charlie.web.modular.data.problem.entity.DataProblem;
import io.charlie.web.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.modular.sys.group.entity.SysGroup;
import io.charlie.web.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.modular.sys.user.entity.SysUser;
import io.charlie.web.modular.sys.user.service.SysUserService;
import io.charlie.web.modular.sys.user.utils.SysUserBuildUtil;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final DataProblemMapper dataProblemMapper;
    private final TransService transService;

    @Override
    @DS("slave")
    public Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam) {
        // 1. 组装过滤条件（user/problem/submit）
        // 2. 排序后分页查询，避免无条件扫全表
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();
        if (StrUtil.isNotBlank(dataLibraryPageParam.getUserId())) {
            queryWrapper.lambda().eq(DataLibrary::getUserId, dataLibraryPageParam.getUserId());
        }
        if (StrUtil.isNotBlank(dataLibraryPageParam.getProblemId())) {
            queryWrapper.lambda().eq(DataLibrary::getProblemId, dataLibraryPageParam.getProblemId());
        }
        if (StrUtil.isNotBlank(dataLibraryPageParam.getSubmitId())) {
            queryWrapper.lambda().eq(DataLibrary::getSubmitId, dataLibraryPageParam.getSubmitId());
        }
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
        // 1. 权限组为空直接空页，避免无 scope 扫全用户
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();

        List<String> accessibleGroupIds = dataScopeUtil.getDataScopeContext().getAccessibleGroupIds();
        if (accessibleGroupIds.isEmpty()) {
            return new Page<>();
        }

        queryWrapper.lambda().in(SysUser::getGroupId, accessibleGroupIds);

        // 2. 与筛查 queryLibrary 同源：按 moduleType/moduleId/problemId/language 过滤 library 用户
        String moduleType = StrUtil.blankToDefault(dataLibraryUserPageParam.getModuleType(), "SET");
        String moduleId = StrUtil.blankToDefault(dataLibraryUserPageParam.getModuleId(), dataLibraryUserPageParam.getSetId());
        String problemId = dataLibraryUserPageParam.getProblemId();
        if (StrUtil.isBlank(problemId) && ObjectUtil.isNotEmpty(dataLibraryUserPageParam.getProblemIds())) {
            problemId = dataLibraryUserPageParam.getProblemIds().getFirst();
        }

        StringBuilder existsSql = new StringBuilder(
                "SELECT 1 FROM data_library dl WHERE dl.user_id = sys_user.id AND dl.deleted = 0");
        existsSql.append(" AND dl.module_type = '").append(escapeSqlLiteral(moduleType)).append("'");
        if (StrUtil.isNotBlank(moduleId)) {
            existsSql.append(" AND dl.module_id = '").append(escapeSqlLiteral(moduleId)).append("'");
        }
        if (StrUtil.isNotBlank(problemId)) {
            existsSql.append(" AND dl.problem_id = '").append(escapeSqlLiteral(problemId)).append("'");
        }
        if (GalaxyStringUtil.isNotEmpty(dataLibraryUserPageParam.getLanguage())
                && !"null".equals(dataLibraryUserPageParam.getLanguage())) {
            existsSql.append(" AND LOWER(dl.language) = '")
                    .append(escapeSqlLiteral(dataLibraryUserPageParam.getLanguage().toLowerCase()))
                    .append("'");
        }
        // 有效样本：有代码且有 token，与 batch 可比对集合一致
        existsSql.append(" AND dl.code IS NOT NULL AND dl.code <> ''");
        existsSql.append(" AND dl.code_token IS NOT NULL AND dl.code_token <> 'null' AND dl.code_token <> '[]'");
        queryWrapper.exists(existsSql.toString());

        if (GalaxyStringUtil.isNotEmpty(dataLibraryUserPageParam.getGroupId())) {
            queryWrapper.lambda().eq(SysUser::getGroupId, dataLibraryUserPageParam.getGroupId());
        }
        if (ObjectUtil.isAllNotEmpty(dataLibraryUserPageParam.getSortField(), dataLibraryUserPageParam.getSortOrder())
                && ISortOrderEnum.isValid(dataLibraryUserPageParam.getSortOrder())) {
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

    /** 与前端 / Jackson 一致：按 Asia/Shanghai 墙钟解释时间戳，再与 DATETIME 比较 */
    private static final ZoneId FRONTEND_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter MYSQL_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 转义 SQL 字符串字面量中的单引号，避免拼接 exists 时注入。
     */
    private static String escapeSqlLiteral(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * 将前端传入的 epoch Date 格式化为上海时区墙钟字符串，与库内 DATETIME 字面对齐。
     */
    private static String toFrontendDateTime(Date date) {
        return date.toInstant().atZone(FRONTEND_ZONE).format(MYSQL_DATETIME);
    }

    @Override
    public QueryWrapper<DataLibrary> queryLibrary(BatchLibraryQueryParam libraryQueryParam) {
        // 1. 基础范围：模块 + 题目 + 语言
        log.debug("开始执行批量查询 {}", JSONUtil.toJsonStr(libraryQueryParam));
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();

        queryWrapper.lambda().eq(DataLibrary::getModuleType, libraryQueryParam.getModuleType());
        queryWrapper.lambda().eq(DataLibrary::getModuleId, libraryQueryParam.getModuleId());

        if (ObjectUtil.isNotEmpty(libraryQueryParam.getProblemId())) {
            queryWrapper.lambda().eq(DataLibrary::getProblemId, libraryQueryParam.getProblemId());
        }

        if (ObjectUtil.isNotEmpty(libraryQueryParam.getLanguage())) {
            queryWrapper.lambda().eq(DataLibrary::getLanguage, libraryQueryParam.getLanguage().toLowerCase());
        }

        // 2. 有效可比对样本：有源码且有 token（与 Go 跳过空 token 行为对齐）
        queryWrapper.lambda().isNotNull(DataLibrary::getCode).ne(DataLibrary::getCode, "");
        queryWrapper.apply("code_token IS NOT NULL AND code_token <> 'null' AND code_token <> '[]'");

        // 3. 时间范围：按前端时间戳→Asia/Shanghai 墙钟，直接比 DATETIME，避免 UNIX_TIMESTAMP 受会话时区影响
        //    手写 apply：需 IFNULL(submit_time, create_time)，Lambda 难以表达
        if (ObjectUtil.isAllNotEmpty(libraryQueryParam.getStartTime(), libraryQueryParam.getEndTime())) {
            String start = toFrontendDateTime(libraryQueryParam.getStartTime());
            String end = toFrontendDateTime(libraryQueryParam.getEndTime());
            queryWrapper.apply(
                    "IFNULL(submit_time, create_time) >= {0} AND IFNULL(submit_time, create_time) <= {1}",
                    start, end);
        }

        // 4. 对比模式附加条件
        if ("PAIR_BY_PAIR".equals(libraryQueryParam.getCompareMode())) {
            if ("TIME_WINDOW".equals(libraryQueryParam.getCodeFilter())
                    && libraryQueryParam.getCodeFilterTimeWindow() != null
                    && libraryQueryParam.getCodeFilterTimeWindow() > 0) {
                long thresholdMs = System.currentTimeMillis()
                        - libraryQueryParam.getCodeFilterTimeWindow() * 60L * 1000L;
                String threshold = toFrontendDateTime(new Date(thresholdMs));
                queryWrapper.apply("IFNULL(submit_time, create_time) >= {0}", threshold);
            }
            // VALID_SUBMIT：已由步骤 2 的有效样本条件覆盖
        } else if ("GROUP_BY_GROUP".equals(libraryQueryParam.getCompareMode())) {
            if (ObjectUtil.isNotEmpty(libraryQueryParam.getGroupId())) {
                queryWrapper.exists("SELECT 1 FROM sys_user u WHERE u.id = user_id AND u.group_id = {0}",
                        libraryQueryParam.getGroupId());
            }
        } else if ("MULTI_BY_MULTI".equals(libraryQueryParam.getCompareMode())) {
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
        batchCount.setTotalCount(String.valueOf(count));
        batchCount.setCheckCount(String.valueOf(checkCount));

        long compareCount = checkCount > 1 ? checkCount * (checkCount - 1) / 2 : 0;
        batchCount.setCompareCount(String.valueOf(compareCount));

        return batchCount;
    }

    @Override
    public List<String> libraryIds(BatchLibraryQueryParam libraryQueryParam) {
        // 1. 与 Preview 同一过滤；按提交时间倒序截断，保证超 200 时集合可复现
        QueryWrapper<DataLibrary> dataLibraryQueryWrapper = this.queryLibrary(libraryQueryParam);
        dataLibraryQueryWrapper.select("id");
        dataLibraryQueryWrapper.last("ORDER BY IFNULL(submit_time, create_time) DESC, id DESC LIMIT 200");

        List<DataLibrary> list = this.list(dataLibraryQueryWrapper);
        if (ObjectUtil.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .map(DataLibrary::getId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
    }

    @Override
    public List<DataProblem> getLibraryProblemList(String moduleType, String moduleId) {
        // 1. 只取 distinct problem_id，避免大模块全量拉 library
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();
        queryWrapper.select("DISTINCT problem_id");
        queryWrapper.lambda()
                .eq(DataLibrary::getModuleType, moduleType)
                .eq(DataLibrary::getModuleId, moduleId)
                .isNotNull(DataLibrary::getProblemId)
                .ne(DataLibrary::getProblemId, "");
        List<DataLibrary> list = this.list(queryWrapper);
        if (ObjectUtil.isEmpty(list)) {
            return List.of();
        }

        List<String> stringList = list.stream()
                .map(DataLibrary::getProblemId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (ObjectUtil.isEmpty(stringList)) {
            return List.of();
        }
        List<DataProblem> dataProblems = dataProblemMapper.selectByIds(stringList);
        transService.transBatch(dataProblems);
        return dataProblems;
    }
}