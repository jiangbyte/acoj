package github.jiangbyte.io.iam.modules.client.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.iam.modules.client.convert.SysClientConvert;
import github.jiangbyte.io.iam.modules.client.entity.SysClientModule;
import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientModuleMapper;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePageParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceTreeParam;
import github.jiangbyte.io.iam.modules.client.service.ClientResourceService;
import github.jiangbyte.io.iam.support.audit.IamAuditLabelSupport;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.entity.SysIamRelation;
import github.jiangbyte.io.iam.modules.relation.mapper.SysIamRelationMapper;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import github.jiangbyte.io.iam.modules.relation.service.PermissionRegistryService;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantMenuOptionResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantModuleOptionResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourcePermissionOptionResult;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户端资源服务实现：资源树构建、CRUD 与权限关系绑定。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ClientResourceServiceImpl extends ServiceImpl<SysClientResourceMapper, SysClientResource>
        implements ClientResourceService {

    private static final String RESOURCE_TYPE_BUTTON = "BUTTON";
    private static final String RESOURCE_TYPE_ACTION = "ACTION";
    private static final Set<String> GRANT_MENU_TYPES = Set.of("MENU", "PAGE", "API_GROUP");

    private final SysClientModuleMapper moduleMapper;
    private final SysIamRelationMapper relationMapper;
    private final IamRelationService relationService;
    private final PermissionRegistryService permissionRegistryService;
    private final SysClientConvert clientConvert;
    private final TransService transService;

    @Override
    @Transactional
    public void create(SysClientResourceAddParam param) {
        SysClientResource resource = clientConvert.toEntity(param);
        this.save(resource);
        AuditSnapshots.created(resource);
    }

    @Override
    @Transactional
    public void update(SysClientResourceEditParam param) {
        SysClientResource resource = this.getById(param.getId());
        if (resource == null) {
            throw new BizException(404, "Client resource not found");
        }
        AuditSnapshots.before(resource);
        clientConvert.update(param, resource);
        this.updateById(resource);
        AuditSnapshots.after(resource);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysClientResource> resources = this.listByIds(ids);
        AuditSnapshots.deletedAll(resources);
        relationService.deleteBySubjectIds(IamRelationTypes.SUBJECT_CLIENT_RESOURCE, ids);
        relationService.deleteByTargetIds(IamRelationTypes.TARGET_CLIENT_RESOURCE, ids);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysClientResource detail(String id) {
        SysClientResource resource = this.getById(id);
        if (resource == null) {
            throw new BizException(404, "Client resource not found");
        }
        return resource;
    }

    @Override
    @ReadDataSource
    public Page<SysClientResource> page(SysClientResourcePageParam param) {
        Page<SysClientResource> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysClientResource>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), SysClientResource::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), SysClientResource::getName, param.getName())
                        .eq(StringUtils.hasText(param.getResourceType()), SysClientResource::getResourceType, param.getResourceType())
                        .eq(StringUtils.hasText(param.getModuleId()), SysClientResource::getModuleId, param.getModuleId())
                        .eq(StringUtils.hasText(param.getParentId()), SysClientResource::getParentId, param.getParentId())
                        .eq(StringUtils.hasText(param.getStatus()), SysClientResource::getStatus, param.getStatus())
                        .orderByAsc(SysClientResource::getSort));
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree(SysClientResourceTreeParam param) {
        // 1. 按模块/账号类型解析过滤范围
        Set<String> moduleIds = null;
        if (StringUtils.hasText(param.getModuleId())) {
            moduleIds = Set.of(param.getModuleId());
        } else if (StringUtils.hasText(param.getAccountType())) {
            moduleIds = moduleMapper.selectList(Wrappers.<SysClientModule>lambdaQuery()
                            .eq(SysClientModule::getAccountType, param.getAccountType()))
                    .stream()
                    .map(SysClientModule::getId)
                    .collect(Collectors.toSet());
            if (moduleIds.isEmpty()) {
                return List.of();
            }
        }
        // 2. 查询扁平资源并回填账号类型（按钮走独立能力，树中排除 BUTTON/ACTION）
        var wrapper = Wrappers.<SysClientResource>lambdaQuery()
                .notIn(SysClientResource::getResourceType, List.of(RESOURCE_TYPE_BUTTON, RESOURCE_TYPE_ACTION))
                .orderByAsc(SysClientResource::getSort);
        if (moduleIds != null) {
            wrapper.in(SysClientResource::getModuleId, moduleIds);
        }
        List<SysClientResource> all = this.getBaseMapper().selectList(wrapper);
        Map<String, String> accountTypeByModule = loadModuleAccountTypes(all.stream()
                .map(SysClientResource::getModuleId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList());
        for (SysClientResource resource : all) {
            if (StringUtils.hasText(resource.getModuleId())) {
                resource.setAccountType(accountTypeByModule.get(resource.getModuleId()));
            }
        }
        transService.transBatch(all);
        if (all.isEmpty()) {
            return List.of();
        }

        // 3. 断链父节点置空后组装树
        Set<String> ids = all.stream().map(SysClientResource::getId).collect(Collectors.toSet());
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parent_id");
        config.setNameKey("name");
        config.setWeightKey("weight");
        config.setChildrenKey("children");
        return TreeUtil.build(all, null, config, (resource, tree) -> {
            String parentId = resource.getParentId();
            if (!StringUtils.hasText(parentId) || !ids.contains(parentId)) {
                parentId = null;
            }
            BeanUtil.beanToMap(resource, false, true).forEach((key, value) -> {
                if (!"children".equals(key)) {
                    tree.putExtra(StrUtil.toUnderlineCase(key), value);
                }
            });
            tree.setId(resource.getId());
            tree.setParentId(parentId);
            tree.setName(resource.getName());
            tree.setWeight(resource.getSort() == null ? 0 : resource.getSort());
        });
    }

    @Override
    @Transactional
    public void bindPermission(SysClientResourcePermissionBindParam param) {
        // 绑定客户端资源-权限关系
        SysClientResource resource = this.getById(param.getResourceId());
        if (resource == null) {
            throw new BizException(404, "Client resource not found");
        }
        AuditSnapshots.subject(resource.getName());
        AuditSnapshots.resourceId(param.getResourceId());
        AuditSnapshots.before(Map.of());
        permissionRegistryService.ensureRegistered(param.getPermissionKey());
        relationService.bindClientResourcePermission(
                param.getResourceId(),
                param.getPermissionKey(),
                param.getAccountType(),
                param.getDataScope(),
                param.getCustomScopeDeptIds(),
                param.getSort(),
                param.getDescription());
        AuditSnapshots.after(IamAuditLabelSupport.permissionBindField(
                param.getPermissionKey(), param.getAccountType(), param.getDataScope()));
    }

    @Override
    @ReadDataSource
    public List<SysResourceGrantModuleOptionResult> listGrantModules(String accountType) {
        // 1. 加载启用客户端模块（可按账号类型过滤）
        var moduleQuery = Wrappers.<SysClientModule>lambdaQuery()
                .eq(SysClientModule::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysClientModule::getSort);
        if (StringUtils.hasText(accountType)) {
            moduleQuery.eq(SysClientModule::getAccountType, accountType);
        }
        List<SysClientModule> modules = moduleMapper.selectList(moduleQuery);
        Set<String> moduleIds = modules.stream().map(SysClientModule::getId).collect(Collectors.toSet());
        if (moduleIds.isEmpty() && StringUtils.hasText(accountType)) {
            return List.of();
        }

        // 2. 加载模块下启用客户端资源
        var resourceQuery = Wrappers.<SysClientResource>lambdaQuery()
                .eq(SysClientResource::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysClientResource::getSort);
        if (!moduleIds.isEmpty()) {
            resourceQuery.in(SysClientResource::getModuleId, moduleIds);
        }
        List<SysClientResource> resources = this.getBaseMapper().selectList(resourceQuery);
        if (resources.isEmpty()) {
            return List.of();
        }

        // 3. 按客户端资源汇总权限选项
        List<SysIamRelation> permissions = relationMapper.selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_CLIENT_RESOURCE)
                .eq(SysIamRelation::getRelationType, IamRelationTypes.CLIENT_RESOURCE_PERMISSION)
                .eq(StringUtils.hasText(accountType), SysIamRelation::getAccountType, accountType));

        Map<String, List<SysResourcePermissionOptionResult>> permissionMap = new HashMap<>();
        for (SysIamRelation permission : permissions) {
            SysResourcePermissionOptionResult option = new SysResourcePermissionOptionResult();
            option.setId(permission.getId());
            option.setPermissionKey(permission.getTargetKey());
            option.setTitle(StringUtils.hasText(permission.getDescription())
                    ? permission.getDescription()
                    : permission.getTargetKey());
            option.setDataScope(permission.getDataScope());
            permissionMap.computeIfAbsent(permission.getSubjectId(), key -> new ArrayList<>()).add(option);
        }

        // 4. 按钮/动作权限挂到父菜单（无关系时回退 code）
        Map<String, SysClientResource> resourceMap = resources.stream()
                .collect(Collectors.toMap(SysClientResource::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Map<String, List<SysResourcePermissionOptionResult>> childPermissionMap = new HashMap<>();
        for (SysClientResource resource : resources) {
            if (!RESOURCE_TYPE_BUTTON.equals(resource.getResourceType())
                    && !RESOURCE_TYPE_ACTION.equals(resource.getResourceType())) {
                continue;
            }
            if (!StringUtils.hasText(resource.getParentId())) {
                continue;
            }
            List<SysResourcePermissionOptionResult> options = permissionMap.get(resource.getId());
            if (options == null || options.isEmpty()) {
                SysResourcePermissionOptionResult fallback = new SysResourcePermissionOptionResult();
                fallback.setId(resource.getId());
                fallback.setPermissionKey(resource.getCode());
                fallback.setTitle(resource.getName());
                options = List.of(fallback);
            }
            childPermissionMap.computeIfAbsent(resource.getParentId(), key -> new ArrayList<>()).addAll(options);
        }

        // 5. 组装模块 → 菜单 → 按钮授权树
        Map<String, SysResourceGrantModuleOptionResult> moduleMap = new LinkedHashMap<>();
        Map<String, Integer> moduleSortMap = new HashMap<>();
        for (SysClientModule module : modules) {
            SysResourceGrantModuleOptionResult option = new SysResourceGrantModuleOptionResult();
            option.setId(module.getId());
            option.setTitle(module.getName());
            moduleMap.put(module.getId(), option);
            moduleSortMap.put(module.getId(), module.getSort() == null ? 99 : module.getSort());
        }

        for (SysClientResource resource : resources) {
            if (!GRANT_MENU_TYPES.contains(resource.getResourceType()) || !StringUtils.hasText(resource.getModuleId())) {
                continue;
            }
            String moduleId = resource.getModuleId();
            SysResourceGrantModuleOptionResult module = moduleMap.computeIfAbsent(moduleId, key -> {
                SysResourceGrantModuleOptionResult created = new SysResourceGrantModuleOptionResult();
                created.setId(key);
                created.setTitle(key);
                moduleSortMap.putIfAbsent(key, 99);
                return created;
            });
            SysClientResource parent = resourceMap.get(resource.getParentId());
            SysResourceGrantMenuOptionResult menu = new SysResourceGrantMenuOptionResult();
            menu.setId(resource.getId());
            menu.setModuleId(moduleId);
            menu.setParentId(resource.getParentId());
            menu.setParentIdName(parent != null ? parent.getName() : resource.getName());
            menu.setTitle(resource.getName());
            List<SysResourcePermissionOptionResult> buttons = new ArrayList<>();
            buttons.addAll(permissionMap.getOrDefault(resource.getId(), List.of()));
            buttons.addAll(childPermissionMap.getOrDefault(resource.getId(), List.of()));
            menu.setButton(buttons);
            module.getMenu().add(menu);
        }

        // 6. 过滤空模块并按排序返回
        return moduleMap.values().stream()
                .filter(item -> item.getMenu() != null && !item.getMenu().isEmpty())
                .sorted(Comparator
                        .comparing((SysResourceGrantModuleOptionResult item) ->
                                moduleSortMap.getOrDefault(item.getId(), 99))
                        .thenComparing(SysResourceGrantModuleOptionResult::getId))
                .toList();
    }

    private Map<String, String> loadModuleAccountTypes(List<String> moduleIds) {
        if (moduleIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        for (SysClientModule module : moduleMapper.selectByIds(moduleIds)) {
            map.put(module.getId(), module.getAccountType());
        }
        return map;
    }
}
