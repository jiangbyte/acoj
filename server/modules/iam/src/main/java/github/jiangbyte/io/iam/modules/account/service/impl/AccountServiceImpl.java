package github.jiangbyte.io.iam.modules.account.service.impl;

import github.jiangbyte.io.iam.modules.account.service.AccountService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.auth.login.PasswordCryptoApi;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.DataSourceSticky;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.common.security.account.AccountLoginSupport;
import github.jiangbyte.io.iam.modules.account.convert.SysAccountConvert;
import github.jiangbyte.io.iam.modules.account.entity.SysAccount;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountIdentity;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountOauthBinding;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountIdentityMapper;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountMapper;
import github.jiangbyte.io.iam.modules.account.service.AccountOauthService;
import github.jiangbyte.io.iam.modules.account.param.SysAccountAddParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountEditParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountUpdateLoginIdentityParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantDeptParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantGroupParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantResourceParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantRoleParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountPageParam;
import github.jiangbyte.io.iam.modules.account.result.AccountIdentityResult;
import github.jiangbyte.io.iam.modules.account.result.AccountOauthBindingResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountListResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnDeptResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnGroupResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnRoleResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountResult;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import github.jiangbyte.io.iam.modules.account.support.AccountLifecycleNotifier;
import github.jiangbyte.io.iam.modules.account.support.PasswordHelper;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientResourceMapper;
import github.jiangbyte.io.iam.modules.client.service.ClientResourceService;
import github.jiangbyte.io.iam.modules.dept.mapper.SysDeptMapper;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.mapper.SysGroupMapper;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import github.jiangbyte.io.iam.modules.relation.service.IamRelationService;
import github.jiangbyte.io.iam.modules.resource.mapper.SysResourceMapper;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import github.jiangbyte.io.iam.support.audit.IamAuditLabelSupport;
import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.mapper.SysRoleMapper;
import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.profile.ProfileIdentityApi;
import github.jiangbyte.io.profile.ProfileIdentityStatusInfo;
import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.admin.ProfileUserAdminInfo;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * 账号领域服务实现：账号生命周期、密码策略、IAM 关系授权替换，
 * 以及详情组装与数据权限过滤。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<SysAccountMapper, SysAccount> implements AccountService {
    private final SysAccountIdentityMapper identityMapper;
    private final ProfileUserAdminApi adminUserProfileApi;
    private final ProfileUserPortalApi portalUserProfileApi;
    private final FileApi fileApi;
    private final IamRelationService relationService;
    private final PasswordHelper passwordHelper;
    private final PasswordCryptoApi passwordCryptoApi;
    private final AccountLifecycleNotifier accountLifecycleNotifier;
    private final SysAccountConvert accountConvert;
    private final DataScopeResolver dataScopeResolver;
    private final SysRoleMapper roleMapper;
    private final SysGroupMapper groupMapper;
    private final SysDeptMapper deptMapper;
    private final SysResourceMapper resourceMapper;
    private final SysClientResourceMapper clientResourceMapper;
    private final ResourceService resourceService;
    private final ClientResourceService clientResourceService;
    private final ConfigApi configApi;
    private final AccountOauthService accountOauthService;
    private final ProfileIdentityApi profileIdentityApi;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public SysAccount findByIdentifier(String identifier, String identityType) {
        if (!StringUtils.hasText(identifier)) {
            return null;
        }
        // 按标识查已绑定身份（账号型免核验，其它须 verified）
        List<String> types = StringUtils.hasText(identityType)
                ? List.of(identityType)
                : List.of("ACCOUNT");
        List<SysAccountIdentity> identities = identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getIdentifier, identifier)
                .eq(SysAccountIdentity::getBindStatus, "BOUND")
                .in(SysAccountIdentity::getIdentityType, types)
                .and(w -> w.eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                        .or()
                        .eq(SysAccountIdentity::getVerified, true)));
        if (identities.isEmpty()) {
            return null;
        }
        // 按账号 > 邮箱 > 手机优先级取首条对应账号
        identities.sort(Comparator.comparingInt(item -> identityTypeOrder(item.getIdentityType())));
        return getBaseMapper().selectById(identities.getFirst().getAccountId());
    }

    private static int identityTypeOrder(String identityType) {
        if ("ACCOUNT".equals(identityType)) {
            return 1;
        }
        if ("EMAIL".equals(identityType)) {
            return 2;
        }
        if ("PHONE".equals(identityType)) {
            return 3;
        }
        return 9;
    }

    @Override
    public String primaryAccountIdentifier(String accountId) {
        SysAccountIdentity identity = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                .orderByDesc(SysAccountIdentity::getIsPrimary)
                .last("limit 1"));
        return identity == null ? null : identity.getIdentifier();
    }

    @Override
    @Transactional
    public void updateLoginMeta(String accountId, String ip, OffsetDateTime time, String device) {
        SysAccount account = getBaseMapper().selectById(accountId);
        if (account == null) {
            return;
        }
        account.setLatestLoginIp(ip);
        account.setLatestLoginTime(time);
        account.setLatestLoginDevice(device);
        getBaseMapper().updateById(account);
    }

    @Override
    public AccountAuthorization getAuthorization(String accountId) {
        // 委托关系服务聚合授权视图
        return relationService.getAccountAuthorization(accountId);
    }

    @Override
    public boolean matchesPassword(String rawPassword, String passwordHash) {
        return passwordHelper.matches(rawPassword, passwordHash);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordHelper.encode(rawPassword);
    }

    @Override
    public boolean isPasswordExpired(String accountId, int expireDays) {
        return passwordHelper.isPasswordExpired(accountId, expireDays);
    }

    @Override
    public Integer getPasswordAgeDays(String accountId) {
        return passwordHelper.getPasswordAgeDays(accountId);
    }

    @Override
    public String findIdentifier(String accountId, String identityType) {
        SysAccountIdentity identity = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, identityType)
                .eq(SysAccountIdentity::getBindStatus, "BOUND")
                .last("limit 1"));
        return identity == null ? null : identity.getIdentifier();
    }

    @Override
    public void recordPasswordHistory(String accountId, String rawPassword, String operatorId, String reason) {
        passwordHelper.recordHistory(accountId, rawPassword, operatorId, reason);
    }

    @Override
    @Transactional
    public SysAccount createPortalAccount(String account, String email, String encodedPassword) {
        SysAccount entity = new SysAccount();
        entity.setPasswordHash(encodedPassword);
        entity.setAccountType(AccountType.PORTAL.name());
        entity.setAccountStatus("ENABLED");
        this.save(entity);

        insertIdentity(entity.getId(), "ACCOUNT", account, true, true);

        if (StringUtils.hasText(email)) {
            insertIdentity(entity.getId(), "EMAIL", email.trim().toLowerCase(Locale.ROOT), true, false);
        }
        return entity;
    }

    @Override
    @Transactional
    public void updatePasswordHash(String accountId, String passwordHash) {
        SysAccount account = getBaseMapper().selectById(accountId);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        account.setPasswordHash(passwordHash);
        this.updateById(account);
    }

    @Override
    @Transactional
    public void cancelAccount(String accountId, String cancelledBy, String cancelReason) {
        DataSourceSticky.mark();
        // 校验账号存在且未注销
        SysAccount account = getBaseMapper().selectById(accountId);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        if ("CANCELLED".equalsIgnoreCase(account.getAccountStatus())) {
            throw new BizException("账号已注销");
        }
        // 收集通知联系人，写入注销元数据并作废密码、清空登录痕迹
        String[] contacts = collectCancelNotifyContacts(accountId);
        OffsetDateTime now = OffsetDateTime.now();
        account.setAccountStatus("CANCELLED");
        if (account.getCancelledAt() == null) {
            account.setCancelledAt(now);
        }
        account.setCancelledBy(cancelledBy);
        account.setCancelReason(cancelReason);
        account.setCancelNotifyEmail(contacts[0]);
        account.setCancelNotifyPhone(contacts[1]);
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        account.setPasswordHash(passwordHelper.encode("cancelled:" + Base64.getUrlEncoder().withoutPadding().encodeToString(token)));
        account.setLastLoginIp(null);
        account.setLastLoginAddress(null);
        account.setLastLoginDevice(null);
        account.setLatestLoginIp(null);
        account.setLatestLoginAddress(null);
        account.setLatestLoginDevice(null);
        this.updateById(account);
        // 保留期内不清侧车数据（防标识抢注）；仅踢会话并通知
        clearAccountSessions(account);
        LoginHelper.logoutAccount(accountId);
        accountLifecycleNotifier.notifyCancelled(
                account.getCancelNotifyEmail(),
                account.getCancelNotifyPhone(),
                account.getCancelledAt());
    }

    @Override
    @Transactional
    public void upsertIdentity(String accountId, String type, String identifier, boolean enabled) {
        // 查本账号同类型身份；关闭或空标识则解绑
        SysAccountIdentity existing = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, type)
                .last("limit 1"));
        if (!enabled || !StringUtils.hasText(identifier)) {
            if (existing != null) {
                existing.setBindStatus("UNBOUND");
                identityMapper.updateById(existing);
            }
            return;
        }
        // 跨账号冲突校验后插入或更新为已绑定
        SysAccountIdentity conflict = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getIdentityType, type)
                .eq(SysAccountIdentity::getIdentifier, identifier)
                .last("limit 1"));
        if (conflict != null && !accountId.equals(conflict.getAccountId())) {
            throw new BizException("Account identifier already exists");
        }
        if (existing == null) {
            insertIdentity(accountId, type, identifier, true, false);
            return;
        }
        existing.setIdentifier(identifier);
        existing.setVerified(true);
        existing.setBindStatus("BOUND");
        identityMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void assignRole(String accountId, String roleId) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(roleId)) {
            return;
        }
        relationService.replaceAccountRoles(accountId, List.of(roleId.trim()));
    }

    @Override
    @Transactional
    public void assignPrimaryDept(String accountId, String deptId) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(deptId)) {
            return;
        }
        SysDeptGrantResult grant = new SysDeptGrantResult();
        grant.setDeptId(deptId.trim());
        grant.setIsPrimary(true);
        relationService.replaceAccountDepts(accountId, List.of(grant));
    }

    @Override
    @Transactional
    public void create(SysAccountAddParam param) {
        String accountLogin = AccountLoginSupport.requireLogin(param.getAccount());
        // 校验状态、登录身份与账号标识唯一
        if ("CANCELLED".equalsIgnoreCase(param.getAccountStatus())) {
            throw new BizException("注销状态不允许通过管理端设置");
        }
        SysAccountIdentity existingAccount = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                .eq(SysAccountIdentity::getIdentifier, accountLogin)
                .last("limit 1"));
        if (existingAccount != null) {
            throw new BizException("Account identifier already exists");
        }
        // 解析密码、规范化类型后落库账号
        String rawPassword = resolveCreatePassword(param.getPassword(), param.getPasswordKeyId());
        param.setAccount(accountLogin);
        SysAccount account = accountConvert.toEntity(param);
        account.setPasswordHash(passwordHelper.encode(rawPassword));
        if (!StringUtils.hasText(param.getAccountType())) {
            throw new BizException("Unsupported account type: " + param.getAccountType());
        }
        String accountType = param.getAccountType().trim().toUpperCase(Locale.ROOT);
        if (!"ADMIN".equals(accountType) && !"PORTAL".equals(accountType)) {
            throw new BizException("Unsupported account type: " + param.getAccountType());
        }
        account.setAccountType(accountType);
        if (!StringUtils.hasText(account.getAccountStatus())) {
            account.setAccountStatus("ENABLED");
        }
        this.save(account);
        replaceAccountLoginIdentity(account.getId(), param.getAccount());
        upsertProfile(account, param.getNickname(), param.getAvatar(),
                param.getSignature(), param.getPhone(), param.getEmail(), param.getRemark());
        AuditSnapshots.created(account);
    }

    @Override
    @Transactional
    public void update(SysAccountEditParam param) {
        DataSourceSticky.mark();
        String accountLogin = AccountLoginSupport.requireLogin(param.getAccount());
        // 加载账号并校验状态、登录身份与账号标识唯一
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        if ("CANCELLED".equalsIgnoreCase(account.getAccountStatus())) {
            throw new BizException("已注销账号不允许通过管理端修改");
        }
        if ("CANCELLED".equalsIgnoreCase(param.getAccountStatus())) {
            throw new BizException("注销状态不允许通过管理端设置");
        }
        SysAccountIdentity existingAccount = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                .eq(SysAccountIdentity::getIdentifier, accountLogin)
                .last("limit 1"));
        if (existingAccount != null && !account.getId().equals(existingAccount.getAccountId())) {
            throw new BizException("Account identifier already exists");
        }
        String previousStatus = account.getAccountStatus();
        AuditSnapshots.before(account);
        // 合并字段、可选改密后落库
        param.setAccount(accountLogin);
        accountConvert.update(param, account);
        if (!StringUtils.hasText(param.getAccountType())) {
            throw new BizException("Unsupported account type: " + param.getAccountType());
        }
        String accountType = param.getAccountType().trim().toUpperCase(Locale.ROOT);
        if (!"ADMIN".equals(accountType) && !"PORTAL".equals(accountType)) {
            throw new BizException("Unsupported account type: " + param.getAccountType());
        }
        account.setAccountType(accountType);
        if (StringUtils.hasText(param.getPassword())) {
            String rawPassword = passwordCryptoApi.decryptPassword(param.getPasswordKeyId(), param.getPassword());
            account.setPasswordHash(passwordHelper.encode(rawPassword));
        }
        this.updateById(account);
        replaceAccountLoginIdentity(account.getId(), param.getAccount());
        upsertProfile(account, param.getNickname(), param.getAvatar(),
                param.getSignature(), param.getPhone(), param.getEmail(), param.getRemark());
        AuditSnapshots.after(account);
        String nextStatus = account.getAccountStatus();
        if (StringUtils.hasText(nextStatus)
                && !"ENABLED".equalsIgnoreCase(nextStatus)
                && (previousStatus == null || "ENABLED".equalsIgnoreCase(previousStatus)
                        || !previousStatus.equalsIgnoreCase(nextStatus))) {
            LoginHelper.logoutAccount(account.getId());
        }
    }

    private String resolveCreatePassword(String password, String passwordKeyId) {
        String resolved = passwordCryptoApi.decryptPassword(passwordKeyId, password);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        String defaults = configApi.getValue("AUTH_DEFAULT_PASSWORD", "");
        if (StringUtils.hasText(defaults)) {
            return defaults.trim();
        }
        throw new BizException("Password is required");
    }

    @Override
    @Transactional
    public void updateLoginIdentity(SysAccountUpdateLoginIdentityParam param) {
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:update");
        if ("CANCELLED".equalsIgnoreCase(account.getAccountStatus())) {
            throw new BizException("已注销账号不允许通过管理端修改");
        }
        if (Boolean.TRUE.equals(param.getEmailLoginEnabled())
                && !StringUtils.hasText(param.getEmail())) {
            throw new BizException("Email login requires an email");
        }
        if (Boolean.TRUE.equals(param.getPhoneLoginEnabled())
                && !StringUtils.hasText(param.getPhone())) {
            throw new BizException("Phone login requires a phone");
        }
        replaceSecondaryLoginIdentities(
                account.getId(),
                param.getEmailLoginEnabled(),
                param.getEmail(),
                param.getPhoneLoginEnabled(),
                param.getPhone());
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        // 去重并确认全部存在
        List<String> uniqueIds = param.getIds().stream().filter(StringUtils::hasText).distinct().toList();
        List<SysAccount> accounts = getBaseMapper().selectByIds(uniqueIds);
        if (accounts.size() != uniqueIds.size()) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountsAccessible(uniqueIds, "iam:account:page");
        AuditSnapshots.deletedAll(accounts);
        // 分批清侧车数据后删账号，再踢会话
        for (List<String> batch : BatchPartition.partition(uniqueIds)) {
            cleanupAccountSideData(batch);
            this.removeByIds(batch);
        }
        for (SysAccount account : accounts) {
            clearAccountSessions(account);
        }
    }

    @Override
    @Transactional
    @Lock4j(keys = {"'iam:account:purge-cancelled'"}, expire = 120000, acquireTimeout = 3000)
    public int purgeExpiredCancelledAccounts(int retentionDays) {
        // 按保留期筛出可物理清理的已注销账号
        int days = retentionDays > 0 ? retentionDays : 15;
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(days);
        List<SysAccount> expired = getBaseMapper().selectList(Wrappers.<SysAccount>lambdaQuery()
                .eq(SysAccount::getAccountStatus, "CANCELLED")
                .isNotNull(SysAccount::getCancelledAt)
                .le(SysAccount::getCancelledAt, cutoff)
                .and(w -> w.isNull(SysAccount::getLatestLoginTime)
                        .or()
                        .le(SysAccount::getLatestLoginTime, cutoff)));
        if (expired.isEmpty()) {
            return 0;
        }
        // 保留期结束后清侧车并物理删除，再发清理通知
        List<String> ids = expired.stream().map(SysAccount::getId).toList();
        for (List<String> batch : BatchPartition.partition(ids)) {
            cleanupAccountSideData(batch);
            this.removeByIds(batch);
        }
        for (SysAccount account : expired) {
            clearAccountSessions(account);
            LoginHelper.logoutAccount(account.getId());
        }
        OffsetDateTime purgedAt = OffsetDateTime.now();
        for (SysAccount account : expired) {
            accountLifecycleNotifier.notifyPurged(
                    account.getCancelNotifyEmail(),
                    account.getCancelNotifyPhone(),
                    purgedAt);
        }
        return ids.size();
    }

    @Override
    @ReadDataSource
    public SysAccountResult detail(String id) {
        SysAccount account = getBaseMapper().selectById(id);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:detail");
        Map<String, SysAccountResult> map = toResultMap(List.of(account));
        SysAccountResult result = map.get(id);
        if (result != null) {
            ProfileIdentityStatusInfo identityStatus = profileIdentityApi.getStatusForAccount(id);
            result.setIdentityStatus(identityStatus);
            if (identityStatus != null && StringUtils.hasText(identityStatus.getRealNameMasked())) {
                result.setName(identityStatus.getRealNameMasked());
            }
        }
        return result;
    }

    @Override
    @ReadDataSource
    public Page<SysAccountListResult> page(SysAccountPageParam param) {
        // 账号标识模糊命中 → 候选 ID 集
        Set<String> scopedIds = null;
        if (StringUtils.hasText(param.getAccount())) {
            scopedIds = identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                            .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                            .like(SysAccountIdentity::getIdentifier, param.getAccount()))
                    .stream()
                    .map(SysAccountIdentity::getAccountId)
                    .collect(Collectors.toCollection(HashSet::new));
            if (scopedIds.isEmpty()) {
                return new Page<>(param.getCurrent(), param.getSize(), 0);
            }
        }
        // 档案条件并集再与身份候选求交
        Set<String> profileIds = findAccountIdsByProfileFilters(
                param.getName(), param.getPhone(), param.getEmail());
        if (profileIds != null) {
            if (profileIds.isEmpty()) {
                return new Page<>(param.getCurrent(), param.getSize(), 0);
            }
            if (scopedIds == null) {
                scopedIds = new HashSet<>(profileIds);
            } else {
                scopedIds.retainAll(profileIds);
                if (scopedIds.isEmpty()) {
                    return new Page<>(param.getCurrent(), param.getSize(), 0);
                }
            }
        }
        // 叠加数据权限与类型/状态后分页，再批量组装结果
        LambdaQueryWrapper<SysAccount> wrapper = Wrappers.lambdaQuery();
        if (scopedIds != null) {
            wrapper.in(SysAccount::getId, scopedIds);
        }
        dataScopeResolver.applyAccountScope(wrapper, "iam:account:page", SysAccount::getId);
        wrapper.eq(StringUtils.hasText(param.getAccountType()), SysAccount::getAccountType, param.getAccountType());
        wrapper.eq(StringUtils.hasText(param.getAccountStatus()), SysAccount::getAccountStatus, param.getAccountStatus());
        wrapper.orderByDesc(SysAccount::getId);
        Page<SysAccount> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);
        Map<String, SysAccountListResult> resultMap = toListResultMap(page.getRecords());
        List<SysAccountListResult> records = page.getRecords().stream()
                .map(item -> resultMap.get(item.getId()))
                .filter(Objects::nonNull)
                .toList();
        Page<SysAccountListResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @ReadDataSource
    public SysAccountOwnRoleResult ownRoles(String id) {
        SysAccount account = getBaseMapper().selectById(id);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        List<String> roleIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_ACCOUNT,
                id,
                IamRelationTypes.ACCOUNT_ROLE,
                account.getAccountType());
        SysAccountOwnRoleResult result = new SysAccountOwnRoleResult();
        result.setId(id);
        result.setRoleIds(roleIds);
        result.setRoles(loadRolesByIds(roleIds));
        return result;
    }

    @Override
    @Transactional
    public void grantRoles(SysAccountGrantRoleParam param) {
        // 全量替换账号角色关系
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(param.getId(), "iam:account:page");
        List<String> beforeRoleIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_ACCOUNT,
                param.getId(),
                IamRelationTypes.ACCOUNT_ROLE,
                account.getAccountType());
        AuditSnapshots.subject(primaryAccountIdentifier(account.getId()));
        AuditSnapshots.resourceId(param.getId());
        AuditSnapshots.before(IamAuditLabelSupport.roleIdsField(beforeRoleIds, roleMapper));
        relationService.replaceAccountRoles(param.getId(), param.getRoleIds());
        AuditSnapshots.after(IamAuditLabelSupport.roleIdsField(param.getRoleIds(), roleMapper));
    }

    @Override
    @ReadDataSource
    public SysAccountOwnGroupResult ownGroups(String id) {
        SysAccount account = getBaseMapper().selectById(id);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        List<String> groupIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_ACCOUNT,
                id,
                IamRelationTypes.ACCOUNT_GROUP,
                account.getAccountType());
        SysAccountOwnGroupResult result = new SysAccountOwnGroupResult();
        result.setId(id);
        result.setGroupIds(groupIds);
        result.setGroups(loadGroupsByIds(groupIds));
        return result;
    }

    @Override
    @Transactional
    public void grantGroups(SysAccountGrantGroupParam param) {
        // 全量替换账号用户组关系
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(param.getId(), "iam:account:page");
        List<String> beforeGroupIds = relationService.listTargetIds(
                IamRelationTypes.SUBJECT_ACCOUNT,
                param.getId(),
                IamRelationTypes.ACCOUNT_GROUP,
                account.getAccountType());
        AuditSnapshots.subject(primaryAccountIdentifier(account.getId()));
        AuditSnapshots.resourceId(param.getId());
        AuditSnapshots.before(IamAuditLabelSupport.groupIdsField(beforeGroupIds, groupMapper));
        relationService.replaceAccountGroups(param.getId(), param.getGroupIds());
        AuditSnapshots.after(IamAuditLabelSupport.groupIdsField(param.getGroupIds(), groupMapper));
    }

    @Override
    @ReadDataSource
    public SysAccountOwnDeptResult ownDepts(String id) {
        if (getBaseMapper().selectById(id) == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(id, "iam:account:page");
        SysAccountOwnDeptResult result = new SysAccountOwnDeptResult();
        result.setId(id);
        result.setGrantInfoList(relationService.listAccountDepts(id));
        return result;
    }

    @Override
    @Transactional
    public void grantDepts(SysAccountGrantDeptParam param) {
        // 全量替换账号部门关系
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(param.getId(), "iam:account:page");
        var beforeGrantInfoList = relationService.listAccountDepts(param.getId());
        AuditSnapshots.subject(primaryAccountIdentifier(account.getId()));
        AuditSnapshots.resourceId(param.getId());
        AuditSnapshots.before(IamAuditLabelSupport.deptGrantField(beforeGrantInfoList, deptMapper));
        relationService.replaceAccountDepts(param.getId(), param.getGrantInfoList());
        AuditSnapshots.after(IamAuditLabelSupport.deptGrantField(param.getGrantInfoList(), deptMapper));
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownResources(String id) {
        SysAccount account = getBaseMapper().selectById(id);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(resourceService.listGrantModules(account.getAccountType()));
        result.setGrantInfoList(relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT, id, account.getAccountType()));
        return result;
    }

    @Override
    @Transactional
    public void grantResources(SysAccountGrantResourceParam param) {
        // 全量替换账号管理端资源授予
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        AuditSnapshots.subject(primaryAccountIdentifier(account.getId()));
        AuditSnapshots.resourceId(param.getId());
        var beforeGrants = relationService.listSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT, param.getId(), account.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantResourceField("授权资源", beforeGrants, resourceMapper));
        relationService.replaceSubjectResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT,
                param.getId(),
                param.getGrantInfoList(),
                account.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantResourceField(
                "授权资源", param.getGrantInfoList(), resourceMapper));
    }

    @Override
    @ReadDataSource
    public SysResourceOwnResult ownClientResources(String id) {
        SysAccount account = getBaseMapper().selectById(id);
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        SysResourceOwnResult result = new SysResourceOwnResult();
        result.setId(id);
        result.setModules(clientResourceService.listGrantModules(account.getAccountType()));
        result.setGrantInfoList(relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT, id, account.getAccountType()));
        return result;
    }

    @Override
    @Transactional
    public void grantClientResources(SysAccountGrantResourceParam param) {
        // 全量替换账号客户端资源授予
        SysAccount account = getBaseMapper().selectById(param.getId());
        if (account == null) {
            throw new BizException(404, "Account not found");
        }
        dataScopeResolver.assertAccountAccessible(account.getId(), "iam:account:page");
        AuditSnapshots.subject(primaryAccountIdentifier(account.getId()));
        AuditSnapshots.resourceId(param.getId());
        var beforeGrants = relationService.listSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT, param.getId(), account.getAccountType());
        AuditSnapshots.before(IamAuditLabelSupport.grantClientResourceField("授权资源", beforeGrants, clientResourceMapper));
        relationService.replaceSubjectClientResourceGrants(
                IamRelationTypes.SUBJECT_ACCOUNT,
                param.getId(),
                param.getGrantInfoList(),
                account.getAccountType());
        AuditSnapshots.after(IamAuditLabelSupport.grantClientResourceField(
                "授权资源", param.getGrantInfoList(), clientResourceMapper));
    }

    @Override
    @ReadDataSource
    public List<SysAccountResult> listResultsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<SysAccount> accounts = getBaseMapper().selectByIds(ids);
        Map<String, SysAccountResult> map = toResultMap(accounts);
        List<SysAccountResult> results = new ArrayList<>();
        for (String id : ids) {
            SysAccountResult item = map.get(id);
            if (item != null) {
                results.add(item);
            }
        }
        return results;
    }

    private List<SysRole> loadRolesByIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
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

    private List<SysGroup> loadGroupsByIds(List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return List.of();
        }
        Map<String, SysGroup> groups = new HashMap<>();
        for (List<String> batch : BatchPartition.partition(groupIds)) {
            for (SysGroup group : groupMapper.selectByIds(batch)) {
                groups.put(group.getId(), group);
            }
        }
        List<SysGroup> result = new ArrayList<>();
        for (String groupId : groupIds) {
            SysGroup group = groups.get(groupId);
            if (group != null) {
                result.add(group);
            }
        }
        return result;
    }

    private Map<String, SysAccountListResult> toListResultMap(List<SysAccount> accounts) {
        Map<String, SysAccountListResult> map = new HashMap<>();
        if (accounts == null || accounts.isEmpty()) {
            return map;
        }
        List<String> ids = accounts.stream().map(SysAccount::getId).toList();
        Map<String, String> accountIdentifiers = new HashMap<>();
        Map<String, ProfileUserAdminInfo> adminProfiles = new HashMap<>();
        Map<String, ProfileUserPortalInfo> portalProfiles = new HashMap<>();

        Map<String, List<String>> idsByType = accounts.stream()
                .filter(a -> StringUtils.hasText(a.getId()) && StringUtils.hasText(a.getAccountType()))
                .collect(Collectors.groupingBy(
                        a -> a.getAccountType().trim().toUpperCase(Locale.ROOT),
                        Collectors.mapping(SysAccount::getId, Collectors.toList())));

        for (List<String> batch : BatchPartition.partition(ids)) {
            identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                            .in(SysAccountIdentity::getAccountId, batch)
                            .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                            .orderByDesc(SysAccountIdentity::getIsPrimary)
                            .orderByAsc(SysAccountIdentity::getId))
                    .forEach(identity -> accountIdentifiers.putIfAbsent(identity.getAccountId(), identity.getIdentifier()));
        }
        for (Map.Entry<String, List<String>> entry : idsByType.entrySet()) {
            for (List<String> batch : BatchPartition.partition(entry.getValue())) {
                if (AccountType.ADMIN.name().equals(entry.getKey())) {
                    adminProfiles.putAll(adminUserProfileApi.getProfiles(batch));
                } else if (AccountType.PORTAL.name().equals(entry.getKey())) {
                    portalProfiles.putAll(portalUserProfileApi.getProfiles(batch));
                }
            }
        }

        for (SysAccount account : accounts) {
            SysAccountListResult result = new SysAccountListResult();
            result.setId(account.getId());
            result.setAccount(accountIdentifiers.get(account.getId()));
            result.setAccountType(account.getAccountType());
            result.setAccountStatus(account.getAccountStatus());
            result.setLatestLoginTime(account.getLatestLoginTime());
            result.setUpdatedAt(account.getUpdatedAt());

            String type = StringUtils.hasText(account.getAccountType())
                    ? account.getAccountType().trim().toUpperCase(Locale.ROOT)
                    : "";
            if (AccountType.ADMIN.name().equals(type)) {
                ProfileUserAdminInfo profile = adminProfiles.get(account.getId());
                if (profile != null) {
                    result.setNickname(profile.getNickname());
                    result.setAvatar(fileApi.resolveUrl(profile.getAvatar()));
                    result.setPhone(profile.getPhone());
                    result.setEmail(profile.getEmail());
                    result.setRemark(profile.getRemark());
                }
            } else if (AccountType.PORTAL.name().equals(type)) {
                ProfileUserPortalInfo profile = portalProfiles.get(account.getId());
                if (profile != null) {
                    result.setNickname(profile.getNickname());
                    result.setAvatar(fileApi.resolveUrl(profile.getAvatar()));
                    result.setPhone(profile.getPhone());
                    result.setEmail(profile.getEmail());
                }
            }
            map.put(account.getId(), result);
        }
        return map;
    }

    private Map<String, SysAccountResult> toResultMap(List<SysAccount> accounts) {
        Map<String, SysAccountResult> map = new HashMap<>();
        if (accounts == null || accounts.isEmpty()) {
            return map;
        }
        List<String> ids = accounts.stream().map(SysAccount::getId).toList();
        Map<String, List<SysAccountIdentity>> identityMap = new HashMap<>();
        Map<String, ProfileUserAdminInfo> adminProfiles = new HashMap<>();
        Map<String, ProfileUserPortalInfo> portalProfiles = new HashMap<>();

        // 按账号类型分组，便于分批拉档案
        Map<String, List<String>> idsByType = accounts.stream()
                .filter(a -> StringUtils.hasText(a.getId()) && StringUtils.hasText(a.getAccountType()))
                .collect(Collectors.groupingBy(
                        a -> a.getAccountType().trim().toUpperCase(Locale.ROOT),
                        Collectors.mapping(SysAccount::getId, Collectors.toList())));

        // 分批加载身份
        for (List<String> batch : BatchPartition.partition(ids)) {
            identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                            .in(SysAccountIdentity::getAccountId, batch)
                            .orderByDesc(SysAccountIdentity::getIsPrimary)
                            .orderByAsc(SysAccountIdentity::getId))
                    .forEach(identity -> identityMap
                            .computeIfAbsent(identity.getAccountId(), key -> new ArrayList<>())
                            .add(identity));
        }
        // 按 ADMIN/PORTAL 分批加载档案
        for (Map.Entry<String, List<String>> entry : idsByType.entrySet()) {
            for (List<String> batch : BatchPartition.partition(entry.getValue())) {
                if (AccountType.ADMIN.name().equals(entry.getKey())) {
                    adminProfiles.putAll(adminUserProfileApi.getProfiles(batch));
                } else if (AccountType.PORTAL.name().equals(entry.getKey())) {
                    portalProfiles.putAll(portalUserProfileApi.getProfiles(batch));
                }
            }
        }

        Map<String, List<SysAccountOauthBinding>> oauthByAccount = accountOauthService.listByAccountIds(ids).stream()
                .collect(Collectors.groupingBy(SysAccountOauthBinding::getAccountId));

        // 合并身份字段与档案到结果
        for (SysAccount account : accounts) {
            SysAccountResult result = accountConvert.toResult(account);
            List<SysAccountIdentity> identities = identityMap.getOrDefault(account.getId(), List.of());
            SysAccountIdentity primary = identities.stream()
                    .filter(item -> "ACCOUNT".equals(item.getIdentityType()) && Boolean.TRUE.equals(item.getIsPrimary()))
                    .findFirst()
                    .orElseGet(() -> identities.stream()
                            .filter(item -> "ACCOUNT".equals(item.getIdentityType()))
                            .findFirst()
                            .orElse(null));
            SysAccountIdentity emailIdentity = identities.stream()
                    .filter(item -> "EMAIL".equals(item.getIdentityType()))
                    .findFirst()
                    .orElse(null);
            SysAccountIdentity phoneIdentity = identities.stream()
                    .filter(item -> "PHONE".equals(item.getIdentityType()))
                    .findFirst()
                    .orElse(null);
            result.setAccount(primary == null ? null : primary.getIdentifier());
            result.setEmailIdentity(emailIdentity == null ? null : emailIdentity.getIdentifier());
            result.setPhoneIdentity(phoneIdentity == null ? null : phoneIdentity.getIdentifier());
            result.setEmailIdentityVerified(emailIdentity != null && Boolean.TRUE.equals(emailIdentity.getVerified()));
            result.setPhoneIdentityVerified(phoneIdentity != null && Boolean.TRUE.equals(phoneIdentity.getVerified()));
            result.setEmailIdentityBindStatus(emailIdentity == null ? null : emailIdentity.getBindStatus());
            result.setPhoneIdentityBindStatus(phoneIdentity == null ? null : phoneIdentity.getBindStatus());
            result.setEmailLoginEnabled(identityLoginEnabled(emailIdentity));
            result.setPhoneLoginEnabled(identityLoginEnabled(phoneIdentity));
            result.setIdentities(accountConvert.toIdentityResultList(identities));
            result.setOauthBindings(toOauthBindingResults(
                    oauthByAccount.getOrDefault(account.getId(), List.of())));

            String type = StringUtils.hasText(account.getAccountType())
                    ? account.getAccountType().trim().toUpperCase(Locale.ROOT)
                    : "";
            if (AccountType.ADMIN.name().equals(type)) {
                ProfileUserAdminInfo profile = adminProfiles.get(account.getId());
                if (profile != null) {
                    result.setNickname(profile.getNickname());
                    result.setAvatar(fileApi.resolveUrl(profile.getAvatar()));
                    result.setSignature(profile.getSignature());
                    result.setPhone(profile.getPhone());
                    result.setEmail(profile.getEmail());
                    result.setRemark(profile.getRemark());
                }
            } else if (AccountType.PORTAL.name().equals(type)) {
                ProfileUserPortalInfo profile = portalProfiles.get(account.getId());
                if (profile != null) {
                    result.setNickname(profile.getNickname());
                    result.setAvatar(fileApi.resolveUrl(profile.getAvatar()));
                    result.setSignature(profile.getSignature());
                    result.setPhone(profile.getPhone());
                    result.setEmail(profile.getEmail());
                }
            }
            map.put(account.getId(), result);
        }
        return map;
    }

    private static boolean identityLoginEnabled(SysAccountIdentity identity) {
        return identity != null
                && StringUtils.hasText(identity.getIdentifier())
                && Boolean.TRUE.equals(identity.getVerified())
                && "BOUND".equalsIgnoreCase(identity.getBindStatus());
    }

    private void upsertProfile(
            SysAccount account,
            String nickname,
            String avatar,
            String signature,
            String phone,
            String email,
            String remark) {
        String type = StringUtils.hasText(account.getAccountType())
                ? account.getAccountType().trim().toUpperCase(Locale.ROOT)
                : "";
        if (AccountType.ADMIN.name().equals(type)) {
            ProfileUserAdminInfo info = new ProfileUserAdminInfo();
            info.setAccountId(account.getId());
            info.setNickname(nickname);
            info.setAvatar(fileApi.normalizeObjectName(avatar));
            info.setSignature(signature);
            info.setPhone(phone);
            info.setEmail(email);
            info.setRemark(remark);
            adminUserProfileApi.upsertProfile(info);
            return;
        }
        if (AccountType.PORTAL.name().equals(type)) {
            ProfileUserPortalInfo info = new ProfileUserPortalInfo();
            info.setAccountId(account.getId());
            info.setNickname(nickname);
            info.setAvatar(fileApi.normalizeObjectName(avatar));
            info.setSignature(signature);
            info.setPhone(phone);
            info.setEmail(email);
            portalUserProfileApi.upsertProfile(info);
        }
    }

    private void insertIdentity(String accountId, String type, String identifier, boolean verified, boolean primary) {
        insertIdentity(accountId, type, identifier, verified, primary, "BOUND");
    }

    private void insertIdentity(
            String accountId,
            String type,
            String identifier,
            boolean verified,
            boolean primary,
            String bindStatus) {
        SysAccountIdentity identity = new SysAccountIdentity();
        identity.setAccountId(accountId);
        identity.setIdentityType(type);
        identity.setIdentifier(identifier);
        identity.setVerified(verified);
        identity.setIsPrimary(primary);
        identity.setBindStatus(StringUtils.hasText(bindStatus) ? bindStatus : "BOUND");
        identityMapper.insert(identity);
    }

    private void replacePrimaryIdentity(String accountId, String type, String identifier) {
        SysAccountIdentity existing = identityMapper.selectOne(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, type)
                .last("limit 1"));
        if (existing == null) {
            insertIdentity(accountId, type, identifier, true, true);
            return;
        }
        existing.setIdentifier(identifier);
        identityMapper.updateById(existing);
    }

    private void replaceAccountLoginIdentity(String accountId, String account) {
        if (!StringUtils.hasText(account)) {
            throw new BizException("Account identity identifier required");
        }
        Long conflict = identityMapper.selectCount(Wrappers.<SysAccountIdentity>lambdaQuery()
                .ne(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                .eq(SysAccountIdentity::getIdentifier, account));
        if (conflict != null && conflict > 0) {
            throw new BizException("Account identity already exists");
        }
        identityMapper.delete(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, "ACCOUNT"));
        insertIdentity(accountId, "ACCOUNT", account, true, true);
    }

    private void replaceSecondaryLoginIdentities(
            String accountId,
            Boolean emailLoginEnabled,
            String email,
            Boolean phoneLoginEnabled,
            String phone) {
        identityMapper.delete(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .in(SysAccountIdentity::getIdentityType, "EMAIL", "PHONE"));
        if (Boolean.TRUE.equals(emailLoginEnabled) && StringUtils.hasText(email)) {
            insertIdentity(accountId, "EMAIL", email.trim(), true, false);
        }
        if (Boolean.TRUE.equals(phoneLoginEnabled) && StringUtils.hasText(phone)) {
            insertIdentity(accountId, "PHONE", phone.trim(), true, false);
        }
    }

    private void replaceAccountIdentities(
            String accountId,
            String account,
            Boolean emailLoginEnabled,
            String emailIdentity,
            String email,
            Boolean emailIdentityVerified,
            String emailIdentityBindStatus,
            Boolean phoneLoginEnabled,
            String phoneIdentity,
            String phone,
            Boolean phoneIdentityVerified,
            String phoneIdentityBindStatus) {
        // 组装目标身份规格：主账号 + 可选邮箱/手机登录身份
        List<IdentitySpec> specs = new ArrayList<>();
        specs.add(new IdentitySpec("ACCOUNT", account, true, true, "BOUND"));
        if (Boolean.TRUE.equals(emailLoginEnabled)) {
            String emailValue = firstNonBlank(emailIdentity, email);
            if (StringUtils.hasText(emailValue)) {
                specs.add(new IdentitySpec(
                        "EMAIL",
                        emailValue.trim(),
                        false,
                        Boolean.TRUE.equals(emailIdentityVerified),
                        StringUtils.hasText(emailIdentityBindStatus) ? emailIdentityBindStatus : "BOUND"));
            }
        }
        if (Boolean.TRUE.equals(phoneLoginEnabled)) {
            String phoneValue = firstNonBlank(phoneIdentity, phone);
            if (StringUtils.hasText(phoneValue)) {
                specs.add(new IdentitySpec(
                        "PHONE",
                        phoneValue.trim(),
                        false,
                        Boolean.TRUE.equals(phoneIdentityVerified),
                        StringUtils.hasText(phoneIdentityBindStatus) ? phoneIdentityBindStatus : "BOUND"));
            }
        }
        // 校验标识非空且无跨账号冲突
        for (IdentitySpec spec : specs) {
            if (!StringUtils.hasText(spec.identifier())) {
                throw new BizException("Account identity identifier required");
            }
        }
        Long conflict = identityMapper.selectCount(Wrappers.<SysAccountIdentity>lambdaQuery()
                .ne(SysAccountIdentity::getAccountId, accountId)
                .and(w -> {
                    for (IdentitySpec spec : specs) {
                        w.or(sub -> sub.eq(SysAccountIdentity::getIdentityType, spec.type())
                                .eq(SysAccountIdentity::getIdentifier, spec.identifier()));
                    }
                }));
        if (conflict != null && conflict > 0) {
            throw new BizException("Account identity already exists");
        }
        // 先删后插：全量替换本账号身份
        identityMapper.delete(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId));
        List<SysAccountIdentity> identities = new ArrayList<>(specs.size());
        for (IdentitySpec spec : specs) {
            SysAccountIdentity identity = new SysAccountIdentity();
            identity.setAccountId(accountId);
            identity.setIdentityType(spec.type());
            identity.setIdentifier(spec.identifier());
            identity.setVerified(spec.verified());
            identity.setIsPrimary(spec.primary());
            identity.setBindStatus(StringUtils.hasText(spec.bindStatus()) ? spec.bindStatus() : "BOUND");
            identities.add(identity);
        }
        if (!identities.isEmpty()) {
            Db.saveBatch(identities);
        }
    }

    private String[] collectCancelNotifyContacts(String accountId) {
        String email = null;
        String phone = null;
        // 优先从身份表取邮箱/手机
        List<SysAccountIdentity> identities = identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .orderByDesc(SysAccountIdentity::getIsPrimary)
                .orderByAsc(SysAccountIdentity::getId));
        for (SysAccountIdentity identity : identities) {
            String identifier = identity.getIdentifier() == null ? "" : identity.getIdentifier().trim();
            if (!StringUtils.hasText(identifier)) {
                continue;
            }
            if ("EMAIL".equals(identity.getIdentityType()) && email == null) {
                email = identifier;
            } else if ("PHONE".equals(identity.getIdentityType()) && phone == null) {
                phone = identifier;
            }
        }
        // 缺项再从管理端/门户档案补齐
        if (email == null || phone == null) {
            SysAccount account = this.getById(accountId);
            if (account != null && StringUtils.hasText(account.getAccountType())) {
                String type = account.getAccountType().trim().toUpperCase(Locale.ROOT);
                if (AccountType.ADMIN.name().equals(type)) {
                    ProfileUserAdminInfo profile = adminUserProfileApi.getProfile(accountId);
                    if (profile != null) {
                        if (email == null) {
                            email = profile.getEmail();
                        }
                        if (phone == null) {
                            phone = profile.getPhone();
                        }
                    }
                } else if (AccountType.PORTAL.name().equals(type)) {
                    ProfileUserPortalInfo profile = portalUserProfileApi.getProfile(accountId);
                    if (profile != null) {
                        if (email == null) {
                            email = profile.getEmail();
                        }
                        if (phone == null) {
                            phone = profile.getPhone();
                        }
                    }
                }
            }
        }
        return new String[]{email, phone};
    }

    private void cleanupAccountSideData(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        List<String> uniqueIds = accountIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (uniqueIds.isEmpty()) {
            return;
        }
        // 删关系、身份、三方绑定、密码历史与双侧档案
        relationService.deleteBySubjectIds(IamRelationTypes.SUBJECT_ACCOUNT, uniqueIds);
        identityMapper.delete(Wrappers.<SysAccountIdentity>lambdaQuery()
                .in(SysAccountIdentity::getAccountId, uniqueIds));
        accountOauthService.deleteByAccountIds(uniqueIds);
        passwordHelper.deleteHistory(uniqueIds);
        adminUserProfileApi.deleteProfiles(uniqueIds);
        portalUserProfileApi.deleteProfiles(uniqueIds);
    }

    private List<AccountOauthBindingResult> toOauthBindingResults(List<SysAccountOauthBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        List<AccountOauthBindingResult> list = new ArrayList<>(bindings.size());
        for (SysAccountOauthBinding binding : bindings) {
            AccountOauthBindingResult row = new AccountOauthBindingResult();
            row.setId(binding.getId());
            row.setProvider(binding.getProvider());
            row.setOpenId(binding.getOpenId());
            row.setUnionId(binding.getUnionId());
            row.setNickname(binding.getNickname());
            row.setAvatar(binding.getAvatar());
            row.setBoundAt(binding.getBoundAt());
            list.add(row);
        }
        return list;
    }

    /**
     * 管理端/门户档案并集过滤；无条件时返回 null。
     */
    private Set<String> findAccountIdsByProfileFilters(String name, String phone, String email) {
        Set<String> adminIds = adminUserProfileApi.findAccountIdsByProfileFilters(name, phone, email);
        Set<String> portalIds = portalUserProfileApi.findAccountIdsByProfileFilters(name, phone, email);
        if (adminIds == null && portalIds == null) {
            return null;
        }
        Set<String> merged = new HashSet<>();
        if (adminIds != null) {
            merged.addAll(adminIds);
        }
        if (portalIds != null) {
            merged.addAll(portalIds);
        }
        return merged;
    }

    private void clearAccountSessions(SysAccount account) {
        if (account == null || !StringUtils.hasText(account.getId()) || !StringUtils.hasText(account.getAccountType())) {
            return;
        }
        try {
            AccountType type = AccountType.valueOf(account.getAccountType().trim().toUpperCase(Locale.ROOT));
            LoginHelper.stpLogic(type).logout(account.getId());
        } catch (IllegalArgumentException ignored) {
            // 未知账号类型：跳过会话清理
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }

    private record IdentitySpec(
            String type,
            String identifier,
            boolean primary,
            boolean verified,
            String bindStatus) {
    }
}
