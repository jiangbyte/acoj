package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${package.Entity}.${entity};
import ${package.AddParam}.${entity}AddParam;
import ${package.EditParam}.${entity}EditParam;
import ${package.IdParam}.${entity}IdParam;
import ${package.PageParam}.${entity}PageParam;
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
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
* @description ${table.comment!} 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ${table.serviceImplName} extends ${superServiceImplClass}<${table.mapperName}, ${entity}> implements ${table.serviceName} {

    @Override
    public Page<${entity}> page(${entity}PageParam ${table.entityPath}PageParam) {
        QueryWrapper<${entity}> queryWrapper = new QueryWrapper<${entity}>().checkSqlInjection();
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if ["title"]?seq_contains(field.propertyName)>
        // 关键字
        if (ObjectUtil.isNotEmpty(${table.entityPath}PageParam.getKeyword())) {
            queryWrapper.lambda().like(${entity}::getTitle, ${table.entityPath}PageParam.getKeyword());
        }
    <#elseif ["name"]?seq_contains(field.propertyName)>
        // 关键字
        if (ObjectUtil.isNotEmpty(${table.entityPath}PageParam.getKeyword())) {
            queryWrapper.lambda().like(${entity}::getName, ${table.entityPath}PageParam.getKeyword());
        }
    </#if>
</#list>
<#------------  END 字段循环遍历  ---------->
        if (ObjectUtil.isAllNotEmpty(${table.entityPath}PageParam.getSortField(), ${table.entityPath}PageParam.getSortOrder()) && ISortOrderEnum.isValid(${table.entityPath}PageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    ${table.entityPath}PageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(${table.entityPath}PageParam.getSortField()));
        }<#list table.fields as field><#if ["sort"]?seq_contains(field.propertyName)> else {
            queryWrapper.lambda().orderByAsc(${entity}::getSort);
        }</#if></#list>

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(${table.entityPath}PageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(${table.entityPath}PageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(${entity}AddParam ${table.entityPath}AddParam) {
        ${entity} bean = BeanUtil.toBean(${table.entityPath}AddParam, ${entity}.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(${entity}EditParam ${table.entityPath}EditParam) {
        if (!this.exists(new LambdaQueryWrapper<${entity}>().eq(${entity}::getId, ${table.entityPath}EditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ${entity} bean = BeanUtil.toBean(${table.entityPath}EditParam, ${entity}.class);
        BeanUtil.copyProperties(${table.entityPath}EditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<${entity}IdParam> ${table.entityPath}IdParamList) {
        if (ObjectUtil.isEmpty(${table.entityPath}IdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(${table.entityPath}IdParamList, ${entity}IdParam::getId));
    }

    @Override
    public ${entity} detail(${entity}IdParam ${table.entityPath}IdParam) {
        ${entity} ${table.entityPath} = this.getById(${table.entityPath}IdParam.getId());
        if (ObjectUtil.isEmpty(${table.entityPath})) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return ${table.entityPath};
    }

}