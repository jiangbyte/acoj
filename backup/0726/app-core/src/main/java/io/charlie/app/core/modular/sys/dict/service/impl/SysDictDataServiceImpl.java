package io.charlie.app.core.modular.sys.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.dict.entity.SysDictData;
import io.charlie.app.core.modular.sys.dict.entity.SysDictType;
import io.charlie.app.core.modular.sys.dict.mapper.SysDictTypeMapper;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataAddParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataEditParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataIdParam;
import io.charlie.app.core.modular.sys.dict.param.SysDictDataPageParam;
import io.charlie.app.core.modular.sys.dict.mapper.SysDictDataMapper;
import io.charlie.app.core.modular.sys.dict.service.SysDictDataService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
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
 * @description 字典数据表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {
    private final SysDictTypeMapper sysDictTypeMapper;

    @Override
    public Page<SysDictData> page(SysDictDataPageParam sysDictDataPageParam) {
        QueryWrapper<SysDictData> queryWrapper = new QueryWrapper<SysDictData>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysDictDataPageParam.getSortField(), sysDictDataPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictDataPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictDataPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictDataPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysDictDataPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysDictDataPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysDictDataAddParam sysDictDataAddParam) {
        SysDictData bean = BeanUtil.toBean(sysDictDataAddParam, SysDictData.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysDictDataEditParam sysDictDataEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getId, sysDictDataEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysDictData bean = BeanUtil.toBean(sysDictDataEditParam, SysDictData.class);
        BeanUtil.copyProperties(sysDictDataEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysDictDataIdParam> sysDictDataIdParamList) {
        if (ObjectUtil.isEmpty(sysDictDataIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysDictDataIdParamList, SysDictDataIdParam::getId));
    }

    @Override
    public SysDictData detail(SysDictDataIdParam sysDictDataIdParam) {
        SysDictData sysDictData = this.getById(sysDictDataIdParam.getId());
        if (ObjectUtil.isEmpty(sysDictData)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysDictData;
    }

    @Override
    public List<SysDictData> listById(String typeId) {
        QueryWrapper<SysDictData> queryWrapper = new QueryWrapper<SysDictData>().checkSqlInjection();
        queryWrapper.lambda().eq(SysDictData::getTypeId, typeId);
        return this.list(queryWrapper);
    }

    @Override
    public List<LabelOption<String>> listByCode(String typeCode) {
        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper<SysDictType>().checkSqlInjection();
        queryWrapper.lambda().eq(SysDictType::getTypeCode, typeCode);
        SysDictType sysDictType = sysDictTypeMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(sysDictType)) {
            return new ArrayList<>();
        }

        QueryWrapper<SysDictData> queryWrapper1 = new QueryWrapper<SysDictData>().checkSqlInjection();
        queryWrapper1.lambda().eq(SysDictData::getTypeId, sysDictType.getId());

        // 获得所有字典数据列表
        List<SysDictData> allDictTypes = this.list(queryWrapper1);

        return allDictTypes.stream().map(dictData -> new LabelOption<>(dictData.getDictValue(), dictData.getDictLabel())).collect(Collectors.toList());
    }

}