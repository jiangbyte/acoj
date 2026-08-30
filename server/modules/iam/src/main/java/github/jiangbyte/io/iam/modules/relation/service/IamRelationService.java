package github.jiangbyte.io.iam.modules.relation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import github.jiangbyte.io.iam.modules.relation.entity.SysIamRelation;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IAM 关系服务接口：账号授权聚合、主体-目标关系查询与全量替换授权。
 *
 * Author: Charlie
 */
public interface IamRelationService extends IService<SysIamRelation> {

    /** 获取单个账号的完整授权视图。 */
    AccountAuthorization getAccountAuthorization(String accountId);

    /** 批量获取账号完整授权视图。 */
    Map<String, AccountAuthorization> getAccountsAuthorization(Collection<String> accountIds);

    /** 仅轻量成员关系：角色/部门/用户组 id（不展开资源/权限）。 */
    Map<String, AccountAuthorization> getAccountsMembership(Collection<String> accountIds);

    /** 按主体与关系类型列出目标 id。 */
    List<String> listTargetIds(String subjectType, String subjectId, String relationType);

    /**
     * 按主体、关系类型与账号类型列出目标 id。
     * @param subjectType 主体类型
     * @param subjectId 主体 id
     * @param relationType 关系类型
     * @param accountType 账号类型
     */
    List<String> listTargetIds(String subjectType, String subjectId, String relationType, String accountType);

    /** 按关系类型与目标列出主体 id。 */
    List<String> listSubjectIds(String relationType, String targetType, String targetId);

    /** 列出账号的部门授予明细。 */
    List<SysDeptGrantResult> listAccountDepts(String accountId);

    /** 列出主体的管理端资源授予明细。 */
    List<SysResourceGrantResult> listSubjectResourceGrants(String subjectType, String subjectId);

    /** 列出主体的管理端资源授予明细。 */
    List<SysResourceGrantResult> listSubjectResourceGrants(String subjectType, String subjectId, String accountType);

    /** 列出主体的客户端资源授予明细。 */
    List<SysResourceGrantResult> listSubjectClientResourceGrants(String subjectType, String subjectId, String accountType);

    /** 全量替换账号-角色关系。 */
    void replaceAccountRoles(String accountId, List<String> roleIds);

    /** 全量替换账号-用户组关系。 */
    void replaceAccountGroups(String accountId, List<String> groupIds);

    /** 全量替换账号-部门关系。 */
    void replaceAccountDepts(String accountId, List<SysDeptGrantResult> grantInfoList);

    /**
     * 全量替换主体管理端资源授予。
     * @param subjectType 主体类型
     * @param subjectId 主体 id
     * @param grants 资源授予明细
     * @param accountType 账号类型
     */
    void replaceSubjectResourceGrants(
            String subjectType, String subjectId, List<SysResourceGrantResult> grants, String accountType);

    /**
     * 全量替换主体客户端资源授予。
     * @param subjectType 主体类型
     * @param subjectId 主体 id
     * @param grants 资源授予明细
     * @param accountType 账号类型
     */
    void replaceSubjectClientResourceGrants(
            String subjectType, String subjectId, List<SysResourceGrantResult> grants, String accountType);

    /** 全量替换角色成员账号。 */
    void replaceRoleUsers(String roleId, List<String> accountIds);

    /** 全量替换用户组成员账号。 */
    void replaceGroupUsers(String groupId, List<String> accountIds);

    /** 全量替换用户组-角色关系。 */
    void replaceGroupRoles(String groupId, List<String> roleIds, String accountType);

    /**
     * 绑定管理端资源与权限（含数据范围）。
     * @param resourceId 资源 id
     * @param permissionKey 权限键
     * @param accountType 账号类型
     * @param dataScope 数据范围
     * @param customScopeDeptIds 自定义部门范围
     * @param sort 排序
     * @param description 描述
     */
    void bindResourcePermission(
            String resourceId,
            String permissionKey,
            String accountType,
            String dataScope,
            List<String> customScopeDeptIds,
            Integer sort,
            String description);

    /**
     * 绑定客户端资源与权限（含数据范围）。
     * @param resourceId 资源 id
     * @param permissionKey 权限键
     * @param accountType 账号类型
     * @param dataScope 数据范围
     * @param customScopeDeptIds 自定义部门范围
     * @param sort 排序
     * @param description 描述
     */
    void bindClientResourcePermission(
            String resourceId,
            String permissionKey,
            String accountType,
            String dataScope,
            List<String> customScopeDeptIds,
            Integer sort,
            String description);

    /** 按主体删除指定关系类型记录。 */
    void deleteSubjectRelations(String subjectType, String subjectId, String relationType);

    /**
     * 按主体删除指定关系类型记录。
     * @param subjectType 主体类型
     * @param subjectId 主体 id
     * @param relationType 关系类型
     * @param accountType 账号类型
     */
    void deleteSubjectRelations(String subjectType, String subjectId, String relationType, String accountType);

    /** 按主体 id 集合删除指定关系类型记录。 */
    void deleteSubjectRelations(String subjectType, Collection<String> subjectIds, String relationType);

    /** 按主体 id 集合删除全部相关关系。 */
    void deleteBySubjectIds(String subjectType, Collection<String> subjectIds);

    /** 按目标 id 集合删除全部相关关系。 */
    void deleteByTargetIds(String targetType, Collection<String> targetIds);

}
