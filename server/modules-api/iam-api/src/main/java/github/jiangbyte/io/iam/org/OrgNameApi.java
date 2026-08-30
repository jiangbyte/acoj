package github.jiangbyte.io.iam.org;

import java.util.Collection;
import java.util.Map;

/**
 * 跨模块组织名称查询：按 id 批量读取角色/部门/用户组 entity 的 name。
 *
 * Author: Charlie
 */
public interface OrgNameApi {

    /** 角色 id → name；缺失 id 不出现在结果中。 */
    Map<String, String> roleNames(Collection<String> ids);

    /** 部门 id → name。 */
    Map<String, String> deptNames(Collection<String> ids);

    /** 用户组 id → name。 */
    Map<String, String> groupNames(Collection<String> ids);
}
