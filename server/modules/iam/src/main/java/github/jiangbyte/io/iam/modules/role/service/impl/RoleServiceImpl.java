package github.jiangbyte.io.iam.modules.role.service.impl;

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
import github.jiangbyte.io.iam.modules.client.service.ClientResourceService;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import github.jiangbyte.io.iam.modules.role.convert.SysRoleConvert;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import github.jiangbyte.io.iam.modules.role.param.SysRoleAddParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleEditParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantResourceParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleGrantUserParam;
import github.jiangbyte.io.iam.modules.role.param.SysRolePageParam;
import github.jiangbyte.io.iam.modules.role.service.RoleService;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.support.audit.IamAuditLabelSupport;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色服务实现：角色维护及资源/成员关系替换。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final IamRelationService relationService;
    private final TransService transService;
    private final SysRoleConvert roleConvert;
    private final DataScopeResolver dataScopeResolver;
    private final ResourceService resourceService;
    private final ClientResourceService clientResourceService;
    private final AccountService accountService;
    private final SysResourceMapper resourceMapper;
    private final SysClientResourceMapper clientResourceMapper;

    @Override
    @Transactional
    public void create(SysRoleAddParam param) {
        SysRole existing = this.getOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, param.getCode()).last("limit 1"));
        if (existing != null) {
            throw new BizException("Role code already exists");
        }
        SysRole role = roleConvert.toEntity(param);
        this.save(role);
        AuditSnapshots.created(role);
    }

    @Override
    @Transactional
    public void update(SysRoleEditParam param) {
        SysRole role = this.getById(param.getId());
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        SysRole existing = this.getOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, param.getCode()).last("limit 1"));
        if (existing != null && !role.getId().equals(existing.getId())) {
            throw new BizException("Role code already exists");
        }
        AuditSnapshots.before(role);
        roleConvert.update(param, role);
        this.updateById(role);
        AuditSnapshots.after(role);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        // 分批：先清主体/客体关系，再删角色
        for (List<String> batch : BatchPartition.partition(param.getIds())) {
            List<SysRole> roles = this.listByIds(batch);
            DataScopeConstraint scope = dataScopeResolver.resolve("iam:role:page");
            for (SysRole role : roles) {
                dataScopeResolver.assertOwnerOrDeptAccessible(
                        role.getCreatedBy(), role.getOwnerDeptId(), scope);
            }
            AuditSnapshots.deletedAll(roles);
            relationService.deleteBySubjectIds(IamRelationTypes.SUBJECT_ROLE, batch);
            relationService.deleteByTargetIds(IamRelationTypes.TARGET_ROLE, batch);
            this.removeByIds(batch);
        }
    }

    @Override
    @ReadDataSource
    public SysRole detail(String id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        return role;
    }

    @Override
    @ReadDataSource
    public Page<SysRole> page(SysRolePageParam param) {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
                .like(StringUtils.hasText(param.getCode()), SysRole::getCode, param.getCode())
                .like(StringUtils.hasText(param.getName()), SysRole::getName, param.getName())
                .eq(StringUtils.hasText(param.getCategory()), SysRole::getCategory, param.getCategory())
                .eq(StringUtils.hasText(param.getScopeType()), SysRole::getScopeType, param.getScopeType())
                .eq(StringUtils.hasText(param.getStatus()), SysRole::getStatus, param.getStatus())
                .orderByAsc(SysRole::getSort)
                .orderByDesc(SysRole::getId);
        dataScopeResolver.applyOwnerOrDept(wrapper, "iam:role:page", SysRole::getCreatedBy, SysRole::getOwnerDeptId);
        Page<SysRole> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownResources(String id, String accountType) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        // 拼装授权树 + 已授列表
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(resourceService.listGrantModules(accountType));
        result.setGrantInfoList(relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ROLE, id, accountType));
        return result;
    }

    @Override
    @Transactional
    public void grantResources(SysRoleGrantResourceParam param) {
        // 全量替换角色管理端资源
        SysRole role = this.getById(param.getId());
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        AuditSnapshots.subject(role.getName());
        AuditSnapshots.resourceId(role.getId());
        var beforeGrants = relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ROLE, param.getId(), param.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantResourceField("授权资源", beforeGrants, resourceMapper));
        relationService.replaceSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ROLE,
                param.getId(),
                param.getGrantInfoList(),
                param.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantResourceField(
                "授权资源", param.getGrantInfoList(), resourceMapper));
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownClientResources(String id, String accountType) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        // 拼装客户端授权树 + 已授列表
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(clientResourceService.listGrantModules(accountType));
        result.setGrantInfoList(relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ROLE, id, accountType));
        return result;
    }

    @Override
    @Transactional
    public void grantClientResources(SysRoleGrantResourceParam param) {
        // 全量替换角色客户端资源
        SysRole role = this.getById(param.getId());
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        AuditSnapshots.subject(role.getName());
        AuditSnapshots.resourceId(role.getId());
        var beforeGrants = relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ROLE, param.getId(), param.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantClientResourceField("授权资源", beforeGrants, clientResourceMapper));
        relationService.replaceSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ROLE,
                param.getId(),
                param.getGrantInfoList(),
                param.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantClientResourceField(
                "授权资源", param.getGrantInfoList(), clientResourceMapper));
    }

    @Override
    @ReadDataSource
    public SysOwnUserResult ownUsers(String id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        // 查角色成员 id，再回填用户详情
        List<String> accountIds = relationService.listSubjectIds(
                IamRelationTypes.ACCOUNT_ROLE, IamRelationTypes.TARGET_ROLE, id);
        SysOwnUserResult result = new SysOwnUserResult();
        result.setId(id);
        result.setAccountIds(accountIds);
        result.setUsers(accountService.listResultsByIds(accountIds));
        return result;
    }

    @Override
    @Transactional
    public void grantUsers(SysRoleGrantUserParam param) {
        // 全量替换角色成员
        SysRole role = this.getById(param.getId());
        if (role == null) {
            throw new BizException(404, "Role not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                role.getCreatedBy(), role.getOwnerDeptId(), "iam:role:page");
        AuditSnapshots.subject(role.getName());
        AuditSnapshots.resourceId(role.getId());
        List<String> beforeIds = relationService.listSubjectIds(
                IamRelationTypes.ACCOUNT_ROLE, IamRelationTypes.TARGET_ROLE, param.getId());
        AuditSnapshots.before(accountField(beforeIds));
        relationService.replaceRoleUsers(param.getId(), param.getAccountIds());
        AuditSnapshots.after(accountField(param.getAccountIds()));
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
}
