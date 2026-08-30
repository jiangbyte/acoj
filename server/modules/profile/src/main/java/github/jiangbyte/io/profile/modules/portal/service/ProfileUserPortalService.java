package github.jiangbyte.io.profile.modules.portal.service;

import github.jiangbyte.io.profile.portal.ProfileUserPortalInfo;
import github.jiangbyte.io.profile.modules.portal.param.ProfileUpdateParam;
import github.jiangbyte.io.profile.modules.portal.result.AvatarUpdateResult;
import github.jiangbyte.io.profile.modules.portal.result.MeResult;
import github.jiangbyte.io.profile.modules.portal.result.PublicProfileResult;
import github.jiangbyte.io.profile.modules.portal.result.UserProfileResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 门户端用户资料领域服务：当前用户/资料维护、头像上传、公开资料，以及跨模块资料读写与筛选。
 *
 * Author: Charlie
 */
public interface ProfileUserPortalService {

    /** 组装当前登录用户的 me 响应（含资料与组织名称）。 */
    MeResult currentMe();

    /** 读取并补全当前用户资料（头像 URL、登录身份开关）。 */
    UserProfileResult currentProfile();

    /** 更新当前用户可编辑资料字段。 */
    void updateProfile(ProfileUpdateParam request);

    /** 校验并上传头像，更新资料并尽力清理旧文件。 */
    AvatarUpdateResult uploadAvatar(MultipartFile file);

    /** 按账号 ID 查询门户公开资料；不存在时抛出业务异常。 */
    PublicProfileResult publicProfile(String accountId);

    /**
     * 按账号 ID 查询资料。
     *
     * @return 不存在时为 null
     */
    ProfileUserPortalInfo getProfile(String accountId);

    /** 批量查询资料，返回 accountId → 资料映射。 */
    Map<String, ProfileUserPortalInfo> getProfiles(Collection<String> accountIds);

    /** 批量解析展示名（已认证姓名优先，否则 nickname）。 */
    Map<String, String> getDisplayNames(Collection<String> accountIds);

    /** 按账号 upsert 资料（不存在则插入）。 */
    void upsertProfile(ProfileUserPortalInfo info);

    /** 按账号更新或创建手机号字段。 */
    void updatePhoneByAccount(String accountId, String phone);

    /** 按账号更新或创建邮箱字段。 */
    void updateEmailByAccount(String accountId, String email);

    /** 创建基础资料（昵称/邮箱）。 */
    void createProfile(String accountId, String nickname, String email);

    /** 按账号集合批量删除资料。 */
    void deleteProfiles(Collection<String> accountIds);

    /**
     * 按姓名/手机/邮箱过滤账号 ID；多条件取交集。
     * 无任何过滤条件时返回 {@code null}；有条件但无匹配时返回空集合。
     */
    Set<String> findAccountIdsByProfileFilters(String name, String phone, String email);
}
