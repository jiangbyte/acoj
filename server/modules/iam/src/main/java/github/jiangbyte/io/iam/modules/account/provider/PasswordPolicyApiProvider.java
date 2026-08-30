package github.jiangbyte.io.iam.modules.account.provider;

import github.jiangbyte.io.iam.modules.account.support.PasswordHelper;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.iam.password.PasswordPolicyApi;
import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.sys.weakpassword.WeakPasswordApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * PasswordPolicyApi 实现：复杂度、连续字符、弱口令与用户信息包含检查。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyApiProvider implements PasswordPolicyApi {

    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");

    private final WeakPasswordApi weakPasswordApi;
    private final PasswordHelper passwordHelper;
    private final ConfigApi configApi;

    /**
     * 断言密码满足策略，否则抛业务异常。
     * @param rawPassword 明文密码
     * @param accountId 账号 id（可空）
     * @param accountName 账号名（可空）
     * @param email 邮箱（可空）
     * @param phone 手机（可空）
     */
    @Override
    public void assertValid(
            String rawPassword,
            String accountId,
            String accountName,
            String email,
            String phone) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new BizException("Password is required");
        }
        int minLen = configApi.getInt("PASSWORD_MIN_LENGTH", 8);
        int maxLen = configApi.getInt("PASSWORD_MAX_LENGTH", 128);
        if (rawPassword.length() < minLen) {
            throw new BizException("Password must be at least " + minLen + " characters");
        }
        if (rawPassword.length() > maxLen) {
            throw new BizException("Password must not exceed " + maxLen + " characters");
        }
    /** 检查密码复杂度配置。 */
        checkComplexity(rawPassword, configApi.getValue("PASSWORD_COMPLEXITY", "DIGITS_UPPER_LOWER_SPECIAL"));
    /** 检查最大连续相同字符。 */
        checkMaxConsecutive(rawPassword, configApi.getInt("PASSWORD_MAX_CONSECUTIVE_CHARS", 3));

        if (configApi.getBoolean("PASSWORD_FORBID_WEAK_LIST", true)) {
            if (isBuiltinOrCustomWeak(rawPassword, configApi.getValue("PASSWORD_CUSTOM_WEAK_WORDS", ""))) {
                throw new BizException("Password is too common");
            }
            weakPasswordApi.assertNotWeak(rawPassword);
        }

        if (configApi.getBoolean("PASSWORD_FORBID_USER_INFO", true)
                && containsUserInfo(rawPassword, accountName, email, phone)) {
            throw new BizException("Password must not contain account, email, or phone");
        }

        if (StringUtils.hasText(accountId)
                && configApi.getBoolean("PASSWORD_FORBID_HISTORICAL", true)) {
            int historyCount = configApi.getInt("PASSWORD_HISTORY_CHECK_COUNT", 5);
            if (passwordHelper.matchesRecentPassword(accountId, rawPassword, historyCount)) {
                throw new BizException("Password must not reuse recent passwords");
            }
        }
    }

    /** 检查密码复杂度配置。 */
    private static void checkComplexity(String password, String complexity) {
        String key = complexity == null ? "" : complexity.trim().toUpperCase(Locale.ROOT);
        boolean hasUpper = HAS_UPPER.matcher(password).find();
        boolean hasLower = HAS_LOWER.matcher(password).find();
        boolean hasDigit = HAS_DIGIT.matcher(password).find();
        boolean hasSpecial = HAS_SPECIAL.matcher(password).find();
        boolean hasLetter = HAS_LETTER.matcher(password).find();

        if ("NO_LIMIT".equals(key)) {
            return;
        }
        if ("DIGITS_AND_LETTERS".equals(key)) {
            if (!hasDigit || !hasLetter) {
                throw new BizException("Password must contain both letters and digits");
            }
            return;
        }
        if ("DIGITS_AND_UPPERCASE".equals(key)) {
    /**
     * 检查大小写/数字/特殊字符类别要求。
     * @param hasUpper 是否含大写
     * @param hasLower 是否含小写
     * @param hasDigit 是否含数字
     * @param hasSpecial 是否含特殊字符
     * @param requireUpper 是否要求大写
     * @param requireLower 是否要求小写
     * @param requireDigit 是否要求数字
     * @param requireSpecial 是否要求特殊字符
     */
            requireClasses(hasUpper, hasLower, hasDigit, hasSpecial, true, false, true, false);
            return;
        }
        if ("DIGITS_UPPER_LOWER_SPECIAL".equals(key)) {
    /** 检查大小写/数字/特殊字符类别要求。 */
            requireClasses(hasUpper, hasLower, hasDigit, hasSpecial, true, true, true, true);
            return;
        }
        if ("TWO_OF_THREE".equals(key)) {
            int classes = (hasLetter ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
            if (classes < 2) {
                throw new BizException("Password must include at least two of: letters, digits, special characters");
            }
            return;
        }
        if ("THREE_OF_FOUR".equals(key)) {
            int classes = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
            if (classes < 3) {
                throw new BizException("Password must include at least three of: upper, lower, digit, special");
            }
            return;
        }
    /** 检查大小写/数字/特殊字符类别要求。 */
        requireClasses(hasUpper, hasLower, hasDigit, hasSpecial, true, true, true, true);
    }

    /** 检查大小写/数字/特殊字符类别要求。 */
    private static void requireClasses(
            boolean hasUpper,
            boolean hasLower,
            boolean hasDigit,
            boolean hasSpecial,
            boolean requireUpper,
            boolean requireLower,
            boolean requireDigit,
            boolean requireSpecial) {
        if (requireUpper && !hasUpper) {
            throw new BizException("Password must contain at least one uppercase letter");
        }
        if (requireLower && !hasLower) {
            throw new BizException("Password must contain at least one lowercase letter");
        }
        if (requireDigit && !hasDigit) {
            throw new BizException("Password must contain at least one digit");
        }
        if (requireSpecial && !hasSpecial) {
            throw new BizException("Password must contain at least one special character");
        }
    }

    /** 检查最大连续相同字符。 */
    private static void checkMaxConsecutive(String password, int maxConsecutive) {
        if (maxConsecutive <= 0 || password.length() <= maxConsecutive) {
            return;
        }
        int run = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                run++;
                if (run > maxConsecutive) {
                    throw new BizException(
                            "Password must not contain more than " + maxConsecutive + " consecutive identical characters");
                }
            } else {
                run = 1;
            }
        }
    }

    /** 是否命中内置或自定义弱口令。 */
    private static boolean isBuiltinOrCustomWeak(String password, String custom) {
        String lowered = password.toLowerCase(Locale.ROOT);
        if (StringUtils.hasText(custom)) {
            for (String part : custom.split(",")) {
                String word = part.trim().toLowerCase(Locale.ROOT);
                if (!word.isEmpty() && word.equals(lowered)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 密码是否包含用户信息片段。 */
    private static boolean containsUserInfo(String password, String accountName, String email, String phone) {
        String lowered = password.toLowerCase(Locale.ROOT);
        if (matchesFragment(lowered, accountName)) {
            return true;
        }
        if (StringUtils.hasText(email)) {
            String normalized = email.trim().toLowerCase(Locale.ROOT);
            if (matchesFragment(lowered, normalized)) {
                return true;
            }
            int at = normalized.indexOf('@');
            if (at > 0 && matchesFragment(lowered, normalized.substring(0, at))) {
                return true;
            }
        }
    /** 小写密码是否包含片段。 */
        return matchesFragment(lowered, phone);
    }

    /** 小写密码是否包含片段。 */
    private static boolean matchesFragment(String loweredPassword, String fragment) {
        if (!StringUtils.hasText(fragment)) {
            return false;
        }
        String item = fragment.trim().toLowerCase(Locale.ROOT);
        return item.length() >= 3 && loweredPassword.contains(item);
    }
}
