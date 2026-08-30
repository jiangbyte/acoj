package github.jiangbyte.io.biz.modules.cg_test_knowledge_category.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.convert.CgTestKnowledgeCategoryConvert;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeCategory;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.mapper.CgTestKnowledgeCategoryMapper;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeCategoryPageParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.service.CgTestKnowledgeCategoryService;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.convert.CgTestKnowledgeDocConvert;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.entity.CgTestKnowledgeDoc;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.mapper.CgTestKnowledgeDocMapper;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocAddParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocEditParam;
import github.jiangbyte.io.biz.modules.cg_test_knowledge_category.param.CgTestKnowledgeDocPageParam;
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
 * KnowledgeCategory服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class CgTestKnowledgeCategoryServiceImpl extends ServiceImpl<CgTestKnowledgeCategoryMapper, CgTestKnowledgeCategory> implements CgTestKnowledgeCategoryService {

    private final CgTestKnowledgeCategoryConvert cgTestKnowledgeCategoryConvert;
    private final CgTestKnowledgeDocMapper cgTestKnowledgeDocMapper;
    private final CgTestKnowledgeDocConvert cgTestKnowledgeDocConvert;

    @Override
    @Transactional
    public void create(CgTestKnowledgeCategoryAddParam param) {
        // 入参转实体并持久化
        CgTestKnowledgeCategory entity = cgTestKnowledgeCategoryConvert.toEntity(param);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(CgTestKnowledgeCategoryEditParam param) {
        // 按主键加载
        CgTestKnowledgeCategory entity = this.getById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestKnowledgeCategory not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestKnowledgeCategoryConvert.update(param, entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestKnowledgeCategory> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestKnowledgeCategory detail(String id) {
        // 按主键加载
        CgTestKnowledgeCategory entity = this.getById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestKnowledgeCategory not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestKnowledgeCategory> page(CgTestKnowledgeCategoryPageParam param) {
        // 组装条件并分页查询
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestKnowledgeCategory>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), CgTestKnowledgeCategory::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), CgTestKnowledgeCategory::getName, param.getName())
                        .eq(param.getStatus() != null && StringUtils.hasText(param.getStatus()), CgTestKnowledgeCategory::getStatus, param.getStatus())
                        .orderByDesc(CgTestKnowledgeCategory::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree(String keyword) {
        // 查询节点列表
        List<CgTestKnowledgeCategory> rows = this.list(Wrappers.<CgTestKnowledgeCategory>lambdaQuery()
                .like(StringUtils.hasText(keyword), CgTestKnowledgeCategory::getName, keyword)
                .orderByAsc(CgTestKnowledgeCategory::getCreatedAt));
        if (rows.isEmpty()) {
            return List.of();
        }

        // 构建树结构
        Set<String> ids = rows.stream().map(CgTestKnowledgeCategory::getId).collect(Collectors.toSet());
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

    @Override
    @Transactional
    public void childCreate(CgTestKnowledgeDocAddParam param) {
        // 入参转子实体并插入
        CgTestKnowledgeDoc entity = cgTestKnowledgeDocConvert.toEntity(param);
        cgTestKnowledgeDocMapper.insert(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void childUpdate(CgTestKnowledgeDocEditParam param) {
        // 按主键加载子实体
        CgTestKnowledgeDoc entity = cgTestKnowledgeDocMapper.selectById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestKnowledgeDoc not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestKnowledgeDocConvert.update(param, entity);
        cgTestKnowledgeDocMapper.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void childDelete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestKnowledgeDoc> entities = cgTestKnowledgeDocMapper.selectByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除子实体
        cgTestKnowledgeDocMapper.deleteBatchIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestKnowledgeDoc childDetail(String id) {
        // 按主键加载子实体
        CgTestKnowledgeDoc entity = cgTestKnowledgeDocMapper.selectById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestKnowledgeDoc not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestKnowledgeDoc> childPage(CgTestKnowledgeDocPageParam param) {
        // 按外键分页查询子实体
        return cgTestKnowledgeDocMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestKnowledgeDoc>lambdaQuery()
                        .eq(StringUtils.hasText(param.getCategoryId()), CgTestKnowledgeDoc::getCategoryId, param.getCategoryId())
                        .orderByDesc(CgTestKnowledgeDoc::getCreatedAt));
    }
}
