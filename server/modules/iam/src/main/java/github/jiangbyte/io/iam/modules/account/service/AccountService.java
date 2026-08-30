package github.jiangbyte.io.iam.modules.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.account.entity.SysAccount;
import github.jiangbyte.io.iam.modules.account.param.SysAccountAddParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountEditParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantDeptParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantGroupParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantResourceParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountGrantRoleParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountPageParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountUpdateLoginIdentityParam;
import github.jiangbyte.io.iam.modules.account.result.SysAccountListResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnDeptResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnGroupResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountOwnRoleResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountResult;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceOwnResult;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 账号领域服务接口：登录元数据、密码与身份、管理端 CRUD，
 * 以及角色/用户组/部门/资源授权与查询。
 *
 * Author: Charlie
 */
public interface AccountService extends IService<SysAccount> {

    /** 按身份类型与标识查找账号。 */
    SysAccount findByIdentifier(String identifier, String identityType);

    /** 返回账号的主登录标识（ACCOUNT 类型）。 */
    String primaryAccountIdentifier(String accountId);

    /**
     * 更新最近登录 IP、时间与设备信息。
     * @param accountId 账号 id
     * @param ip 登录 IP
     * @param time 登录时间
     * @param device 设备信息
     */
    void updateLoginMeta(String accountId, String ip, OffsetDateTime time, String device);

    /** 聚合账号角色/部门/用户组/权限等授权视图。 */
    AccountAuthorization getAuthorization(String accountId);

    /** 校验明文密码与哈希是否匹配。 */
    boolean matchesPassword(String rawPassword, String passwordHash);

    /** 对明文密码编码为存储哈希。 */
    String encodePassword(String rawPassword);

    /** 按过期天数判断密码是否已过期。 */
    boolean isPasswordExpired(String accountId, int expireDays);

    /** 返回密码已使用天数；无历史则可能为 null。 */
    Integer getPasswordAgeDays(String accountId);

    /** 按账号与身份类型查找标识字符串。 */
    String findIdentifier(String accountId, String identityType);

    /**
     * 记录一次密码变更历史。
     * @param accountId 账号 id
     * @param rawPassword 明文密码（将编码后入库）
     * @param operatorId 操作人 id
     * @param reason 变更原因
     */
    void recordPasswordHistory(String accountId, String rawPassword, String operatorId, String reason);

    /**
     * 创建门户账号 + ACCOUNT 身份，可选 EMAIL 身份。
     * 密码须已编码。调用方负责资料与密码历史。
     */
    SysAccount createPortalAccount(String account, String email, String encodedPassword);

    /** 更新账号密码哈希。 */
    void updatePasswordHash(String accountId, String passwordHash);

    /** 取消/注销账号并记录操作人与原因。 */
    void cancelAccount(String accountId, String cancelledBy, String cancelReason);

    /**
     * 新增或更新账号身份标识。
     * @param accountId 账号 id
     * @param type 身份类型
     * @param identifier 登录标识
     * @param enabled 是否启用
     */
    void upsertIdentity(String accountId, String type, String identifier, boolean enabled);

    /** 为账号追加单个角色关系。 */
    void assignRole(String accountId, String roleId);

    /** 为账号设置主部门关系。 */
    void assignPrimaryDept(String accountId, String deptId);

    /** 管理端创建账号。 */
    void create(SysAccountAddParam param);

    /** 管理端更新账号。 */
    void update(SysAccountEditParam param);

    /** 管理端更新账号邮箱/手机号登录身份。 */
    void updateLoginIdentity(SysAccountUpdateLoginIdentityParam param);

    /** 管理端批量删除账号。 */
    void delete(IdsParam param);

    /** 物理清理超过保留期的已取消账号，返回清理条数。 */
    int purgeExpiredCancelledAccounts(int retentionDays);

    /** 账号详情（含资料与身份）。 */
    SysAccountResult detail(String id);

    /** 账号分页查询（仅 account + profile 展示字段）。 */
    Page<SysAccountListResult> page(SysAccountPageParam param);

    /** 查询账号已拥有角色。 */
    SysAccountOwnRoleResult ownRoles(String id);

    /** 全量替换账号角色授权。 */
    void grantRoles(SysAccountGrantRoleParam param);

    /** 查询账号已拥有用户组。 */
    SysAccountOwnGroupResult ownGroups(String id);

    /** 全量替换账号用户组授权。 */
    void grantGroups(SysAccountGrantGroupParam param);

    /** 查询账号已拥有部门。 */
    SysAccountOwnDeptResult ownDepts(String id);

    /** 全量替换账号部门授权。 */
    void grantDepts(SysAccountGrantDeptParam param);

    /** 查询账号已拥有管理端资源。 */
    SysResourceOwnResult ownResources(String id);

    /** 全量替换账号管理端资源授权。 */
    void grantResources(SysAccountGrantResourceParam param);

    /** 查询账号已拥有客户端资源。 */
    SysResourceOwnResult ownClientResources(String id);

    /** 全量替换账号客户端资源授权。 */
    void grantClientResources(SysAccountGrantResourceParam param);

    /** 按 id 列表批量查询账号结果。 */
    List<SysAccountResult> listResultsByIds(List<String> ids);

}
