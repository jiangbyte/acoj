package github.jiangbyte.io.common.security.datascope;

import java.util.List;

/**
 * 列表/分页查询解析后的数据范围约束（全部、本人、部门集合或拒绝）。
 *
 * Author: Charlie
 */
public sealed interface DataScopeConstraint
        permits DataScopeConstraint.All, DataScopeConstraint.Self,
        DataScopeConstraint.Depts, DataScopeConstraint.Deny {

    record All() implements DataScopeConstraint {
    }

    record Self(String accountId) implements DataScopeConstraint {
    }

    record Depts(List<String> deptIds) implements DataScopeConstraint {
    }

    /** 无匹配行（如 SELF 缺少归属列，或部门集合为空）。 */
    record Deny() implements DataScopeConstraint {
    }
}
