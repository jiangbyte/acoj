package github.jiangbyte.io.common.security.datascope;

import java.util.Collection;
import java.util.List;

/**
 * 部门子树展开：将根部门 id 展开为含子孙，供 DEPT_AND_CHILD 数据范围使用。
 *
 * Author: Charlie
 */
@FunctionalInterface
public interface DeptSubtreeExpander {

    List<String> expand(Collection<String> rootDeptIds);
}
