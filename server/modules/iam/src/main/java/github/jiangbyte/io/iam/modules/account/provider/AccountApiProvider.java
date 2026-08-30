package github.jiangbyte.io.iam.modules.account.provider;

import github.jiangbyte.io.iam.account.AccountApi;
import github.jiangbyte.io.iam.account.AccountAuthorizationInfo;
import github.jiangbyte.io.iam.account.AccountInfo;
import github.jiangbyte.io.iam.modules.account.convert.SysAccountConvert;
import github.jiangbyte.io.iam.modules.account.service.AccountService;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 跨模块 AccountApi 适配器实现：将 iam 域服务暴露给 auth 等模块，
 * 避免对方直接依赖 iam 实体与内部工具类。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AccountApiProvider implements AccountApi {

    private final AccountService accountService;
    private final SysAccountConvert accountConvert;

    @Override
    public AccountInfo findByIdentifier(String identifier, String identityType) {
        return accountConvert.toInfo(accountService.findByIdentifier(identifier, identityType));
    }

    @Override
    public AccountInfo getById(String accountId) {
        return accountConvert.toInfo(accountService.getById(accountId));
    }

    @Override
    public void updateLoginMeta(String accountId, String ip, OffsetDateTime time, String device) {
        accountService.updateLoginMeta(accountId, ip, time, device);
    }

    @Override
    public AccountAuthorizationInfo getAuthorization(String accountId) {
        AccountAuthorization auth = accountService.getAuthorization(accountId);
        if (auth == null) {
            return new AccountAuthorizationInfo();
        }
        return accountConvert.toAuthInfo(auth);
    }

    @Override
    public boolean matchesPassword(String rawPassword, String passwordHash) {
        return accountService.matchesPassword(rawPassword, passwordHash);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return accountService.encodePassword(rawPassword);
    }

    @Override
    public boolean isPasswordExpired(String accountId, int expireDays) {
        return accountService.isPasswordExpired(accountId, expireDays);
    }

    @Override
    public Integer getPasswordAgeDays(String accountId) {
        return accountService.getPasswordAgeDays(accountId);
    }

    @Override
    public String findIdentifier(String accountId, String identityType) {
        return accountService.findIdentifier(accountId, identityType);
    }

    @Override
    public void recordPasswordHistory(String accountId, String rawPassword, String operatorId, String reason) {
        accountService.recordPasswordHistory(accountId, rawPassword, operatorId, reason);
    }

    @Override
    public AccountInfo createPortalAccount(String account, String email, String encodedPassword) {
        return accountConvert.toInfo(accountService.createPortalAccount(account, email, encodedPassword));
    }

    @Override
    public void updatePasswordHash(String accountId, String passwordHash) {
        accountService.updatePasswordHash(accountId, passwordHash);
    }

    @Override
    public void cancelAccount(String accountId, String cancelledBy, String cancelReason) {
        accountService.cancelAccount(accountId, cancelledBy, cancelReason);
    }

    @Override
    public void upsertIdentity(String accountId, String type, String identifier, boolean enabled) {
        accountService.upsertIdentity(accountId, type, identifier, enabled);
    }

    @Override
    public void assignRole(String accountId, String roleId) {
        accountService.assignRole(accountId, roleId);
    }

    @Override
    public void assignPrimaryDept(String accountId, String deptId) {
        accountService.assignPrimaryDept(accountId, deptId);
    }
}
