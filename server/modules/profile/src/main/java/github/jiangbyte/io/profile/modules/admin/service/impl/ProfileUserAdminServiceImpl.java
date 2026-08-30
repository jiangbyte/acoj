package github.jiangbyte.io.profile.modules.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.iam.account.AccountIdentityApi;
import github.jiangbyte.io.iam.org.OrgNameApi;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.sys.file.FileInfo;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.admin.ProfileUserAdminInfo;
import github.jiangbyte.io.profile.modules.admin.convert.ProfileUserAdminConvert;
import github.jiangbyte.io.profile.modules.admin.entity.ProfileUserAdmin;
import github.jiangbyte.io.profile.modules.admin.mapper.ProfileUserAdminMapper;
import github.jiangbyte.io.profile.modules.admin.param.ProfileUpdateParam;
import github.jiangbyte.io.profile.modules.admin.result.AvatarUpdateResult;
import github.jiangbyte.io.profile.modules.admin.result.DeptIdNameResult;
import github.jiangbyte.io.profile.modules.admin.result.GroupIdNameResult;
import github.jiangbyte.io.profile.modules.admin.result.MeResult;
import github.jiangbyte.io.profile.modules.admin.result.OrgInfoResult;
import github.jiangbyte.io.profile.modules.admin.result.RoleIdNameResult;
import github.jiangbyte.io.profile.modules.admin.result.UserProfileResult;
import github.jiangbyte.io.profile.modules.admin.service.ProfileUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ProfileUserAdminService} 实现：资料持久化、头像文件处理、组织名称翻译与跨模块资料读写。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ProfileUserAdminServiceImpl implements ProfileUserAdminService {

    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    private static final Set<String> AVATAR_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ProfileUserAdminMapper adminProfileMapper;
    private final FileApi fileApi;
    private final AccountIdentityApi accountIdentityApi;
    private final OrgNameApi orgNameApi;
    private final ConfigApi configApi;
    private final ProfileIdentityService profileIdentityService;
    private final ProfileUserAdminConvert adminUserProfileConvert;

    @Override
    @ReadDataSource
    public MeResult currentMe() {
        // 读取会话与资料，组装 me 并填充组织名称
        LoginUser loginUser = LoginHelper.requireUser();
        UserProfileResult profile = currentProfile();
        MeResult response = adminUserProfileConvert.toMe(loginUser, profile);
        fillOrgIdNames(response, loginUser);
        response.setAvatar(profile.getAvatar());
        fillForceBindFlags(response, loginUser);
        response.setIdentity(profileIdentityService.getUserStatusForAccount(loginUser.getAccountId()));
        return response;
    }

    @Override
    @ReadDataSource
    public UserProfileResult currentProfile() {
        // 确保资料存在后补全登录开关与头像 URL
        LoginUser loginUser = LoginHelper.requireUser();
        return withResolvedAvatar(enrichLoginFlags(adminUserProfileConvert.toDto(ensureProfile(loginUser.getAccountId()))));
    }

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateParam request) {
        LoginUser loginUser = LoginHelper.requireUser();
        ProfileUserAdmin profile = ensureProfile(loginUser.getAccountId());
        AuditSnapshots.before(profile);
        // 映射可编辑字段；头像单独规范化对象名
        adminUserProfileConvert.update(request, profile);
        if (StringUtils.hasText(request.getAvatar())) {
            profile.setAvatar(fileApi.normalizeObjectName(request.getAvatar()));
        }
        adminProfileMapper.updateById(profile);
        AuditSnapshots.after(profile);
    }

    @Override
    @Transactional
    public AvatarUpdateResult uploadAvatar(MultipartFile file) {
        LoginUser loginUser = LoginHelper.requireUser();
        // 校验文件存在、大小与 MIME
        if (file == null || file.isEmpty()) {
            throw new BizException("File is required");
        }
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new BizException("Avatar exceeds 2MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!AVATAR_TYPES.contains(contentType)) {
            throw new BizException("Avatar must be jpeg/png/webp");
        }
        ProfileUserAdmin current = ensureProfile(loginUser.getAccountId());
        AuditSnapshots.before(current);
        String previousAvatar = current.getAvatar();
        // 上传新头像并落库
        FileInfo uploaded = fileApi.upload(file, null);
        current.setAvatar(uploaded.getObjectName());
        adminProfileMapper.updateById(current);
        AuditSnapshots.after(current);
        // 尽力删除旧对象
        if (StringUtils.hasText(previousAvatar) && !previousAvatar.equals(uploaded.getObjectName())) {
            try {
                fileApi.deleteByObjectName(previousAvatar);
            } catch (Exception ignored) {
                // 尽力清理
            }
        }
        String resolved = fileApi.resolveUrl(uploaded.getObjectName());
        if (!StringUtils.hasText(resolved)) {
            resolved = uploaded.getUrl();
        }
        AvatarUpdateResult response = new AvatarUpdateResult();
        BeanUtil.copyProperties(uploaded, response);
        response.setFileId(uploaded.getId());
        response.setAvatar(resolved);
        response.setUrl(resolved);
        return response;
    }

    @Override
    @ReadDataSource
    public OrgInfoResult orgInfo() {
        return resolveOrgIdNames(LoginHelper.requireUser());
    }

    @Override
    @ReadDataSource
    public ProfileUserAdminInfo getProfile(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return null;
        }
        return adminUserProfileConvert.toInfo(adminProfileMapper.selectById(accountId));
    }

    @Override
    @ReadDataSource
    public Map<String, ProfileUserAdminInfo> getProfiles(Collection<String> accountIds) {
        Map<String, ProfileUserAdminInfo> map = new HashMap<>();
        if (accountIds == null || accountIds.isEmpty()) {
            return map;
        }
        // 批量查询并按 accountId 建索引
        adminProfileMapper.selectByIds(accountIds)
                .forEach(profile -> map.put(profile.getAccountId(), adminUserProfileConvert.toInfo(profile)));
        return map;
    }

    @Override
    @ReadDataSource
    public Map<String, String> getDisplayNames(Collection<String> accountIds) {
        Map<String, String> map = new HashMap<>();
        if (accountIds == null || accountIds.isEmpty()) {
            return map;
        }
        adminProfileMapper.selectByIds(accountIds).forEach(profile -> {
            if (StringUtils.hasText(profile.getNickname())) {
                map.put(profile.getAccountId(), profile.getNickname().trim());
            }
        });
        return map;
    }

    @Override
    @Transactional
    public void upsertProfile(ProfileUserAdminInfo info) {
        if (info == null || !StringUtils.hasText(info.getAccountId())) {
            return;
        }
        ProfileUserAdmin profile = adminProfileMapper.selectById(info.getAccountId());
        if (profile == null) {
            // 不存在则新建
            profile = new ProfileUserAdmin();
            profile.setAccountId(info.getAccountId());
            adminUserProfileConvert.updateInfo(info, profile);
            adminProfileMapper.insert(profile);
            return;
        }
        adminUserProfileConvert.updateInfo(info, profile);
        adminProfileMapper.updateById(profile);
    }

    @Override
    @Transactional
    public void updatePhoneByAccount(String accountId, String phone) {
        String value = StringUtils.hasText(phone) ? phone.trim() : null;
        ProfileUserAdmin profile = adminProfileMapper.selectById(accountId);
        if (profile == null) {
            profile = new ProfileUserAdmin();
            profile.setAccountId(accountId);
            profile.setPhone(value);
            adminProfileMapper.insert(profile);
            return;
        }
        profile.setPhone(value);
        adminProfileMapper.updateById(profile);
    }

    @Override
    @Transactional
    public void updateEmailByAccount(String accountId, String email) {
        // 邮箱统一小写存储
        String value = StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : null;
        ProfileUserAdmin profile = adminProfileMapper.selectById(accountId);
        if (profile == null) {
            profile = new ProfileUserAdmin();
            profile.setAccountId(accountId);
            profile.setEmail(value);
            adminProfileMapper.insert(profile);
            return;
        }
        profile.setEmail(value);
        adminProfileMapper.updateById(profile);
    }

    @Override
    @Transactional
    public void createProfile(String accountId, String nickname, String email) {
        if (!StringUtils.hasText(accountId)) {
            return;
        }
        ProfileUserAdminInfo info = new ProfileUserAdminInfo();
        info.setAccountId(accountId);
        info.setNickname(nickname);
        info.setEmail(email);
        upsertProfile(info);
    }

    @Override
    @Transactional
    public void deleteProfiles(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        adminProfileMapper.deleteByIds(accountIds);
    }

    @Override
    @ReadDataSource
    public Set<String> findAccountIdsByProfileFilters(String name, String phone, String email) {
        boolean hasName = StringUtils.hasText(name);
        boolean hasPhone = StringUtils.hasText(phone);
        boolean hasEmail = StringUtils.hasText(email);
        // 无条件时返回 null，表示不过滤
        if (!hasName && !hasPhone && !hasEmail) {
            return null;
        }
        // 多条件取交集
        Set<String> matched = null;
        if (hasName) {
            matched = intersectOrInit(matched, accountIdsByName(name.trim()));
        }
        if (hasPhone) {
            matched = intersectOrInit(matched, accountIdsByPhone(phone.trim()));
        }
        if (hasEmail) {
            matched = intersectOrInit(matched, accountIdsByEmail(email.trim()));
        }
        return matched == null ? Set.of() : matched;
    }

    private Set<String> accountIdsByName(String name) {
        return profileIdentityService.findAccountIdsByRealName(name);
    }

    private Set<String> accountIdsByPhone(String phone) {
        return adminProfileMapper.selectList(Wrappers.<ProfileUserAdmin>lambdaQuery()
                        .like(ProfileUserAdmin::getPhone, phone)
                        .select(ProfileUserAdmin::getAccountId))
                .stream()
                .map(ProfileUserAdmin::getAccountId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<String> accountIdsByEmail(String email) {
        return adminProfileMapper.selectList(Wrappers.<ProfileUserAdmin>lambdaQuery()
                        .like(ProfileUserAdmin::getEmail, email)
                        .select(ProfileUserAdmin::getAccountId))
                .stream()
                .map(ProfileUserAdmin::getAccountId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Set<String> intersectOrInit(Set<String> current, Set<String> next) {
        if (current == null) {
            return next;
        }
        return current.stream().filter(next::contains).collect(Collectors.toCollection(HashSet::new));
    }

    private ProfileUserAdmin ensureProfile(String accountId) {
        // 首次访问时懒创建空资料行
        ProfileUserAdmin profile = adminProfileMapper.selectById(accountId);
        if (profile != null) {
            return profile;
        }
        profile = new ProfileUserAdmin();
        profile.setAccountId(accountId);
        adminProfileMapper.insert(profile);
        return profile;
    }

    private UserProfileResult enrichLoginFlags(UserProfileResult dto) {
        dto.setPhoneLoginEnabled(accountIdentityApi.hasIdentity(dto.getAccountId(), "PHONE"));
        dto.setEmailLoginEnabled(accountIdentityApi.hasIdentity(dto.getAccountId(), "EMAIL"));
        return dto;
    }

    private void fillForceBindFlags(MeResult response, LoginUser loginUser) {
        String typeName = loginUser.getAccountType() == null ? "ADMIN" : loginUser.getAccountType().name();
        boolean forceEmail = configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_EMAIL", false)
                && !accountIdentityApi.hasIdentity(loginUser.getAccountId(), "EMAIL");
        boolean forcePhone = configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_PHONE", false)
                && !accountIdentityApi.hasIdentity(loginUser.getAccountId(), "PHONE");
        boolean forceIdentity = configApi.getBoolean("AUTH_FORCE_BIND_" + typeName + "_IDENTITY", false)
                && !profileIdentityService.isVerified(loginUser.getAccountId());
        response.setForceBindEmail(forceEmail);
        response.setForceBindPhone(forcePhone);
        response.setForceBindIdentity(forceIdentity);
    }

    private UserProfileResult withResolvedAvatar(UserProfileResult dto) {
        if (dto != null) {
            dto.setAvatar(fileApi.resolveUrl(dto.getAvatar()));
        }
        return dto;
    }

    private void fillOrgIdNames(MeResult response, LoginUser loginUser) {
        // 解析组织 ID-名称后写入 me
        OrgInfoResult org = resolveOrgIdNames(loginUser);
        response.setRoleIdNames(org.getRoleIdNames());
        response.setDeptIdNames(org.getDeptIdNames());
        response.setGroupIdNames(org.getGroupIdNames());
    }

    private OrgInfoResult resolveOrgIdNames(LoginUser loginUser) {
        // 查 entity 名称后填充 Result（Result 不走 easy-trans）
        OrgInfoResult org = new OrgInfoResult();
        org.setRoleIdNames(toRoleIdNames(loginUser.getRoleIds()));
        org.setDeptIdNames(toDeptIdNames(loginUser.getDeptIds()));
        org.setGroupIdNames(toGroupIdNames(loginUser.getGroupIds()));
        return org;
    }

    private List<RoleIdNameResult> toRoleIdNames(Collection<String> ids) {
        List<RoleIdNameResult> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        Map<String, String> names = orgNameApi.roleNames(ids);
        for (String id : ids) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            RoleIdNameResult item = new RoleIdNameResult();
            item.setId(id);
            item.setName(names.get(id));
            result.add(item);
        }
        return result;
    }

    private List<DeptIdNameResult> toDeptIdNames(Collection<String> ids) {
        List<DeptIdNameResult> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        Map<String, String> names = orgNameApi.deptNames(ids);
        for (String id : ids) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            DeptIdNameResult item = new DeptIdNameResult();
            item.setId(id);
            item.setName(names.get(id));
            result.add(item);
        }
        return result;
    }

    private List<GroupIdNameResult> toGroupIdNames(Collection<String> ids) {
        List<GroupIdNameResult> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        Map<String, String> names = orgNameApi.groupNames(ids);
        for (String id : ids) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            GroupIdNameResult item = new GroupIdNameResult();
            item.setId(id);
            item.setName(names.get(id));
            result.add(item);
        }
        return result;
    }
}
