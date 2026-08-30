package github.jiangbyte.io.profile.modules.admin.provider;

import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.admin.ProfileUserAdminInfo;
import github.jiangbyte.io.profile.modules.admin.service.ProfileUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 跨模块 {@link ProfileUserAdminApi} 适配器，将 IAM/认证等模块的资料读写请求委托给 {@link ProfileUserAdminService}。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ProfileUserAdminApiProvider implements ProfileUserAdminApi {

    private final ProfileUserAdminService adminUserProfileService;

    @Override
    public ProfileUserAdminInfo getProfile(String accountId) {
        // 直接委托领域服务查询
        return adminUserProfileService.getProfile(accountId);
    }

    @Override
    public Map<String, ProfileUserAdminInfo> getProfiles(Collection<String> accountIds) {
        return adminUserProfileService.getProfiles(accountIds);
    }

    @Override
    public Map<String, String> getDisplayNames(Collection<String> accountIds) {
        return adminUserProfileService.getDisplayNames(accountIds);
    }

    @Override
    public void upsertProfile(ProfileUserAdminInfo info) {
        adminUserProfileService.upsertProfile(info);
    }

    @Override
    public void updatePhone(String accountId, String phone) {
        // API 方法名与领域服务命名对齐后转发
        adminUserProfileService.updatePhoneByAccount(accountId, phone);
    }

    @Override
    public void updateEmail(String accountId, String email) {
        adminUserProfileService.updateEmailByAccount(accountId, email);
    }

    @Override
    public void createProfile(String accountId, String nickname, String email) {
        adminUserProfileService.createProfile(accountId, nickname, email);
    }

    @Override
    public void deleteProfiles(Collection<String> accountIds) {
        adminUserProfileService.deleteProfiles(accountIds);
    }

    @Override
    public Set<String> findAccountIdsByProfileFilters(String name, String phone, String email) {
        return adminUserProfileService.findAccountIdsByProfileFilters(name, phone, email);
    }
}
