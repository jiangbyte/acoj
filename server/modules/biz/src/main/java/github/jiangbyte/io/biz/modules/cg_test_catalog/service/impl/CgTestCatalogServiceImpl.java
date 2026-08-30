package github.jiangbyte.io.biz.modules.cg_test_catalog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.biz.modules.cg_test_catalog.convert.CgTestCatalogConvert;
import github.jiangbyte.io.biz.modules.cg_test_catalog.entity.CgTestCatalog;
import github.jiangbyte.io.biz.modules.cg_test_catalog.mapper.CgTestCatalogMapper;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogAddParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogEditParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.param.CgTestCatalogPageParam;
import github.jiangbyte.io.biz.modules.cg_test_catalog.service.CgTestCatalogService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Catalog服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class CgTestCatalogServiceImpl extends ServiceImpl<CgTestCatalogMapper, CgTestCatalog> implements CgTestCatalogService {

    private final CgTestCatalogConvert cgTestCatalogConvert;

    @Override
    @Transactional
    public void create(CgTestCatalogAddParam param) {
        // 入参转实体并持久化
        CgTestCatalog entity = cgTestCatalogConvert.toEntity(param);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(CgTestCatalogEditParam param) {
        // 按主键加载
        CgTestCatalog entity = this.getById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestCatalog not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestCatalogConvert.update(param, entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestCatalog> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestCatalog detail(String id) {
        // 按主键加载
        CgTestCatalog entity = this.getById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestCatalog not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestCatalog> page(CgTestCatalogPageParam param) {
        // 组装条件并分页查询
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestCatalog>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), CgTestCatalog::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), CgTestCatalog::getName, param.getName())
                        .like(StringUtils.hasText(param.getCategory()), CgTestCatalog::getCategory, param.getCategory())
                        .eq(param.getStatus() != null && StringUtils.hasText(param.getStatus()), CgTestCatalog::getStatus, param.getStatus())
                        .orderByDesc(CgTestCatalog::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree(String keyword) {
        // 查询节点列表
        List<CgTestCatalog> rows = this.list(Wrappers.<CgTestCatalog>lambdaQuery()
                .like(StringUtils.hasText(keyword), CgTestCatalog::getName, keyword)
                .orderByAsc(CgTestCatalog::getCreatedAt));
        if (rows.isEmpty()) {
            return List.of();
        }

        // 构建树结构
        Set<String> ids = rows.stream().map(CgTestCatalog::getId).collect(Collectors.toSet());
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parent_id");
        config.setNameKey("name");
        config.setWeightKey("weight");
        config.setChildrenKey("children");
        return TreeUtil.build(rows, null, config, (row, tree) -> {
            String parentId = row.getParentId();
            if (!StringUtils.hasText(parentId) || !ids.contains(parentId)) {
                parentId = null;
            }
            BeanUtil.beanToMap(row, false, true).forEach((key, value) -> {
                if (!"children".equals(key)) {
                    tree.putExtra(StrUtil.toUnderlineCase(key), value);
                }
            });
            tree.setId(row.getId());
            tree.setParentId(parentId);
            tree.setName(row.getName());
            tree.setWeight(0);
        });
    }
}
