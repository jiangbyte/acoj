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
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

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

    @Override
    public Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam) {
        QueryWrapper<DataLibrary> queryWrapper = new QueryWrapper<DataLibrary>().checkSqlInjection();
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

    @Override
    public void addLibrary(DataSubmit submit) {
        // 构建查询条件
        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getUserId, submit.getUserId())
                .eq(DataLibrary::getIsSet, submit.getIsSet())
                .eq(DataLibrary::getProblemId, submit.getProblemId());

        // 如果是set模式，需要额外匹配setId
        if (submit.getIsSet()) {
            queryWrapper.eq(DataLibrary::getSetId, submit.getSetId());
        }

        // 查找已存在的记录
        DataLibrary library = this.getOne(queryWrapper);

        if (library != null) {
            log.info("存在记录，执行更新");
            // 更新现有记录
            updateExistingLibrary(library, submit);
            this.updateById(library);
        } else {
            log.info("不存在记录，创建新记录");
            // 创建新记录
            library = createNewLibrary(submit);
            this.save(library);
        }
    }

    private long countCodeLibraries(boolean isSet, String language,
                                    List<String> problemIds, List<String> setIds, List<String> userIds) {
        LambdaQueryWrapper<DataLibrary> query = buildSampleQuery(isSet, language, problemIds, setIds, userIds);
        return this.count(query);
    }

    private LambdaQueryWrapper<DataLibrary> buildSampleQuery(boolean isSet, String language, List<String> problemIds, List<String> setIds, List<String> userIds) {
        LambdaQueryWrapper<DataLibrary> query = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getIsSet, isSet);

        if (ObjectUtil.isEmpty(language)) {
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
    public List<DataLibrary> getCodeLibraries(boolean isSet, String language, List<String> problemIds, List<String> setIds, List<String> userIds) {
        LambdaQueryWrapper<DataLibrary> query = buildSampleQuery(isSet, language, problemIds, setIds, userIds);
        return this.list(query);
    }

    @Override
    public Page<DataLibrary> getCodeLibrariesByPage(boolean isSet, String language,
                                                    List<String> problemIds, List<String> setIds, List<String> userIds,
                                                    long current, long size) {

        Page<DataLibrary> page = new Page<>(current, size);
        LambdaQueryWrapper<DataLibrary> query = buildSampleQuery(isSet, language, problemIds, setIds, userIds);
        return this.page(page, query);
    }

    @Override
    public void processCodeLibrariesInBatches(boolean isSet, String language, List<String> problemIds, List<String> setIds, List<String> userIds, int batchSize, Consumer<List<DataLibrary>> processor) {
        long total = countCodeLibraries(isSet, language, problemIds, setIds, userIds);

        // 如果批次数为0，则一次性处理所有数据
        if (batchSize <= 0) {
            List<DataLibrary> allData = getCodeLibraries(isSet, language, problemIds, setIds, userIds);
            if (allData != null && !allData.isEmpty()) {
                // 先增加访问量
                incrementAccessCount(allData);
                // 然后执行处理逻辑
                processor.accept(allData);
            }
            return;
        }

        // 正常分批处理
        long pages = (total + batchSize - 1) / batchSize;

        for (long current = 1; current <= pages; current++) {
            Page<DataLibrary> page = getCodeLibrariesByPage(isSet, language, problemIds, setIds, userIds, current, batchSize);
            if (page.getRecords() != null && !page.getRecords().isEmpty()) {
                // 先增加访问量
                incrementAccessCount(page.getRecords());
                // 然后执行处理逻辑
                processor.accept(page.getRecords());
            }
        }
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

    /**
     * 更新已存在的库记录
     */
    private void updateExistingLibrary(DataLibrary library, DataSubmit submit) {
        library.setUserId(submit.getUserId());
        library.setSetId(submit.getSetId());
        library.setIsSet(submit.getIsSet());
        library.setProblemId(submit.getProblemId());
        library.setSubmitId(submit.getId());
        library.setSubmitTime(submit.getCreateTime());
        library.setLanguage(submit.getLanguage());
        library.setCode(submit.getCode());
        library.setCodeLength(submit.getCodeLength());
    }

    /**
     * 创建新的库记录
     */
    private DataLibrary createNewLibrary(DataSubmit submit) {
        DataLibrary library = new DataLibrary();
        updateExistingLibrary(library, submit); // 复用设置逻辑
        return library;
    }
}