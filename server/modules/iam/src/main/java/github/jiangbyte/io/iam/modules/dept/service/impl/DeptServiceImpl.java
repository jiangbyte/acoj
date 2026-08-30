package github.jiangbyte.io.iam.modules.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.security.datascope.DataScopeConstraint;
import github.jiangbyte.io.iam.modules.dept.convert.SysDeptConvert;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.mapper.SysDeptMapper;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptAddParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptEditParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptPageParam;
import github.jiangbyte.io.iam.modules.dept.service.DeptService;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门服务实现：层级校验、树构建与数据权限过滤。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements DeptService {

    private final IamRelationService relationService;
    private final TransService transService;
    private final DataScopeResolver dataScopeResolver;
    private final SysDeptConvert deptConvert;

    @Override
    @Transactional
    public void create(SysDeptAddParam param) {
        SysDept dept = deptConvert.toEntity(param);
        this.save(dept);
        AuditSnapshots.created(dept);
    }

    @Override
    @Transactional
    public void update(SysDeptEditParam param) {
        SysDept dept = this.getById(param.getId());
        if (dept == null) {
            throw new BizException(404, "Dept not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(dept.getCreatedBy(), dept.getId(), "iam:dept:page");
        AuditSnapshots.before(dept);
        deptConvert.update(param, dept);
        this.updateById(dept);
        AuditSnapshots.after(dept);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysDept> depts = this.listByIds(ids);
        DataScopeConstraint scope = dataScopeResolver.resolve("iam:dept:page");
        for (SysDept dept : depts) {
            dataScopeResolver.assertOwnerOrDeptAccessible(dept.getCreatedBy(), dept.getId(), scope);
        }
        AuditSnapshots.deletedAll(depts);
        relationService.deleteByTargetIds(IamRelationTypes.TARGET_DEPT, ids);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysDept detail(String id) {
        SysDept dept = this.getById(id);
        if (dept == null) {
            throw new BizException(404, "Dept not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(dept.getCreatedBy(), dept.getId(), "iam:dept:page");
        return dept;
    }

    @Override
    @ReadDataSource
    public Page<SysDept> page(SysDeptPageParam param) {
        LambdaQueryWrapper<SysDept> wrapper = Wrappers.<SysDept>lambdaQuery()
                .like(StringUtils.hasText(param.getName()), SysDept::getName, param.getName())
                .eq(StringUtils.hasText(param.getCategory()), SysDept::getCategory, param.getCategory())
                .eq(StringUtils.hasText(param.getStatus()), SysDept::getStatus, param.getStatus())
                .eq(StringUtils.hasText(param.getParentId()), SysDept::getParentId, param.getParentId())
                .orderByAsc(SysDept::getSort);
        dataScopeResolver.applyOwnerOrDept(wrapper, "iam:dept:page", SysDept::getCreatedBy, SysDept::getId);
        Page<SysDept> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree() {
        // 查询部门列表后构建组织树
        LambdaQueryWrapper<SysDept> wrapper = Wrappers.<SysDept>lambdaQuery()
                .orderByAsc(SysDept::getSort);
        dataScopeResolver.applyOwnerOrDept(wrapper, "iam:dept:tree", SysDept::getCreatedBy, SysDept::getId);
        List<SysDept> all = getBaseMapper().selectList(wrapper);
        transService.transBatch(all);
        if (all.isEmpty()) {
            return List.of();
        }

        Set<String> ids = all.stream().map(SysDept::getId).collect(Collectors.toSet());
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parent_id");
        config.setNameKey("name");
        config.setWeightKey("weight");
        config.setChildrenKey("children");
        return TreeUtil.build(all, null, config, (dept, tree) -> {
            String parentId = dept.getParentId();
            if (!StringUtils.hasText(parentId) || !ids.contains(parentId)) {
                parentId = null;
            }
            BeanUtil.beanToMap(dept, false, true).forEach((key, value) -> {
                if (!"children".equals(key)) {
                    tree.putExtra(StrUtil.toUnderlineCase(key), value);
                }
            });
            tree.setId(dept.getId());
            tree.setParentId(parentId);
            tree.setName(dept.getName());
            tree.setWeight(dept.getSort() == null ? 0 : dept.getSort());
        });
    }

}
