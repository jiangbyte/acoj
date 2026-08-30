package github.jiangbyte.io.profile.portal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 门户端用户档案跨模块 API：查询/批量查询、展示名、增删改手机邮箱及条件反查账户。
 * 实现见 {@code module/user} 的 {@code ProfileUserPortalApiProvider}。
 *
 * Author: Charlie
 */
public interface ProfileUserPortalApi {

    /** 按账号 id 获取档案；不存在时返回 null。 */
    ProfileUserPortalInfo getProfile(String accountId);

    /** 批量获取档案，key 为 accountId。 */
    Map<String, ProfileUserPortalInfo> getProfiles(Collection<String> accountIds);

    /** 展示名映射：已认证姓名优先，否则 nickname。 */
    Map<String, String> getDisplayNames(Collection<String> accountIds);

    /** 按 accountId 插入或更新档案。 */
    void upsertProfile(ProfileUserPortalInfo info);

    /** 更新档案手机号。 */
    void updatePhone(String accountId, String phone);

    /** 更新档案邮箱。 */
    void updateEmail(String accountId, String email);

    /** 创建门户端档案基础字段（昵称/邮箱）。 */
    void createProfile(String accountId, String nickname, String email);

    /** 按账号 id 集合删除档案。 */
    void deleteProfiles(Collection<String> accountIds);

    /**
     * 按门户档案字段模糊匹配账户 ID（多条件取交集）。
     * 无任何过滤条件时返回 {@code null}；有条件但无匹配时返回空集合。
     */
    Set<String> findAccountIdsByProfileFilters(String name, String phone, String email);
}
