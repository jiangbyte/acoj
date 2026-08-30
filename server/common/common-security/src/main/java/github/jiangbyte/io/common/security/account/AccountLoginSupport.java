package github.jiangbyte.io.common.security.account;

import github.jiangbyte.io.common.core.exception.BizException;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 账号登录名规则：仅允许字母、数字、下划线，长度 3-64。
 *
 * Author: Charlie
 */
public final class AccountLoginSupport {

  public static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,64}$");

  private AccountLoginSupport() {}

  public static boolean isValid(String account) {
    if (!StringUtils.hasText(account)) {
      return false;
    }
    return LOGIN_PATTERN.matcher(account.trim()).matches();
  }

  public static String requireLogin(String account) {
    if (!StringUtils.hasText(account)) {
      throw new BizException("请输入用户名");
    }
    String value = account.trim();
    if (!LOGIN_PATTERN.matcher(value).matches()) {
      throw new BizException("账号仅允许字母、数字和下划线，长度 3-64");
    }
    return value;
  }

  public static String sanitizeBase(String raw) {
    String cleaned = raw == null ? "" : raw.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase(Locale.ROOT);
    if (!StringUtils.hasText(cleaned)) {
      cleaned = "user";
    }
    if (cleaned.length() < 3) {
      cleaned = cleaned + "000".substring(0, 3 - cleaned.length());
    }
    if (cleaned.length() > 48) {
      cleaned = cleaned.substring(0, 48);
    }
    return cleaned;
  }
}
