package github.jiangbyte.io.common.mybatis.datascope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import github.jiangbyte.io.common.security.datascope.DataScopeConstraint;
import github.jiangbyte.io.common.security.datascope.DataScopeSupport;
import github.jiangbyte.io.common.security.datascope.DeptSubtreeExpander;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责人/部门列数据范围：供 iam 以外模块（如 biz）复用，不依赖 iam 实现。
 *
 * Author: Charlie
 */
@Component
public class OwnerDeptDataScope {

    private final ObjectProvider<DeptSubtreeExpander> expander;

    public OwnerDeptDataScope(ObjectProvider<DeptSubtreeExpander> expander) {
        this.expander = expander;
    }

    public DataScopeConstraint resolve(String permissionKey) {
        DeptSubtreeExpander expand = expander.getIfAvailable();
        return DataScopeSupport.resolveCurrent(
                permissionKey,
                roots -> expand == null ? List.copyOf(roots) : expand.expand(roots));
    }

    public void assertAccessible(String createdBy, String ownerDeptId, String permissionKey) {
        assertAccessible(createdBy, ownerDeptId, resolve(permissionKey));
    }

    /** 使用已解析约束断言（批量 delete 时只 resolve 一次）。 */
    public void assertAccessible(String createdBy, String ownerDeptId, DataScopeConstraint constraint) {
        DataScopeSupport.assertOwnerOrDeptAccessible(constraint, createdBy, ownerDeptId);
    }

    public <T> void apply(
            LambdaQueryWrapper<T> wrapper,
            String permissionKey,
            SFunction<T, ?> ownerColumn,
            SFunction<T, ?> deptColumn) {
        DataScopeConstraint constraint = resolve(permissionKey);
        switch (constraint) {
            case DataScopeConstraint.All ignored -> {
            }
            case DataScopeConstraint.Self self -> {
                if (ownerColumn == null) {
                    wrapper.apply("1 = 0");
                } else {
                    wrapper.eq(ownerColumn, self.accountId());
                }
            }
            case DataScopeConstraint.Depts depts -> {
                if (deptColumn == null || depts.deptIds().isEmpty()) {
                    wrapper.apply("1 = 0");
                } else {
                    wrapper.in(deptColumn, depts.deptIds());
                }
            }
            case DataScopeConstraint.Deny ignored -> wrapper.apply("1 = 0");
        }
    }
}
