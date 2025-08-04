package io.charlie.app.core.modular.sys.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.article.entity.SysArticle;
import io.charlie.app.core.modular.sys.dict.entity.SysDictType;
import io.charlie.app.core.modular.sys.dict.param.*;
import io.charlie.app.core.modular.sys.dict.mapper.SysDictTypeMapper;
import io.charlie.app.core.modular.sys.dict.service.SysDictTypeService;
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

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 字典类型表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> page(SysDictTypePageParam sysDictTypePageParam) {
        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper<SysDictType>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysDictTypePageParam.getSortField(), sysDictTypePageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictTypePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictTypePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictTypePageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysDictTypePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysDictTypePageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysDictTypeAddParam sysDictTypeAddParam) {
        SysDictType bean = BeanUtil.toBean(sysDictTypeAddParam, SysDictType.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysDictTypeEditParam sysDictTypeEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getId, sysDictTypeEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysDictType bean = BeanUtil.toBean(sysDictTypeEditParam, SysDictType.class);
        BeanUtil.copyProperties(sysDictTypeEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysDictTypeIdParam> sysDictTypeIdParamList) {
        if (ObjectUtil.isEmpty(sysDictTypeIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysDictTypeIdParamList, SysDictTypeIdParam::getId));
    }

    @Override
    public SysDictType detail(SysDictTypeIdParam sysDictTypeIdParam) {
        SysDictType sysDictType = this.getById(sysDictTypeIdParam.getId());
        if (ObjectUtil.isEmpty(sysDictType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysDictType;
    }

    @Override
    public List<LabelOption<String>> treeOptions(SysDictTypeOptionParam sysDictTypeOptionParam) {
        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper<SysDictType>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysDictTypeOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysDictType::getTypeName, sysDictTypeOptionParam.getKeyword());
        }

        // 获得所有字典类型列表
        List<SysDictType> allDictTypes = this.list(queryWrapper);

        // 构建树形结构
        // 使用Map存储所有节点，key为节点ID，value为转换后的LabelOption
        Map<String, LabelOption<String>> nodeMap = new HashMap<>();

        // 使用Map存储父子关系，key为父节点ID，value为子节点列表
        Map<String, List<LabelOption<String>>> parentChildMap = new HashMap<>();

        // 第一次遍历：初始化所有节点
        for (SysDictType dictType : allDictTypes) {
            LabelOption<String> option = new LabelOption<>(
                    dictType.getId(),
                    dictType.getTypeName()
            );
            nodeMap.put(dictType.getId(), option);

            // 初始化父节点下的子节点列表
            String parentId = dictType.getParentId() == null ? "0" : dictType.getParentId();
            parentChildMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(option);
        }

        // 第二次遍历：构建树形结构
        List<LabelOption<String>> rootOptions = parentChildMap.getOrDefault("0", new ArrayList<>());

        for (LabelOption<String> option : nodeMap.values()) {
            // 获取当前节点的所有子节点
            List<LabelOption<String>> children = parentChildMap.get(option.getValue());
            if (children != null && !children.isEmpty()) {
                option.setChildren(children);
            }
        }

        return rootOptions;
    }

}