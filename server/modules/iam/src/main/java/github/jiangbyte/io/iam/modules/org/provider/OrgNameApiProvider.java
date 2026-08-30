package github.jiangbyte.io.iam.modules.org.provider;

import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.mapper.SysDeptMapper;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.mapper.SysGroupMapper;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import github.jiangbyte.io.iam.org.OrgNameApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link OrgNameApi}：经 Mapper 读 entity 名称，避免注入 Role/Dept/Group Service 形成环。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OrgNameApiProvider implements OrgNameApi {

    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final SysGroupMapper groupMapper;

    @Override
    public Map<String, String> roleNames(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SysRole> roles = roleMapper.selectByIds(ids);
        Map<String, String> map = new HashMap<>();
        for (SysRole role : roles) {
            if (role != null && StringUtils.hasText(role.getId())) {
                map.put(role.getId(), Objects.requireNonNullElse(role.getName(), ""));
            }
        }
        return map;
    }

    @Override
    public Map<String, String> deptNames(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SysDept> depts = deptMapper.selectByIds(ids);
        Map<String, String> map = new HashMap<>();
        for (SysDept dept : depts) {
            if (dept != null && StringUtils.hasText(dept.getId())) {
                map.put(dept.getId(), Objects.requireNonNullElse(dept.getName(), ""));
            }
        }
        return map;
    }

    @Override
    public Map<String, String> groupNames(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SysGroup> groups = groupMapper.selectByIds(ids);
        Map<String, String> map = new HashMap<>();
        for (SysGroup group : groups) {
            if (group != null && StringUtils.hasText(group.getId())) {
                map.put(group.getId(), Objects.requireNonNullElse(group.getName(), ""));
            }
        }
        return map;
    }
}
