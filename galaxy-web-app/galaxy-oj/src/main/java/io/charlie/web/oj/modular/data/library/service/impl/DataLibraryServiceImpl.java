package io.charlie.web.oj.modular.data.library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.utils.str.GaStringUtil;
import io.charlie.web.oj.modular.context.DataScopeUtil;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.*;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.library.entity.LibraryBatchCount;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import io.charlie.web.oj.modular.sys.user.utils.SysUserBuildUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private final RedissonClient redissonClient;

    private final SysUserBuildUtil sysUserBuildUtil;
    private final DataScopeUtil dataScopeUtil;

    private final SysUserService sysUserService;

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

    //    @Override
//    public void addLibrary(DataSubmit submit) {
//        // 构建查询条件
//        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
//                .eq(DataLibrary::getUserId, submit.getUserId())
//                .eq(DataLibrary::getLanguage, submit.getLanguage())
//                .eq(DataLibrary::getProblemId, submit.getProblemId());
//
//        // 如果是set模式，需要额外匹配setId
//        if (submit.getIsSet()) {
//            queryWrapper.eq(DataLibrary::getSetId, submit.getSetId());
//            queryWrapper.eq(DataLibrary::getIsSet, Boolean.TRUE);
//        } else {
//            queryWrapper.eq(DataLibrary::getIsSet, Boolean.FALSE);
//        }
//
//        // 查找已存在的记录
//        DataLibrary library = this.getOne(queryWrapper);
//
//        if (library != null) {
//            log.info("存在记录，执行更新");
//            // 更新现有记录
//            library.setSubmitId(submit.getId());
//            library.setSubmitTime(submit.getCreateTime());
//            library.setCode(submit.getCode());
//            library.setCodeLength(submit.getCodeLength());
//            this.updateById(library);
//        } else {
//            log.info("不存在记录，创建新记录");
//            // 创建新记录
//            library = new DataLibrary();
//            library.setUserId(submit.getUserId());
//            library.setSetId(submit.getIsSet() ? submit.getSetId() : null);
//            library.setProblemId(submit.getProblemId());
//            library.setLanguage(submit.getLanguage());
//            library.setSubmitId(submit.getId());
//            library.setSubmitTime(submit.getCreateTime());
//            library.setCode(submit.getCode());
//            library.setCodeLength(submit.getCodeLength());
//            library.setIsSet(submit.getIsSet());
//            library.setAccessCount(0);
//
//            this.save(library);
//        }
//    }
    @Override
    public void addLibrary(DataSubmit submit) {

        String lockKey = buildLockKey(submit);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待5秒，锁超时时间30秒
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("获取分布式锁失败");
            }

            try {
                // 在锁保护下执行原有逻辑
                doAddLibrary(submit);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("操作被中断", e);
        }
    }

    private void doAddLibrary(DataSubmit submit) {
        // 原有的业务逻辑
        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getUserId, submit.getUserId())
                .eq(DataLibrary::getLanguage, submit.getLanguage())
                .eq(DataLibrary::getProblemId, submit.getProblemId())
                .eq(DataLibrary::getIsSet, submit.getIsSet());

        if (submit.getIsSet()) {
            queryWrapper.eq(DataLibrary::getSetId, submit.getSetId());
        }

        DataLibrary library = this.getOne(queryWrapper);

        if (library != null) {
            log.info("存在记录，执行更新");
            library.setSubmitId(submit.getId());
            library.setSubmitTime(submit.getCreateTime());
            library.setCodeLength(submit.getCodeLength());
            if (!submit.getCode().equals(library.getCode())) {
                library.setCode(submit.getCode());
                library.setAccessCount(0);// 重置访问次数
            }
            this.updateById(library);
        } else {
            log.info("不存在记录，创建新记录");
            library = new DataLibrary();
            library.setUserId(submit.getUserId());
            library.setSetId(submit.getIsSet() ? submit.getSetId() : null);
            library.setProblemId(submit.getProblemId());
            library.setLanguage(submit.getLanguage());
            library.setSubmitId(submit.getId());
            library.setSubmitTime(submit.getCreateTime());
            library.setCode(submit.getCode());
            library.setCodeLength(submit.getCodeLength());
            library.setIsSet(submit.getIsSet());
            library.setAccessCount(0);
            this.save(library);
        }
    }

    private String buildLockKey(DataSubmit submit) {
        if (submit.getIsSet()) {
            return String.format("library:lock:%s:%s:%s:%s",
                    submit.getUserId(), submit.getLanguage(),
                    submit.getProblemId(), submit.getSetId());
        } else {
            return String.format("library:lock:%s:%s:%s",
                    submit.getUserId(), submit.getLanguage(), submit.getProblemId());
        }
    }

    private LambdaQueryWrapper<DataLibrary> buildSampleQuery(
            boolean isSet,
            String language,
            List<String> problemIds,
            List<String> setIds,
            List<String> userIds
    ) {

        LambdaQueryWrapper<DataLibrary> query = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getIsSet, isSet);

        if (GaStringUtil.isNotEmpty(language)) {
            query.eq(DataLibrary::getLanguage, language);
        }
        if (ObjectUtil.isNotEmpty(problemIds)) {
            query.in(DataLibrary::getProblemId, problemIds);
        }
        if (ObjectUtil.isNotEmpty(setIds)) {
            query.in(DataLibrary::getSetId, setIds);
        }
        if (ObjectUtil.isNotEmpty(userIds)) {
            query.in(DataLibrary::getUserId, userIds);
        }

        // 按时间降序，优先更新时间
        query.orderByDesc(DataLibrary::getUpdateTime);
        query.orderByDesc(DataLibrary::getCreateTime);

        return query;
    }

    @Override
    @DS("slave")
    public <R> List<R> processCodeLibrariesInBatches(boolean isSet, String language,
                                                     List<String> problemIds, List<String> setIds,
                                                     List<String> userIds, int batchSize,
                                                     String filterProblemId, String filterSetId,
                                                     String filterUserId,
                                                     int maxBatches, // 最大批次数限制
                                                     Function<List<DataLibrary>, List<R>> processor) {

        LambdaQueryWrapper<DataLibrary> queryWrapper = buildSampleQuery(isSet, language, problemIds, setIds, userIds);

        // 添加过滤条件
        if (filterProblemId != null) {
            log.info("添加 filterProblemId 过滤条件：{}", filterProblemId);
            queryWrapper.ne(DataLibrary::getProblemId, filterProblemId);
        }
        if (filterSetId != null) {
            log.info("添加 filterSetId 过滤条件：{}", filterSetId);
            queryWrapper.ne(DataLibrary::getSetId, filterSetId);
        }
        if (filterUserId != null) {
            log.info("添加 filterUserId 过滤条件：{}", filterUserId);
            queryWrapper.ne(DataLibrary::getUserId, filterUserId);
        }

        List<R> allResults = new ArrayList<>();

        // 如果批次数为0，则一次性处理所有数据
        if (batchSize <= 0) {
            log.info("处理所有数据 批次大小 {}", batchSize);
            List<DataLibrary> allData = this.list(queryWrapper);
            if (allData != null && !allData.isEmpty()) {
                // 先增加访问量
                incrementAccessCount(allData);
                // 然后执行处理逻辑并收集结果
                List<R> results = processor.apply(allData);
                if (results != null) {
                    allResults.addAll(results);
                }
            }
            return allResults; // 直接返回全量结果
        }

        // 正常分批处理
        long total = this.count(queryWrapper);
        log.info("总记录数：{}", total);
        long pages = (total + batchSize - 1) / batchSize;
        log.info("总页数：{}", pages);

        // 如果设置了最大批次数，则限制处理的页数
        if (maxBatches > 0) {
            pages = Math.min(pages, maxBatches);
            log.info("限制最大批次数：{}，实际处理页数：{}", maxBatches, pages);
        } else {
            log.info("总页数：{}", pages);
        }

        for (long current = 1; current <= pages; current++) {
            log.info("处理分页 {} 批次大小 {}", current, batchSize);
            Page<DataLibrary> page = this.page(new Page<>(current, batchSize), queryWrapper);
            if (page.getRecords() != null && !page.getRecords().isEmpty()) {
                List<DataLibrary> batch = page.getRecords();

                // 增加访问量
                incrementAccessCount(batch);

                // 然后执行处理逻辑并收集结果
                List<R> batchResults = processor.apply(batch);
                if (batchResults != null) {
                    allResults.addAll(batchResults);
                }
            }
        }

        return allResults;
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
        StringBuilder existsSql = new StringBuilder("SELECT 1 FROM data_library dl WHERE dl.user_id = sys_user.id");

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
        if (GaStringUtil.isNotEmpty(dataLibraryUserPageParam.getSetId())) {
            existsSql.append(" AND dl.set_id = '").append(dataLibraryUserPageParam.getSetId()).append("'");
            hasCondition = true;
        }

        // 添加 language 条件
        if (GaStringUtil.isNotEmpty(dataLibraryUserPageParam.getLanguage())) {
            existsSql.append(" AND dl.language = '").append(dataLibraryUserPageParam.getLanguage()).append("'");
            hasCondition = true;
        }

        // 只有当有具体条件时才添加 EXISTS，或者保持原样查询所有有 data_library 记录的用户
        if (hasCondition) {
            queryWrapper.exists(existsSql.toString());
        } else {
            // 如果没有特定条件，但想查询所有在 data_library 中有记录的用户
            queryWrapper.exists("SELECT 1 FROM data_library dl WHERE dl.user_id = sys_user.id");
        }

        if (ObjectUtil.isNotEmpty(dataLibraryUserPageParam.getType())) {
            String type = dataLibraryUserPageParam.getType();
            if (type.equals("username")) {
                queryWrapper.lambda()
                        .like(SysUser::getUsername, dataLibraryUserPageParam.getKeyword());
            } else if (type.equals("nickname")) {
                queryWrapper.lambda()
                        .like(SysUser::getNickname, dataLibraryUserPageParam.getKeyword());
            } else if (type.equals("email")) {
                queryWrapper.lambda()
                        .like(SysUser::getEmail, dataLibraryUserPageParam.getKeyword());
            } else if (type.equals("telephone")) {
                queryWrapper.lambda()
                        .like(SysUser::getTelephone, dataLibraryUserPageParam.getKeyword());
            } else if (type.equals("studentNumber")) {
                queryWrapper.lambda()
                        .like(SysUser::getStudentNumber, dataLibraryUserPageParam.getKeyword());
            }
        }

        if (GaStringUtil.isNotEmpty(dataLibraryUserPageParam.getGroupId())) {
            queryWrapper.lambda()
                    .eq(SysUser::getGroupId, dataLibraryUserPageParam.getGroupId());
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

    // 增加访问量
    private void incrementAccessCount(List<DataLibrary> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        // 方式1: 逐条更新
        for (DataLibrary data : dataList) {
            data.setAccessCount(data.getAccessCount() + 1);
        }

        // 批量更新到数据库
        this.updateBatchById(dataList);
    }

    @Override
    public LibraryBatchCount batchQuery(BatchLibraryQueryParam libraryQueryParam) {
        log.info("开始执行批量查询 {}", JSONUtil.toJsonStr(libraryQueryParam));
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

        if ("GROUP_BY_GROUP".equals(libraryQueryParam.getCompareMode())) {
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

        // 代码筛选
        if (ObjectUtil.isNotEmpty(libraryQueryParam.getCodeFilter())) {
            // 代码筛选时间窗口（分钟）
            if ("TIME_WINDOW".equals(libraryQueryParam.getCodeFilter())) {
                queryWrapper.lambda().ge(DataLibrary::getUpdateTime, DateUtil.offsetMinute(new Date(), -libraryQueryParam.getCodeFilterTimeWindow()));
            }
        }

        long count = this.count(queryWrapper);

        // 采样计算
        BigDecimal checkCount = BigDecimal.ZERO;
        if (libraryQueryParam.getEnableSampling() && count > 0) {
            checkCount = BigDecimal.valueOf(count).multiply(libraryQueryParam.getSamplingRatio());
        }

        LibraryBatchCount batchCount = new LibraryBatchCount();
        batchCount.setTotalCount(String.valueOf(count)); // 获取有效记录代码总数量
        batchCount.setCheckCount(String.valueOf(checkCount));

        // 组合数计算
        long compareCount = count > 1 ? count * (count - 1) / 2 : 0;
        batchCount.setCompareCount(String.valueOf(compareCount));

        return batchCount;
    }
}