package github.jiangbyte.io.iam.modules.account.convert;

import github.jiangbyte.io.iam.account.AccountAuthorizationInfo;
import github.jiangbyte.io.iam.account.AccountInfo;
import github.jiangbyte.io.iam.account.PermissionGrantInfo;
import github.jiangbyte.io.iam.modules.account.entity.SysAccount;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountIdentity;
import github.jiangbyte.io.iam.modules.account.param.SysAccountAddParam;
import github.jiangbyte.io.iam.modules.account.param.SysAccountEditParam;
import github.jiangbyte.io.iam.modules.account.result.AccountIdentityResult;
import github.jiangbyte.io.iam.modules.account.result.SysAccountResult;
import github.jiangbyte.io.iam.modules.account.support.AccountAuthorization;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 账号模块 MapStruct 转换：入参/实体/结果、跨模块 AccountInfo 与授权信息映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysAccountConvert {

    /** 新增入参转账号实体。 */
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "account", "password", "passwordKeyId",
                    "nickname", "avatar", "signature", "phone", "email", "remark"
            },
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    SysAccount toEntity(SysAccountAddParam param);

    /** 编辑入参更新到账号实体。 */
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "account", "password", "passwordKeyId",
                    "nickname", "avatar", "signature", "phone", "email", "remark"
            },
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void update(SysAccountEditParam param, @MappingTarget SysAccount entity);

    /** 账号实体转结果 DTO。 */
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "signature", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "emailLoginEnabled", ignore = true)
    @Mapping(target = "phoneLoginEnabled", ignore = true)
    @Mapping(target = "emailIdentity", ignore = true)
    @Mapping(target = "phoneIdentity", ignore = true)
    @Mapping(target = "emailIdentityVerified", ignore = true)
    @Mapping(target = "phoneIdentityVerified", ignore = true)
    @Mapping(target = "emailIdentityBindStatus", ignore = true)
    @Mapping(target = "phoneIdentityBindStatus", ignore = true)
    @Mapping(target = "identities", ignore = true)
    @Mapping(target = "oauthBindings", ignore = true)
    @Mapping(target = "remark", ignore = true)
    SysAccountResult toResult(SysAccount entity);

    /** 身份实体转结果。 */
    AccountIdentityResult toIdentityResult(SysAccountIdentity entity);

    /** 身份实体列表转结果列表。 */
    List<AccountIdentityResult> toIdentityResultList(List<SysAccountIdentity> list);

    /** 账号实体转跨模块 AccountInfo。 */
    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    AccountInfo toInfo(SysAccount account);

    /** 授权聚合转跨模块 AccountAuthorizationInfo。 */
    @Mapping(target = "permissionGrants", source = "permissionGrants", qualifiedByName = "toPermissionGrantInfos")
    AccountAuthorizationInfo toAuthInfo(AccountAuthorization auth);

    /** 登录权限授予列表转跨模块 DTO。 */
    @Named("toPermissionGrantInfos")
    default List<PermissionGrantInfo> toPermissionGrantInfos(List<LoginUser.PermissionGrant> grants) {
        return PermissionGrantInfo.fromLoginGrants(grants);
    }
}
