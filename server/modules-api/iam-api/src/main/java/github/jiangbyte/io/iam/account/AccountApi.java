package github.jiangbyte.io.iam.account;

import java.time.OffsetDateTime;

/**
 * 跨模块账号门面：查询/创建账号、登录元数据、密码与身份绑定、角色与部门分配。
 * HTTP 类型留在 {@code module/iam}；实现为 {@code AccountApiProvider}。
 *
 * Author: Charlie
 */
public interface AccountApi {

    /** 按登录标识与身份类型查找账号；不存在时返回 null。 */
    AccountInfo findByIdentifier(String identifier, String identityType);

    /** 按账号 id 获取快照；不存在时返回 null。 */
    AccountInfo getById(String accountId);

    /**
     * 更新最近登录 IP / 时间 / 设备。
     *
     * @param accountId 账号 id
     * @param ip        登录 IP
     * @param time      登录时间
     * @param device    设备描述
     */
    void updateLoginMeta(String accountId, String ip, OffsetDateTime time, String device);

    /** 加载账号授权快照（角色、部门、权限等）。 */
    AccountAuthorizationInfo getAuthorization(String accountId);

    /** 校验明文密码是否匹配给定哈希。 */
    boolean matchesPassword(String rawPassword, String passwordHash);

    /** 对明文密码做哈希编码。 */
    String encodePassword(String rawPassword);

    /** 判断密码是否已超过指定过期天数。 */
    boolean isPasswordExpired(String accountId, int expireDays);

    /** 密码年龄（整天数）；未知时为 null。 */
    Integer getPasswordAgeDays(String accountId);

    /** 查询指定类型的绑定标识；无则 null。 */
    String findIdentifier(String accountId, String identityType);

    /**
     * 记录密码历史（明文入参由实现侧再哈希落库）。
     *
     * @param accountId  账号 id
     * @param rawPassword 明文密码
     * @param operatorId 操作者 id
     * @param reason     变更原因
     */
    void recordPasswordHistory(String accountId, String rawPassword, String operatorId, String reason);

    /**
     * 创建门户 SysAccount、ACCOUNT 身份，以及可选的 EMAIL 身份。
     * 调用方负责创建档案与密码历史。
     */
    AccountInfo createPortalAccount(String account, String email, String encodedPassword);

    /** 直接更新账号密码哈希。 */
    void updatePasswordHash(String accountId, String passwordHash);

    /** 注销账号并记录操作者与原因。 */
    void cancelAccount(String accountId, String cancelledBy, String cancelReason);

    /**
     * 新增或更新账号身份绑定。
     *
     * @param accountId  账号 id
     * @param type       身份类型（如 ACCOUNT / EMAIL / PHONE）
     * @param identifier 标识值
     * @param enabled    是否启用该身份登录
     */
    void upsertIdentity(String accountId, String type, String identifier, boolean enabled);

    /** 分配单个角色（用该角色替换现有 ACCOUNT_ROLE 集合）。 */
    void assignRole(String accountId, String roleId);

    /** 分配单个主部门（替换现有 ACCOUNT_DEPT 集合）。 */
    void assignPrimaryDept(String accountId, String deptId);
}
