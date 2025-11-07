package io.charlie.web.oj.modular.sys.dict.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.galaxy.utils.str.GalaxyStringUtil;
import io.charlie.web.oj.modular.sys.dict.entity.SysDict;
import io.charlie.web.oj.modular.sys.dict.param.*;
import io.charlie.web.oj.modular.sys.dict.mapper.SysDictMapper;
import io.charlie.web.oj.modular.sys.dict.service.SysDictService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import jakarta.annotation.PostConstruct;
import org.dromara.trans.service.impl.DictionaryTransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 系统字典表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {
    private final DictionaryTransService dictionaryTransService;

    @Override
    @DS("slave")
    public Page<SysDict> page(SysDictPageParam sysDictPageParam) {
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysDictPageParam.getSortField(), sysDictPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysDictPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysDictPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysDictPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysDictPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysDictPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysDictAddParam sysDictAddParam) {
        boolean exists = this.exists(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, sysDictAddParam.getDictType())
                .eq(SysDict::getDictValue, sysDictAddParam.getDictValue())
        );
        if (exists) {
            throw new BusinessException("字典值已经存在");
        }
        SysDict bean = BeanUtil.toBean(sysDictAddParam, SysDict.class);
        this.save(bean);
        refreshDictCache(bean.getDictType());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysDictEditParam sysDictEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysDict>().eq(SysDict::getId, sysDictEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        // 获得原本的 数据
        SysDict old = this.getById(sysDictEditParam.getId());
        // 赋值处理
        SysDict bean = BeanUtil.toBean(sysDictEditParam, SysDict.class);
        BeanUtil.copyProperties(sysDictEditParam, bean);
        this.updateById(bean);
        // 如果修改dictType，其他引用的dictType也修改
        if (!bean.getDictType().equals(old.getDictType())) {
            // 找到所有的
            List<SysDict> list = this.listByDictType(old.getDictType());
            // 全部更新字段
            list.forEach(item -> item.setDictType(bean.getDictType()));
            this.updateBatchById(list);
        }
        // 如果修改了dictTypeLabel，其他引用的dictTypeLabel也修改
        if (!bean.getTypeLabel().equals(old.getTypeLabel())) {
            List<SysDict> list = this.listByDictType(bean.getDictType());
            list.forEach(item -> item.setDictLabel(bean.getTypeLabel()));
            this.updateBatchById(list);
        }
        refreshDictCache(bean.getDictType());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysDictIdParam> sysDictIdParamList) {
        if (ObjectUtil.isEmpty(sysDictIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysDictIdParamList, SysDictIdParam::getId));
    }

    @Override
    @DS("slave")
    public SysDict detail(SysDictIdParam sysDictIdParam) {
        SysDict sysDict = this.getById(sysDictIdParam.getId());
        if (ObjectUtil.isEmpty(sysDict)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysDict;
    }

    @Override
    @DS("slave")
    public List<LabelOption<String>> options(String dictType) {
        if (GalaxyStringUtil.isEmpty(dictType)) {
            return List.of();
        }

        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>().checkSqlInjection();
        queryWrapper.lambda().like(SysDict::getDictType, dictType);
        // 从数据库获取的所有字典数据
        List<SysDict> dictList = this.list(queryWrapper);

        return dictList.stream().map(sysDict -> new LabelOption<String>(sysDict.getDictValue(), sysDict.getDictLabel())).collect(Collectors.toList());
    }

    @Override
    @DS("slave")
    public List<LabelOption<String>> listLabelOption(SysDictOptionParam sysDictOptionParam) {
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysDictOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysDict::getTypeLabel, sysDictOptionParam.getKeyword());
        }
        // 从数据库获取的所有字典数据
        List<SysDict> dictList = this.list(queryWrapper);

        return new ArrayList<>(dictList.stream()
                .collect(Collectors.toMap(
                        SysDict::getDictType,  // 以dictType作为key
                        dict -> new LabelOption<>(dict.getDictType(), dict.getTypeLabel()),
                        (existing, replacement) -> existing  // 如果有重复key，保留已存在的
                ))
                .values());
    }

    @Override
    @DS("slave")
    public List<LabelOption<String>> treeLabelOption(SysDictTreeParam sysDictTreeParam) {
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysDictTreeParam.getKeyword())) {
            queryWrapper.lambda().like(SysDict::getTypeLabel, sysDictTreeParam.getKeyword());
        }

        // 从数据库获取的所有字典数据
        List<SysDict> dictList = this.list(queryWrapper);

        // 按字典类型分组
        Map<String, List<SysDict>> dictTypeMap = dictList.stream()
                .collect(Collectors.groupingBy(SysDict::getDictType));

        // 构建树形结构
        List<LabelOption<String>> tree = new ArrayList<>();

        // 遍历每种字典类型
        dictTypeMap.forEach((dictType, dictItems) -> {
            // 创建字典类型节点(父节点)
            LabelOption<String> typeNode = new LabelOption<>();
            typeNode.setLabel(dictItems.getFirst().getTypeLabel()); // 使用第一个元素的dictTypeLabel
            typeNode.setValue(dictType);

            // 为该类型下的字典项创建子节点
            List<LabelOption<String>> children = dictItems.stream()
                    .sorted(Comparator.comparing(SysDict::getSortOrder)) // 按排序字段排序
                    .map(item -> {
                        LabelOption<String> child = new LabelOption<>();
                        child.setLabel(item.getDictLabel());
                        child.setValue(item.getDictType() + ":" + item.getDictValue());
                        return child;
                    })
                    .collect(Collectors.toList());

            typeNode.setChildren(children);
            tree.add(typeNode);
        });

        return tree;
    }

    @Override
    @DS("slave")
    public List<SysDict> listByDictType(String dictType) {
        if (ObjectUtil.isEmpty(dictType)) {
            return List.of();
        }

        return this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getSortOrder));
    }


    @PostConstruct
    @DS("slave")
    public void initDictCache() {
        CompletableFuture.runAsync(() -> {
            log.debug("开始异步初始化字典缓存...");

            try {
                // 一次性查询所有字典数据
                List<SysDict> allDicts = this.list();

                // 按字典类型分组
                Map<String, List<SysDict>> dictsByType = allDicts.stream()
                        .collect(Collectors.groupingBy(SysDict::getDictType));

                // 批量刷新缓存
                dictsByType.forEach((dictType, dictItems) -> {
                    Map<String, String> transMap = dictItems.stream()
                            .collect(Collectors.toMap(SysDict::getDictValue, SysDict::getDictLabel));
                    dictionaryTransService.refreshCache(dictType, transMap);
                });

                log.debug("字典缓存异步初始化完成，共初始化 {} 个字典类型", dictsByType.size());
            } catch (Exception e) {
                log.error("字典缓存初始化失败", e);
            }
        });
    }

    @DS("slave")
    public void refreshDictCache(String dictType) {
        List<SysDict> dictItems = this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType));
        Map<String, String> transMap = dictItems.stream()
                .collect(Collectors.toMap(SysDict::getDictValue, SysDict::getDictLabel));

        dictionaryTransService.refreshCache(dictType, transMap);
    }

}