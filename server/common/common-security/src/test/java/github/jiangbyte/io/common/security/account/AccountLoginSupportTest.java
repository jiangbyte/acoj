package github.jiangbyte.io.common.security.account;

import github.jiangbyte.io.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountLoginSupportTest {

  @Test
  void acceptsValidLogin() {
    assertTrue(AccountLoginSupport.isValid("admin_iam"));
    assertEquals("admin_iam", AccountLoginSupport.requireLogin(" admin_iam "));
  }

  @Test
  void rejectsHyphenAndSpecialChars() {
    assertFalse(AccountLoginSupport.isValid("admin-iam"));
    assertThrows(BizException.class, () -> AccountLoginSupport.requireLogin("admin-iam"));
  }

  @Test
  void sanitizeBaseStripsInvalidChars() {
    assertEquals("adminiam", AccountLoginSupport.sanitizeBase("admin-iam"));
  }
}
