package github.jiangbyte.io.iam.password;

/**
 * 密码强度 / 复用 / 弱密码库校验门面，由 {@code PASSWORD_*} 等系统配置驱动。
 * 改密、重置密码等流程在落库前调用。
 *
 * Author: Charlie
 */
public interface PasswordPolicyApi {

    /**
     * 校验新密码是否满足策略；{@code accountId} 为 null 时跳过历史密码检查。
     *
     * @param rawPassword 明文新密码
     * @param accountId   账号 id；null 表示不做历史复用检查
     * @param accountName 账号名（用于禁止包含用户名等规则）
     * @param email       邮箱（可选，用于弱关联规则）
     * @param phone       手机号（可选，用于弱关联规则）
     */
    void assertValid(
            String rawPassword,
            String accountId,
            String accountName,
            String email,
            String phone);
}
