package io.charlie.web.oj.modular.data.library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.DataLibraryAddParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryEditParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryIdParam;
import io.charlie.web.oj.modular.data.library.param.DataLibraryPageParam;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.library.service.DataLibraryService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

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

    @Override
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
            library.setCode(submit.getCode());
            library.setCodeLength(submit.getCodeLength());
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

        if (ObjectUtil.isNotEmpty(language)) {
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

        return query;
    }

    @Override
    public <R> List<R> processCodeLibrariesInBatches(boolean isSet, String language,
                                                     List<String> problemIds, List<String> setIds,
                                                     List<String> userIds, int batchSize,
                                                     String filterProblemId, String filterSetId,
                                                     String filterUserId,
                                                     Function<List<DataLibrary>, List<R>> processor) {

        LambdaQueryWrapper<DataLibrary> queryWrapper = buildSampleQuery(isSet, language, problemIds, setIds, userIds);

        // 添加过滤条件
        if (filterProblemId != null) {
            queryWrapper.ne(DataLibrary::getProblemId, filterProblemId);
        }
        if (filterSetId != null) {
            queryWrapper.ne(DataLibrary::getSetId, filterSetId);
        }
        if (filterUserId != null) {
            queryWrapper.ne(DataLibrary::getUserId, filterUserId);
        }

        List<R> allResults = new ArrayList<>();

        // 如果批次数为0，则一次性处理所有数据
        if (batchSize <= 0) {
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
        long pages = (total + batchSize - 1) / batchSize;

        for (long current = 1; current <= pages; current++) {
            Page<DataLibrary> page = this.page(new Page<>(current, batchSize), queryWrapper);
            if (page.getRecords() != null && !page.getRecords().isEmpty()) {
                List<DataLibrary> batch = page.getRecords();

                // 先增加访问量
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
}