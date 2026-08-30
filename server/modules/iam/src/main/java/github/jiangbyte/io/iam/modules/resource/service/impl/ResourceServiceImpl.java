package github.jiangbyte.io.iam.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.entity.SysIamRelation;
import github.jiangbyte.io.iam.modules.relation.mapper.SysIamRelationMapper;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import github.jiangbyte.io.iam.modules.relation.service.PermissionRegistryService;
import github.jiangbyte.io.iam.modules.resource.convert.SysResourceConvert;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.entity.SysResourceModule;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceModuleMapper;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceButtonPageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePageParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourcePermissionBindParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceTreeParam;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceButtonResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantMenuOptionResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantModuleOptionResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourcePermissionOptionResult;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import github.jiangbyte.io.iam.support.audit.IamAuditLabelSupport;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端资源服务实现：资源树构建、模块/按钮维护与权限关系绑定。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements ResourceService {

    private static final String RESOURCE_TYPE_BUTTON = "BUTTON";
    private static final String RESOURCE_TYPE_ACTION = "ACTION";
    private static final String RESOURCE_TYPE_MENU = "MENU";
    private static final String RESOURCE_TYPE_PAGE = "PAGE";
    private static final String RESOURCE_TYPE_API_GROUP = "API_GROUP";
    private static final String MODULE_CLIENT_ADMIN = "ADMIN";
    private static final String MODULE_CLIENT_PORTAL = "PORTAL";
    private static final Set<String> GRANT_MENU_TYPES = Set.of(
            RESOURCE_TYPE_MENU, RESOURCE_TYPE_PAGE, RESOURCE_TYPE_API_GROUP);
    private final SysResourceModuleMapper moduleMapper;
    private final SysIamRelationMapper relationMapper;
    private final IamRelationService relationService;
    private final PermissionRegistryService permissionRegistryService;
    private final SysResourceConvert resourceConvert;
    private final TransService transService;

    @Override
    @Transactional
    public void create(SysResourceAddParam param) {
        SysResource resource = resourceConvert.toEntity(param);
        this.save(resource);
        AuditSnapshots.created(resource);
    }

    @Override
    @Transactional
    public void update(SysResourceEditParam param) {
        SysResource resource = this.getById(param.getId());
        if (resource == null) {
            throw new BizException(404, "Resource not found");
        }
        AuditSnapshots.before(resource);
        resourceConvert.update(param, resource);
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
        List<SysResource> resources = this.listByIds(ids);
        AuditSnapshots.deletedAll(resources);
        relationService.deleteBySubjectIds(IamRelationTypes.SUBJECT_RESOURCE, ids);
        relationService.deleteByTargetIds(IamRelationTypes.TARGET_RESOURCE, ids);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysResource detail(String id) {
        SysResource resource = this.getById(id);
        if (resource == null) {
            throw new BizException(404, "Resource not found");
        }
        return resource;
    }

    @Override
    @ReadDataSource
    public Page<SysResource> page(SysResourcePageParam param) {
        Page<SysResource> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysResource>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), SysResource::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), SysResource::getName, param.getName())
                        .eq(StringUtils.hasText(param.getResourceType()), SysResource::getResourceType, param.getResourceType())
                        .eq(StringUtils.hasText(param.getModuleId()), SysResource::getModuleId, param.getModuleId())
                        .eq(StringUtils.hasText(param.getStatus()), SysResource::getStatus, param.getStatus())
                        .orderByAsc(SysResource::getSort));
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public List<Tree<String>> tree(SysResourceTreeParam param) {
        // 1. 按模块/客户端解析过滤范围
        Set<String> moduleIds = null;
        if (StringUtils.hasText(param.getModuleId())) {
            moduleIds = Set.of(param.getModuleId());
        } else if (StringUtils.hasText(param.getModuleClient())) {
            moduleIds = moduleMapper.selectList(Wrappers.<SysResourceModule>lambdaQuery()
                            .eq(SysResourceModule::getClient, param.getModuleClient()))
                    .stream().map(SysResourceModule::getId).collect(Collectors.toSet());
            if (moduleIds.isEmpty()) {
                return List.of();
            }
        }
        // 2. 查询扁平资源并字典翻译（按钮走独立接口，树中排除 BUTTON/ACTION）
        var wrapper = Wrappers.<SysResource>lambdaQuery()
                .notIn(SysResource::getResourceType, List.of(RESOURCE_TYPE_BUTTON, RESOURCE_TYPE_ACTION))
                .orderByAsc(SysResource::getSort);
        if (moduleIds != null) {
            wrapper.in(SysResource::getModuleId, moduleIds);
        }
        List<SysResource> all = this.getBaseMapper().selectList(wrapper);
        transService.transBatch(all);
        if (all.isEmpty()) {
            return List.of();
        }

        // 3. 断链父节点置空后组装树
        Set<String> ids = all.stream().map(SysResource::getId).collect(Collectors.toSet());
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
    @ReadDataSource
    public List<SysResource> currentMenus() {
        // 按当前授权过滤可见菜单
        return listVisibleResources(MODULE_CLIENT_ADMIN);
    }

    @Override
    @ReadDataSource
    public List<SysResource> listPublicPortalResources() {
        return listEnabledResourcesByClient(MODULE_CLIENT_PORTAL);
    }

    @Override
    @Transactional
    public void bindPermission(SysResourcePermissionBindParam param) {
        // 绑定资源-权限关系
        SysResource resource = this.getById(param.getResourceId());
        if (resource == null) {
            throw new BizException(404, "Resource not found");
        }
        AuditSnapshots.subject(resource.getName());
        AuditSnapshots.resourceId(param.getResourceId());
        AuditSnapshots.before(Map.of());
        permissionRegistryService.ensureRegistered(param.getPermissionKey());
        relationService.bindResourcePermission(
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
    @Transactional
    public void createButton(SysResourceButtonAddParam param) {
        // 校验父节点合法后落库按钮，再绑定权限键与数据范围
        SysResource parent = this.getById(param.getParentId());
        if (parent == null) {
            throw new BizException(404, "Resource not found");
        }
        if (RESOURCE_TYPE_BUTTON.equals(parent.getResourceType())
                || RESOURCE_TYPE_ACTION.equals(parent.getResourceType())) {
            throw new BizException("Button resource cannot be parent resource");
        }
        permissionRegistryService.ensureRegistered(param.getPermissionKey());
        SysResource button = new SysResource();
        button.setParentId(parent.getId());
        button.setCode(param.getCode());
        button.setName(param.getName());
        button.setResourceType(RESOURCE_TYPE_BUTTON);
        button.setModuleId(parent.getModuleId());
        button.setSort(param.getSort() == null ? 99 : param.getSort());
        button.setIsVisible(false);
        button.setIsCache(false);
        button.setIsAffix(false);
        button.setStatus(StringUtils.hasText(param.getStatus()) ? param.getStatus() : IamRelationTypes.STATUS_ENABLED);
        button.setDescription(param.getDescription());
        this.save(button);
        AuditSnapshots.created(button);
        replaceButtonPermission(button.getId(), param.getPermissionKey(), param.getDataScope(),
                param.getCustomScopeDeptIds(), param.getSort(), param.getDescription());
    }

    @Override
    @Transactional
    public void updateButton(SysResourceButtonEditParam param) {
        SysResource button = this.getById(param.getId());
        if (button == null) {
            throw new BizException(404, "Resource not found");
        }
        if (!RESOURCE_TYPE_BUTTON.equals(button.getResourceType())) {
            throw new BizException("Resource is not a button");
        }
        SysResource parent = this.getById(param.getParentId());
        if (parent == null) {
            throw new BizException(404, "Resource not found");
        }
        if (RESOURCE_TYPE_BUTTON.equals(parent.getResourceType())
                || RESOURCE_TYPE_ACTION.equals(parent.getResourceType())) {
            throw new BizException("Button resource cannot be parent resource");
        }
        permissionRegistryService.ensureRegistered(param.getPermissionKey());
        AuditSnapshots.before(button);
        button.setParentId(parent.getId());
        button.setCode(param.getCode());
        button.setName(param.getName());
        button.setModuleId(parent.getModuleId());
        button.setSort(param.getSort() == null ? 99 : param.getSort());
        button.setStatus(StringUtils.hasText(param.getStatus()) ? param.getStatus() : IamRelationTypes.STATUS_ENABLED);
        button.setDescription(param.getDescription());
        this.updateById(button);
        AuditSnapshots.after(button);
        replaceButtonPermission(button.getId(), param.getPermissionKey(), param.getDataScope(),
                param.getCustomScopeDeptIds(), param.getSort(), param.getDescription());
    }

    @Override
    @Transactional
    public void deleteButtons(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 分批加载并校验均为 BUTTON
        List<SysResource> buttons = new ArrayList<>();
        for (List<String> batch : BatchPartition.partition(ids)) {
            buttons.addAll(this.listByIds(batch));
        }
        if (buttons.size() != new LinkedHashSet<>(ids).size()) {
            throw new BizException(404, "Resource not found");
        }
        for (SysResource button : buttons) {
            if (!RESOURCE_TYPE_BUTTON.equals(button.getResourceType())) {
                throw new BizException("Resource is not a button");
            }
        }
        AuditSnapshots.deletedAll(buttons);
        // 先批量删权限关系，再分批删按钮资源
        relationService.deleteSubjectRelations(
                IamRelationTypes.SUBJECT_RESOURCE,
                ids,
                IamRelationTypes.RESOURCE_PERMISSION);
        for (List<String> batch : BatchPartition.partition(ids)) {
            this.removeByIds(batch);
        }
    }

    @Override
    @ReadDataSource
    public Page<SysResourceButtonResult> pageButtons(SysResourceButtonPageParam param) {
        Page<SysResource> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysResource>lambdaQuery()
                        .eq(SysResource::getParentId, param.getParentId())
                        .eq(SysResource::getResourceType, RESOURCE_TYPE_BUTTON)
                        .like(StringUtils.hasText(param.getCode()), SysResource::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), SysResource::getName, param.getName())
                        .eq(StringUtils.hasText(param.getStatus()), SysResource::getStatus, param.getStatus())
                        .orderByAsc(SysResource::getSort)
                        .orderByDesc(SysResource::getId));
        List<SysResourceButtonResult> records = toButtonResults(page.getRecords());
        Page<SysResourceButtonResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional
    public void createModule(SysResourceModuleAddParam param) {
        SysResourceModule module = resourceConvert.toModuleEntity(param);
        moduleMapper.insert(module);
        AuditSnapshots.created(module);
    }

    @Override
    @Transactional
    public void updateModule(SysResourceModuleEditParam param) {
        SysResourceModule module = moduleMapper.selectById(param.getId());
        if (module == null) {
            throw new BizException(404, "Resource module not found");
        }
        AuditSnapshots.before(module);
        resourceConvert.updateModule(param, module);
        moduleMapper.updateById(module);
        AuditSnapshots.after(module);
    }

    @Override
    @Transactional
    public void deleteModules(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysResourceModule> modules = moduleMapper.selectByIds(ids);
        AuditSnapshots.deletedAll(modules);
        moduleMapper.deleteByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysResourceModule moduleDetail(String id) {
        SysResourceModule module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BizException(404, "Resource module not found");
        }
        return module;
    }

    @Override
    @ReadDataSource
    public Page<SysResourceModule> pageModules(SysResourcePageParam param) {
        return moduleMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysResourceModule>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), SysResourceModule::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), SysResourceModule::getName, param.getName())
                        .eq(StringUtils.hasText(param.getStatus()), SysResourceModule::getStatus, param.getStatus())
                        .orderByAsc(SysResourceModule::getSort));
    }

    @Override
    @ReadDataSource
    public List<SysResourceModule> moduleSelector() {
        return moduleMapper.selectList(Wrappers.<SysResourceModule>lambdaQuery()
                .eq(SysResourceModule::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysResourceModule::getSort));
    }

    @Override
    @ReadDataSource
    public List<SysResourceGrantModuleOptionResult> listGrantModules(String moduleClient) {
        // 1. 加载启用模块（可按客户端过滤）
        var moduleQuery = Wrappers.<SysResourceModule>lambdaQuery()
                .eq(SysResourceModule::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysResourceModule::getSort);
        if (StringUtils.hasText(moduleClient)) {
            moduleQuery.eq(SysResourceModule::getClient, moduleClient);
        }
        List<SysResourceModule> modules = moduleMapper.selectList(moduleQuery);
        Set<String> moduleIds = modules.stream().map(SysResourceModule::getId).collect(Collectors.toSet());
        if (moduleIds.isEmpty() && StringUtils.hasText(moduleClient)) {
            return List.of();
        }

        // 2. 加载模块下启用资源
        var resourceQuery = Wrappers.<SysResource>lambdaQuery()
                .eq(SysResource::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysResource::getSort);
        if (!moduleIds.isEmpty()) {
            resourceQuery.in(SysResource::getModuleId, moduleIds);
        }
        List<SysResource> resources = this.getBaseMapper().selectList(resourceQuery);
        if (resources.isEmpty()) {
            return List.of();
        }

        // 3. 按资源汇总权限选项
        List<SysIamRelation> permissions = relationMapper.selectList(Wrappers.<SysIamRelation>lambdaQuery()
                .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_RESOURCE)
                .eq(SysIamRelation::getRelationType, IamRelationTypes.RESOURCE_PERMISSION)
                .eq(StringUtils.hasText(moduleClient), SysIamRelation::getAccountType, moduleClient));

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
        Map<String, SysResource> resourceMap = resources.stream()
                .collect(Collectors.toMap(SysResource::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Map<String, List<SysResourcePermissionOptionResult>> childPermissionMap = new HashMap<>();
        for (SysResource resource : resources) {
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
        for (SysResourceModule module : modules) {
            SysResourceGrantModuleOptionResult option = new SysResourceGrantModuleOptionResult();
            option.setId(module.getId());
            option.setTitle(module.getName());
            moduleMap.put(module.getId(), option);
            moduleSortMap.put(module.getId(), module.getSort() == null ? 99 : module.getSort());
        }

        for (SysResource resource : resources) {
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
            SysResource parent = resourceMap.get(resource.getParentId());
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

    private void replaceButtonPermission(
            String buttonId,
            String permissionKey,
            String dataScope,
            List<String> customScopeDeptIds,
            Integer sort,
            String description) {
        relationService.deleteSubjectRelations(
                IamRelationTypes.SUBJECT_RESOURCE,
                buttonId,
                IamRelationTypes.RESOURCE_PERMISSION);
        relationService.bindResourcePermission(
                buttonId,
                permissionKey,
                AccountType.ADMIN.name(),
                dataScope,
                customScopeDeptIds,
                sort,
                description);
    }

    private List<SysResource> listVisibleResources(String moduleClient) {
        // 1. 取登录态权限标记；资源 id 不存会话，按需回源授权
        var loginUser = LoginHelper.currentUser()
                .orElseThrow(() -> new BizException(401, "未登录"));
        boolean superAdmin = loginUser.getRoles() != null
                && loginUser.getRoles().contains(IamRelationTypes.SUPER_ADMIN);
        boolean allPerms = loginUser.getPermissions() != null
                && loginUser.getPermissions().contains("*:*:*");
        List<String> resourceIds = null;
        if (!superAdmin && !allPerms) {
            AccountAuthorization auth = relationService.getAccountAuthorization(loginUser.getAccountId());
            superAdmin = auth.getRoleCodes().contains(IamRelationTypes.SUPER_ADMIN);
            allPerms = auth.getPermissionKeys().contains("*:*:*");
            resourceIds = auth.getResourceIds();
        }
        // 2. 超管/全权限返回客户端全部启用资源
        List<SysResource> resources;
        if (superAdmin || allPerms) {
            resources = listEnabledResourcesByClient(moduleClient);
        } else if (resourceIds == null || resourceIds.isEmpty()) {
            resources = List.of();
        } else {
            // 3. 普通账号按授权 id 补齐祖先后返回
            resources = listResourcesByIdsWithParents(resourceIds, moduleClient);
        }
        // 工作台作为各角色统一首页入口，管理端始终可见（不依赖单独授权）
        return ensureWorkbenchHome(resources, moduleClient);
    }

    /**
     * 确保管理端可见资源包含工作台菜单，便于任意角色进入统一首页。
     */
    private List<SysResource> ensureWorkbenchHome(List<SysResource> resources, String moduleClient) {
        if (!MODULE_CLIENT_ADMIN.equals(moduleClient)) {
            return resources;
        }
        boolean hasHome = resources.stream().anyMatch(item ->
                "workspace".equals(item.getCode()) || "/workspace".equals(item.getPath()));
        if (hasHome) {
            return resources;
        }
        Page<SysResource> homePage = this.getBaseMapper().selectPage(
                new Page<>(1, 1, false),
                Wrappers.<SysResource>lambdaQuery()
                        .eq(SysResource::getCode, "workspace")
                        .eq(SysResource::getStatus, IamRelationTypes.STATUS_ENABLED));
        SysResource home = homePage.getRecords().isEmpty() ? null : homePage.getRecords().get(0);
        if (home == null) {
            return resources;
        }
        Set<String> moduleIds = moduleIdsByClient(moduleClient);
        if (!moduleIds.contains(home.getModuleId())) {
            return resources;
        }
        List<SysResource> merged = new ArrayList<>(resources.size() + 1);
        merged.add(home);
        merged.addAll(resources);
        merged.sort(Comparator
                .comparing(SysResource::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SysResource::getId, Comparator.nullsLast(String::compareTo)));
        return merged;
    }

    private List<SysResource> listEnabledResourcesByClient(String moduleClient) {
        Set<String> moduleIds = moduleIdsByClient(moduleClient);
        if (moduleIds.isEmpty()) {
            return List.of();
        }
        return this.getBaseMapper().selectList(Wrappers.<SysResource>lambdaQuery()
                .in(SysResource::getModuleId, moduleIds)
                .eq(SysResource::getStatus, IamRelationTypes.STATUS_ENABLED)
                .orderByAsc(SysResource::getSort)
                .orderByAsc(SysResource::getId));
    }

    private List<SysResource> listResourcesByIdsWithParents(Collection<String> resourceIds, String moduleClient) {
        // 1. 先取客户端全部启用资源作索引
        List<SysResource> all = listEnabledResourcesByClient(moduleClient);
        if (all.isEmpty()) {
            return List.of();
        }
        Map<String, SysResource> byId = all.stream()
                .collect(Collectors.toMap(SysResource::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        // 2. 自授权节点向上补齐父链
        Set<String> selected = new LinkedHashSet<>();
        for (String resourceId : resourceIds) {
            SysResource current = byId.get(resourceId);
            while (current != null && selected.add(current.getId())) {
                current = StringUtils.hasText(current.getParentId()) ? byId.get(current.getParentId()) : null;
            }
        }
        // 3. 保持原排序过滤选中节点
        return all.stream().filter(item -> selected.contains(item.getId())).toList();
    }

    private Set<String> moduleIdsByClient(String moduleClient) {
        return moduleMapper.selectList(Wrappers.<SysResourceModule>lambdaQuery()
                        .eq(SysResourceModule::getClient, moduleClient))
                .stream()
                .map(SysResourceModule::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<SysResourceButtonResult> toButtonResults(List<SysResource> buttons) {
        if (buttons.isEmpty()) {
            return List.of();
        }
        List<String> ids = buttons.stream().map(SysResource::getId).toList();
        Map<String, SysIamRelation> permissionMap = relationMapper.selectList(Wrappers.<SysIamRelation>lambdaQuery()
                        .eq(SysIamRelation::getSubjectType, IamRelationTypes.SUBJECT_RESOURCE)
                        .eq(SysIamRelation::getRelationType, IamRelationTypes.RESOURCE_PERMISSION)
                        .in(SysIamRelation::getSubjectId, ids))
                .stream()
                .collect(Collectors.toMap(SysIamRelation::getSubjectId, item -> item, (a, b) -> a));
        List<SysResourceButtonResult> result = new ArrayList<>(buttons.size());
        for (SysResource button : buttons) {
            SysResourceButtonResult dto = resourceConvert.toButtonResult(button);
            SysIamRelation permission = permissionMap.get(button.getId());
            if (permission != null) {
                dto.setPermissionRelId(permission.getId());
                dto.setPermissionKey(permission.getTargetKey());
                dto.setDataScope(permission.getDataScope());
                dto.setCustomScopeDeptIds(permission.getCustomScopeDeptIds() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(permission.getCustomScopeDeptIds()));
                dto.setPermissionDescription(permission.getDescription());
            }
            result.add(dto);
        }
        return result;
    }

}
