package github.jiangbyte.io.auth.modules.login.support;

import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.sys.config.ConfigApi;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的登录失败/锁定保护：按账号与 IP 统计失败次数，超限后临时锁定。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class LoginProtectionService {

    private final RedissonClient redissonClient;
    private final ConfigApi configApi;

    /** 登录前检查账号或 IP 是否处于锁定状态。 */
    public void ensureAllowed(AccountType accountType, String account, String clientIp) {
        String type = accountType.name();
        String normalized = normalizeAccount(account);
        if (isLocked(lockAccountKey(type, normalized))) {
            throw new BizException(401, "账号已临时锁定");
        }
        if (StringUtils.hasText(clientIp) && isLocked(lockIpKey(type, clientIp))) {
            throw new BizException(401, "该 IP 登录失败次数过多");
        }
    }

    /** 记录一次登录失败；达到阈值时写入锁定键。 */
    public void recordFailure(AccountType accountType, String account, String clientIp) {
        String type = accountType.name();
        int window = typedInt(type, "FAILURE_WINDOW_SECONDS",
                configApi.getInt("AUTH_LOGIN_FAILURE_WINDOW_SECONDS", 900));
        int maxFailures = typedInt(type, "MAX_FAILURES",
                configApi.getInt("AUTH_LOGIN_ACCOUNT_MAX_FAILURES", 5));
        int lockSeconds = typedInt(type, "LOCK_SECONDS",
                configApi.getInt("AUTH_LOGIN_LOCK_SECONDS", 900));
        int ipMax = configApi.getInt("AUTH_LOGIN_IP_MAX_FAILURES", 30);
        int sharedWindow = configApi.getInt("AUTH_LOGIN_FAILURE_WINDOW_SECONDS", 900);
        int sharedLock = configApi.getInt("AUTH_LOGIN_LOCK_SECONDS", 900);

        String normalized = normalizeAccount(account);
        // 账号维度失败计数
        increaseFailure(
                failureAccountKey(type, normalized),
                lockAccountKey(type, normalized),
                maxFailures,
                window,
                lockSeconds);
        if (StringUtils.hasText(clientIp)) {
            // IP 维度失败计数（使用全局阈值）
            increaseFailure(
                    failureIpKey(type, clientIp),
                    lockIpKey(type, clientIp),
                    ipMax,
                    sharedWindow,
                    sharedLock);
        }
    }

    /** 登录成功后清除账号与 IP 的失败计数。 */
    public void recordSuccess(AccountType accountType, String account, String clientIp) {
        String type = accountType.name();
        String normalized = normalizeAccount(account);
        redissonClient.getBucket(failureAccountKey(type, normalized)).delete();
        if (StringUtils.hasText(clientIp)) {
            redissonClient.getBucket(failureIpKey(type, clientIp)).delete();
        }
    }

    private void increaseFailure(
            String failureKey, String lockKey, int maxFailures, int windowSeconds, int lockSeconds) {
        if (maxFailures <= 0) {
            return;
        }
        RAtomicLong counter = redissonClient.getAtomicLong(failureKey);
        long count = counter.incrementAndGet();
        if (count == 1L && windowSeconds > 0) {
            counter.expire(Duration.ofSeconds(windowSeconds));
        }
        if (count >= maxFailures) {
            // 超限：设置锁定并清空计数窗口
            RBucket<String> lock = redissonClient.getBucket(lockKey);
            lock.set("1", Math.max(1, lockSeconds), TimeUnit.SECONDS);
            counter.delete();
        }
    }

    private boolean isLocked(String lockKey) {
        return Boolean.TRUE.equals(redissonClient.getBucket(lockKey).isExists());
    }

    private int typedInt(String typeName, String suffix, int defaultValue) {
        String key = "AUTH_LOGIN_" + typeName + "_" + suffix;
        String raw = configApi.getValue(key);
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        return configApi.getInt(key, defaultValue);
    }

    private static String normalizeAccount(String account) {
        return account == null ? "" : account.trim().toLowerCase(Locale.ROOT);
    }

    private static String failureAccountKey(String accountType, String account) {
        return "login:failure:account:" + accountType + ":" + account;
    }

    private static String failureIpKey(String accountType, String ip) {
        return "login:failure:ip:" + accountType + ":" + ip;
    }

    private static String lockAccountKey(String accountType, String account) {
        return "login:lock:account:" + accountType + ":" + account;
    }

    private static String lockIpKey(String accountType, String ip) {
        return "login:lock:ip:" + accountType + ":" + ip;
    }
}
