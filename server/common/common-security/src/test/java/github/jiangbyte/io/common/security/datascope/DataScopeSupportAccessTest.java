package github.jiangbyte.io.common.security.datascope;

import github.jiangbyte.io.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Charlie
 */
class DataScopeSupportAccessTest {

    @Test
    void allAllowsAccountAndOwnerOrDept() {
        DataScopeConstraint all = new DataScopeConstraint.All();
        assertTrue(DataScopeSupport.allowsAccount(all, "acc-1"));
        assertTrue(DataScopeSupport.allowsOwnerOrDept(all, "acc-1", "dept-1"));
        DataScopeSupport.assertAccountAccessible(all, "acc-other");
        DataScopeSupport.assertOwnerOrDeptAccessible(all, "acc-other", "dept-other");
    }

    @Test
    void selfAllowsMatchingAccountAndDeniesOthers() {
        DataScopeConstraint self = new DataScopeConstraint.Self("acc-1");
        assertTrue(DataScopeSupport.allowsAccount(self, "acc-1"));
        assertFalse(DataScopeSupport.allowsAccount(self, "acc-2"));
        assertFalse(DataScopeSupport.allowsAccount(self, null));
        assertTrue(DataScopeSupport.allowsOwnerOrDept(self, "acc-1", "dept-x"));
        assertFalse(DataScopeSupport.allowsOwnerOrDept(self, "acc-2", "dept-x"));

        DataScopeSupport.assertAccountAccessible(self, "acc-1");
        BizException denied = assertThrows(
                BizException.class,
                () -> DataScopeSupport.assertAccountAccessible(self, "acc-2"));
        assertEquals(403, denied.getCode());
        assertEquals("无权访问该数据", denied.getMessage());
    }

    @Test
    void deptsAllowsMatchingOwnerDeptAndDeniesOthers() {
        DataScopeConstraint depts = new DataScopeConstraint.Depts(List.of("dept-a", "dept-b"));
        assertFalse(DataScopeSupport.allowsAccount(depts, "acc-1"));
        assertTrue(DataScopeSupport.allowsOwnerOrDept(depts, "acc-any", "dept-a"));
        assertTrue(DataScopeSupport.allowsOwnerOrDept(depts, null, "dept-b"));
        assertFalse(DataScopeSupport.allowsOwnerOrDept(depts, "acc-any", "dept-c"));
        assertFalse(DataScopeSupport.allowsOwnerOrDept(depts, "acc-any", ""));
        assertFalse(DataScopeSupport.allowsOwnerOrDept(depts, "acc-any", null));

        DataScopeSupport.assertOwnerOrDeptAccessible(depts, "acc-any", "dept-a");
        BizException denied = assertThrows(
                BizException.class,
                () -> DataScopeSupport.assertOwnerOrDeptAccessible(depts, "acc-any", "dept-c"));
        assertEquals(403, denied.getCode());
        assertEquals("无权访问该数据", denied.getMessage());
    }

    @Test
    void denyRejectsAllAccess() {
        DataScopeConstraint deny = new DataScopeConstraint.Deny();
        assertFalse(DataScopeSupport.allowsAccount(deny, "acc-1"));
        assertFalse(DataScopeSupport.allowsOwnerOrDept(deny, "acc-1", "dept-1"));
        BizException accountDenied = assertThrows(
                BizException.class,
                () -> DataScopeSupport.assertAccountAccessible(deny, "acc-1"));
        assertEquals(403, accountDenied.getCode());
        BizException ownerDenied = assertThrows(
                BizException.class,
                () -> DataScopeSupport.assertOwnerOrDeptAccessible(deny, "acc-1", "dept-1"));
        assertEquals("无权访问该数据", ownerDenied.getMessage());
    }
}
