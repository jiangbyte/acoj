package github.jiangbyte.io.sys.weakpassword;

/**
 * 弱密码库校验门面：拒绝命中系统弱密码列表的明文密码。
 * 通常由密码策略链路在强度规则之外调用。
 *
 * Author: Charlie
 */
public interface WeakPasswordApi {

    /**
     * 断言密码不在弱密码拒绝列表中。
     *
     * @throws github.jiangbyte.io.common.core.exception.BizException 密码在拒绝列表中时抛出
     */
    void assertNotWeak(String rawPassword);
}
