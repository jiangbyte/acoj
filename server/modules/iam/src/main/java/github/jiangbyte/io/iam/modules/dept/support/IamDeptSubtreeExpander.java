package github.jiangbyte.io.iam.modules.dept.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.security.datascope.DeptSubtreeExpander;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IAM 部门子树展开，供跨模块 OwnerDeptDataScope 使用。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class IamDeptSubtreeExpander implements DeptSubtreeExpander {

    private final SysDeptMapper deptMapper;

    @Override
    public List<String> expand(Collection<String> rootDeptIds) {
        if (rootDeptIds == null || rootDeptIds.isEmpty()) {
            return List.of();
        }
        List<SysDept> all = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .select(SysDept::getId, SysDept::getParentId));
        Map<String, List<String>> childrenByParent = new HashMap<>();
        for (SysDept dept : all) {
            String parentId = dept.getParentId() == null ? "" : dept.getParentId();
            childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(dept.getId());
        }
        Set<String> result = new HashSet<>();
        ArrayDeque<String> stack = new ArrayDeque<>();
        rootDeptIds.stream().filter(StringUtils::hasText).sorted().forEach(stack::push);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!result.add(current)) {
                continue;
            }
            for (String child : childrenByParent.getOrDefault(current, List.of())) {
                stack.push(child);
            }
        }
        return result.stream().sorted().collect(Collectors.toList());
    }
}
