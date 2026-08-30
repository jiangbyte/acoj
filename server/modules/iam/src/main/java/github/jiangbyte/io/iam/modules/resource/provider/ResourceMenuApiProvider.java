package github.jiangbyte.io.iam.modules.resource.provider;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.entity.SysResourceModule;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceModuleMapper;
import github.jiangbyte.io.iam.resource.ResourceMenuApi;
import github.jiangbyte.io.iam.resource.ResourceMenuNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ResourceMenuApi 实现：供 codegen 选择父级菜单。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ResourceMenuApiProvider implements ResourceMenuApi {

    private static final Set<String> PARENT_RESOURCE_TYPES = Set.of("CATALOG", "MENU", "PAGE");

    private final SysResourceMapper resourceMapper;
    private final SysResourceModuleMapper resourceModuleMapper;

    @Override
    public List<ResourceMenuNode> listParentMenus(String client, String moduleId) {
        String resolvedClient = StringUtils.hasText(client) ? client : "ADMIN";
        Set<String> moduleIds = resourceModuleMapper.selectList(Wrappers.<SysResourceModule>lambdaQuery()
                        .eq(SysResourceModule::getClient, resolvedClient)
                        .select(SysResourceModule::getId))
                .stream()
                .map(SysResourceModule::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (moduleIds.isEmpty()) {
            return List.of();
        }
        if (StringUtils.hasText(moduleId) && !moduleIds.contains(moduleId)) {
            return List.of();
        }

        List<SysResource> resources = resourceMapper.selectList(Wrappers.<SysResource>lambdaQuery()
                .eq(SysResource::getStatus, "ENABLED")
                .in(SysResource::getResourceType, PARENT_RESOURCE_TYPES)
                .in(SysResource::getModuleId, StringUtils.hasText(moduleId) ? List.of(moduleId) : moduleIds)
                .orderByAsc(SysResource::getSort)
                .orderByAsc(SysResource::getId));
        return resources.stream().map(this::toNode).toList();
    }

    private ResourceMenuNode toNode(SysResource resource) {
        ResourceMenuNode node = new ResourceMenuNode();
        node.setId(resource.getId());
        node.setParentId(resource.getParentId());
        node.setName(resource.getName());
        node.setResourceType(resource.getResourceType());
        node.setModuleId(resource.getModuleId());
        node.setSort(resource.getSort());
        return node;
    }
}
