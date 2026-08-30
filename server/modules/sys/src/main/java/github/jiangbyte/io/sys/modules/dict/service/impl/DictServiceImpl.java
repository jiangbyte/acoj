package github.jiangbyte.io.sys.modules.dict.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.sys.modules.dict.convert.SysDictConvert;
import github.jiangbyte.io.sys.modules.dict.entity.SysDict;
import github.jiangbyte.io.sys.modules.dict.mapper.SysDictMapper;
import github.jiangbyte.io.sys.modules.dict.param.SysDictAddParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictEditParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictPageParam;
import github.jiangbyte.io.sys.modules.dict.service.DictService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.DictionaryTransService;
import org.dromara.trans.service.impl.TransService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现：字典维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements DictService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DictServiceImpl.class);

    /** 字典分类仅允许 SYS / BIZ（与 hei-fastapi SysBizCategory 枚举对齐）。 */
    private static final Set<String> DICT_CATEGORIES = Set.of("SYS", "BIZ");

    private final SysDictConvert dictConvert;
    private final DictionaryTransService dictionaryTransService;
    private final TransService transService;

    @Override
    public void afterPropertiesSet() {
        // 刷新翻译缓存
        refreshTransCache();
    }

    /** 校验字典分类：仅允许 SYS / BIZ。 */
    private void validateCategory(String category) {
        if (StringUtils.hasText(category) && !DICT_CATEGORIES.contains(category)) {
            throw new BizException("Dict category must be SYS or BIZ");
        }
    }

    @Override
    @Transactional
    public void create(SysDictAddParam param) {
        validateCategory(param.getCategory());
        // 校验唯一性
        SysDict existing = getBaseMapper().selectOne(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getCode, param.getCode()).last("limit 1"));
        if (existing != null) {
            throw new BizException("Dict code already exists");
        }
        SysDict dict = dictConvert.toEntity(param);
        this.save(dict);
        AuditSnapshots.created(dict);
        refreshTransCache();
    }

    @Override
    @Transactional
    public void update(SysDictEditParam param) {
        validateCategory(param.getCategory());
        // 按主键加载
        SysDict dict = this.getById(param.getId());
        if (dict == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Dict not found");
        }
        SysDict existing = getBaseMapper().selectOne(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getCode, param.getCode()).last("limit 1"));
        if (existing != null && !dict.getId().equals(existing.getId())) {
            throw new BizException("Dict code already exists");
        }
        AuditSnapshots.before(dict);
        dictConvert.update(param, dict);
        this.updateById(dict);
        AuditSnapshots.after(dict);
        refreshTransCache();
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysDict> dicts = this.listByIds(ids);
        AuditSnapshots.deletedAll(dicts);
        this.removeByIds(ids);
        refreshTransCache();
    }

    @Override
    @ReadDataSource
    public SysDict detail(String id) {
        // 按主键加载
        SysDict dict = this.getById(id);
        if (dict == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Dict not found");
        }
        transService.transOne(dict);
        return dict;
    }

    @Override
    @ReadDataSource
    public Page<SysDict> page(SysDictPageParam param) {
        // 分页查询
        Page<SysDict> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysDict>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), SysDict::getCode, param.getCode())
                        .eq(StringUtils.hasText(param.getCategory()), SysDict::getCategory, param.getCategory())
                        .eq(StringUtils.hasText(param.getParentId()), SysDict::getParentId, param.getParentId())
                        .eq(StringUtils.hasText(param.getStatus()), SysDict::getStatus, param.getStatus())
                        .orderByAsc(SysDict::getSort)
                        .orderByDesc(SysDict::getCreatedAt));
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree(String category) {
        // 组装查询条件
        List<SysDict> all = getBaseMapper().selectList(Wrappers.<SysDict>lambdaQuery()
                .eq(StringUtils.hasText(category), SysDict::getCategory, category)
                .orderByAsc(SysDict::getSort));
        transService.transBatch(all);
        if (all.isEmpty()) {
            return List.of();
        }

        Set<String> ids = all.stream().map(SysDict::getId).collect(Collectors.toSet());
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parent_id");
        config.setNameKey("name");
        config.setWeightKey("weight");
        config.setChildrenKey("children");
        return TreeUtil.build(all, null, config, (dict, tree) -> {
            String parentId = dict.getParentId();
            if (!StringUtils.hasText(parentId) || !ids.contains(parentId)) {
                parentId = null;
            }
            BeanUtil.beanToMap(dict, false, true).forEach((key, value) -> {
                if (!"children".equals(key)) {
                    tree.putExtra(StrUtil.toUnderlineCase(key), value);
                }
            });
            tree.setId(dict.getId());
            tree.setParentId(parentId);
            tree.setName(dict.getLabel());
            tree.setWeight(dict.getSort() == null ? 0 : dict.getSort());
        });
    }

    @Override
    @ReadDataSource
    public List<SysDict> listByType(String dictType) {
        // 组装查询条件
        return getBaseMapper().selectList(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getStatus, "ENABLED")
                .and(wrapper -> wrapper.eq(SysDict::getCode, dictType).or().eq(SysDict::getCategory, dictType))
                .orderByAsc(SysDict::getSort));
    }

    private void refreshTransCache() {
        CompletableFuture.runAsync(() -> {
            try {
                // 启用 Redis 翻译缓存
                dictionaryTransService.makeUseRedis();
                List<SysDict> all = getBaseMapper().selectList(Wrappers.emptyWrapper());
                // 按父字典分组子项
                Map<String, List<SysDict>> childrenByParent = all.stream()
                        .filter(dict -> StringUtils.hasText(dict.getParentId()))
                        .collect(Collectors.groupingBy(SysDict::getParentId));
                Map<String, String> parentCodeById = all.stream()
                        .filter(dict -> !StringUtils.hasText(dict.getParentId()))
                        .collect(Collectors.toMap(SysDict::getId, SysDict::getCode, (a, b) -> a));
                // 刷新 value→label 映射
                for (Map.Entry<String, String> parent : parentCodeById.entrySet()) {
                    List<SysDict> children = childrenByParent.get(parent.getKey());
                    if (children == null || children.isEmpty() || !StringUtils.hasText(parent.getValue())) {
                        continue;
                    }
                    Map<String, String> valueLabel = children.stream()
                            .filter(item -> StringUtils.hasText(item.getValue()))
                            .collect(Collectors.toMap(
                                    SysDict::getValue,
                                    item -> Objects.requireNonNullElse(item.getLabel(), item.getValue()),
                                    (a, b) -> a));
                    if (!valueLabel.isEmpty()) {
                        dictionaryTransService.refreshCache(parent.getValue(), valueLabel);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to refresh dictionary trans cache", ex);
            }
        });
    }
}
