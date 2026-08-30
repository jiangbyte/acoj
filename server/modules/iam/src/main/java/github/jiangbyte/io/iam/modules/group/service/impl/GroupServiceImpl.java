package github.jiangbyte.io.iam.modules.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.security.datascope.DataScopeConstraint;
import github.jiangbyte.io.iam.modules.account.result.SysAccountResult;
import github.jiangbyte.io.iam.modules.account.result.SysOwnUserResult;
import github.jiangbyte.io.iam.modules.account.service.AccountService;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.client.service.ClientResourceService;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.iam.modules.group.convert.SysGroupConvert;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.mapper.SysGroupMapper;
import github.jiangbyte.io.iam.modules.group.param.SysGroupAddParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupEditParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantResourceParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantRoleParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupGrantUserParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupPageParam;
import github.jiangbyte.io.iam.modules.group.result.SysGroupOwnRoleResult;
import github.jiangbyte.io.iam.modules.group.service.GroupService;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import github.jiangbyte.io.iam.support.audit.IamAuditLabelSupport;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户组服务实现：组维护及成员、角色、资源关系替换。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements GroupService {

    private final IamRelationService relationService;
    private final TransService transService;
    private final DataScopeResolver dataScopeResolver;
    private final SysGroupConvert groupConvert;
    private final AccountService accountService;
    private final ResourceService resourceService;
    private final ClientResourceService clientResourceService;
    private final SysRoleMapper roleMapper;
    private final SysResourceMapper resourceMapper;
    private final SysClientResourceMapper clientResourceMapper;

    @Override
    @Transactional
    public void create(SysGroupAddParam param) {
        SysGroup group = groupConvert.toEntity(param);
        this.save(group);
        AuditSnapshots.created(group);
    }

    @Override
    @Transactional
    public void update(SysGroupEditParam param) {
        SysGroup group = this.getById(param.getId());
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        AuditSnapshots.before(group);
        groupConvert.update(param, group);
        this.updateById(group);
        AuditSnapshots.after(group);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysGroup> groups = this.listByIds(ids);
        DataScopeConstraint scope = dataScopeResolver.resolve("iam:group:page");
        for (SysGroup group : groups) {
            dataScopeResolver.assertOwnerOrDeptAccessible(
                    group.getCreatedBy(), group.getOwnerDeptId(), scope);
        }
        AuditSnapshots.deletedAll(groups);
        // 先清主体/客体关系，再删用户组
        relationService.deleteBySubjectIds(IamRelationTypes.SUBJECT_GROUP, ids);
        relationService.deleteByTargetIds(IamRelationTypes.TARGET_GROUP, ids);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysGroup detail(String id) {
        SysGroup group = this.getById(id);
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        return group;
    }

    @Override
    @ReadDataSource
    public Page<SysGroup> page(SysGroupPageParam param) {
        LambdaQueryWrapper<SysGroup> wrapper = Wrappers.<SysGroup>lambdaQuery()
                .like(StringUtils.hasText(param.getName()), SysGroup::getName, param.getName())
                .eq(StringUtils.hasText(param.getStatus()), SysGroup::getStatus, param.getStatus())
                .orderByDesc(SysGroup::getId);
        dataScopeResolver.applyOwnerOrDept(wrapper, "iam:group:page", SysGroup::getCreatedBy, SysGroup::getOwnerDeptId);
        Page<SysGroup> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public SysOwnUserResult ownUsers(String id) {
        SysGroup group = this.getById(id);
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        // 查分组成员 id，再回填用户详情
        List<String> accountIds = relationService.listSubjectIds(
                IamRelationTypes.ACCOUNT_GROUP, IamRelationTypes.TARGET_GROUP, id);
        SysOwnUserResult result = new SysOwnUserResult();
        result.setId(id);
        result.setAccountIds(accountIds);
        result.setUsers(accountService.listResultsByIds(accountIds));
        return result;
    }

    @Override
    @Transactional
    public void grantUsers(SysGroupGrantUserParam param) {
        // 全量替换用户组成员
        SysGroup group = this.getById(param.getId());
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        List<String> beforeAccountIds = relationService.listSubjectIds(
                IamRelationTypes.ACCOUNT_GROUP, IamRelationTypes.TARGET_GROUP, param.getId());
        AuditSnapshots.subject(group.getName());
        AuditSnapshots.resourceId(param.getId());
        AuditSnapshots.before(accountField(beforeAccountIds));
        relationService.replaceGroupUsers(param.getId(), param.getAccountIds());
        AuditSnapshots.after(accountField(param.getAccountIds()));
    }

    @Override
    @ReadDataSource
    public SysGroupOwnRoleResult ownRoles(String id, String accountType) {
        SysGroup group = this.getById(id);
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        // 查已绑角色 id，再按序回填角色实体
        List<String> roleIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_GROUP, id, IamRelationTypes.GROUP_ROLE, accountType);
        SysGroupOwnRoleResult result = new SysGroupOwnRoleResult();
        result.setId(id);
        result.setRoleIds(roleIds);
        result.setRoles(loadRolesByIds(roleIds));
        return result;
    }

    @Override
    @Transactional
    public void grantRoles(SysGroupGrantRoleParam param) {
        // 全量替换用户组角色
        SysGroup group = this.getById(param.getId());
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        List<String> beforeRoleIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_GROUP, param.getId(), IamRelationTypes.GROUP_ROLE, param.getAccountType());
        AuditSnapshots.subject(group.getName());
        AuditSnapshots.resourceId(param.getId());
        AuditSnapshots.before(IamAuditLabelSupport.roleIdsField(beforeRoleIds, roleMapper));
        relationService.replaceGroupRoles(param.getId(), param.getRoleIds(), param.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.roleIdsField(param.getRoleIds(), roleMapper));
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownResources(String id, String accountType) {
        SysGroup group = this.getById(id);
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        // 拼装授权树 + 已授列表
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(resourceService.listGrantModules(accountType));
        result.setGrantInfoList(relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_GROUP, id, accountType));
        return result;
    }

    @Override
    @Transactional
    public void grantResources(SysGroupGrantResourceParam param) {
        // 全量替换用户组管理端资源
        SysGroup group = this.getById(param.getId());
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        AuditSnapshots.subject(group.getName());
        AuditSnapshots.resourceId(param.getId());
        var beforeGrants = relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_GROUP, param.getId(), param.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantResourceField("授权资源", beforeGrants, resourceMapper));
        relationService.replaceSubjectResourceGrants(
                IamRelationTypes.SUBJECT_GROUP,
                param.getId(),
                param.getGrantInfoList(),
                param.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantResourceField(
                "授权资源", param.getGrantInfoList(), resourceMapper));
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownClientResources(String id, String accountType) {
        SysGroup group = this.getById(id);
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        // 拼装客户端授权树 + 已授列表
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(clientResourceService.listGrantModules(accountType));
        result.setGrantInfoList(relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_GROUP, id, accountType));
        return result;
    }

    @Override
    @Transactional
    public void grantClientResources(SysGroupGrantResourceParam param) {
        // 全量替换用户组客户端资源
        SysGroup group = this.getById(param.getId());
        if (group == null) {
            throw new BizException(404, "Group not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                group.getCreatedBy(), group.getOwnerDeptId(), "iam:group:page");
        AuditSnapshots.subject(group.getName());
        AuditSnapshots.resourceId(param.getId());
        var beforeGrants = relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_GROUP, param.getId(), param.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantClientResourceField("授权资源", beforeGrants, clientResourceMapper));
        relationService.replaceSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_GROUP,
                param.getId(),
                param.getGrantInfoList(),
                param.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantClientResourceField(
                "授权资源", param.getGrantInfoList(), clientResourceMapper));
    }

    private Map<String, Object> accountField(List<String> accountIds) {
        List<String> ids = accountIds == null ? List.of() : accountIds;
        Map<String, String> labelMap = accountService.listResultsByIds(ids).stream()
                .collect(Collectors.toMap(
                        SysAccountResult::getId,
                        item -> StringUtils.hasText(item.getName()) ? item.getName()
                                : (StringUtils.hasText(item.getAccount()) ? item.getAccount() : item.getId()),
                        (a, b) -> a));
        return IamAuditLabelSupport.accountIdsField(ids, id -> labelMap.getOrDefault(id, id));
    }

    private List<SysRole> loadRolesByIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        // 分批加载后按入参顺序回填
        Map<String, SysRole> roles = new HashMap<>();
        for (List<String> batch : BatchPartition.partition(roleIds)) {
            for (SysRole role : roleMapper.selectByIds(batch)) {
                roles.put(role.getId(), role);
            }
        }
        List<SysRole> result = new ArrayList<>();
        for (String roleId : roleIds) {
            SysRole role = roles.get(roleId);
            if (role != null) {
                result.add(role);
            }
        }
        return result;
    }
}
