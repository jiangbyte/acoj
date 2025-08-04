package io.charlie.app.core.modular.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.dict.entity.SysDictData;
import io.charlie.app.core.modular.dict.param.SysDictDataAddParam;
import io.charlie.app.core.modular.dict.param.SysDictDataEditParam;
import io.charlie.app.core.modular.dict.param.SysDictDataIdParam;
import io.charlie.app.core.modular.dict.param.SysDictDataPageParam;
import io.charlie.app.core.modular.dict.mapper.SysDictDataMapper;
import io.charlie.app.core.modular.dict.service.SysDictDataService;
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
* @description 字典数据表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public Page<SysDictData> page(SysDictDataPageParam sysDictDataPageParam) {
        QueryWrapper<SysDictData> queryWrapper = new QueryWrapper<SysDictData>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysDictDataPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysDictData::getTitle, sysDictDataPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysDictDataPageParam.getSortField(), sysDictDataPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictDataPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictDataPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictDataPageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysDictData::getSort);
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

}