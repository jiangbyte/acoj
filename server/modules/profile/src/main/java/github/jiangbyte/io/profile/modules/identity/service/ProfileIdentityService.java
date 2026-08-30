package github.jiangbyte.io.profile.modules.identity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.param.IdentityPageParam;
import github.jiangbyte.io.profile.modules.identity.param.IdentityRevokeParam;
import github.jiangbyte.io.profile.modules.identity.result.IdentityPageResult;
import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 账号实名认证快照领域服务。
 *
 * Author: Charlie
 */
public interface ProfileIdentityService {

    /** 读取当前账号实名状态（含进行中工单摘要）。 */
    IdentityStatusResult getStatusForAccount(String accountId);

    /** 用户侧实名状态：隐藏审核通道等内部字段。 */
    IdentityStatusResult getUserStatusForAccount(String accountId);

    /** 审核通过后 upsert 认证快照。 */
    void upsertOnApprove(RealNameCase caseEntity, String reviewerId);

    /** 撤销账号实名认证。 */
    void revoke(IdentityRevokeParam param, String operatorId);

    /** 管理端分页查询已认证快照。 */
    Page<IdentityPageResult> page(IdentityPageParam param);

    /** 批量解析展示名：已认证姓名优先，否则 null。 */
    Map<String, String> getVerifiedRealNames(Collection<String> accountIds);

    /** 按姓名模糊匹配已认证账号 ID（解密后内存过滤）。 */
    Set<String> findAccountIdsByRealName(String name);

    /** 账号是否已通过实名认证。 */
    boolean isVerified(String accountId);
}
