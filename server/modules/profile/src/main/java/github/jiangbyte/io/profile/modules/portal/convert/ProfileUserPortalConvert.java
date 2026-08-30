package github.jiangbyte.io.profile.modules.portal.convert;

import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.profile.portal.ProfileUserPortalInfo;
import github.jiangbyte.io.profile.modules.portal.entity.ProfileUserPortal;
import github.jiangbyte.io.profile.modules.portal.param.ProfileUpdateParam;
import github.jiangbyte.io.profile.modules.portal.result.MeResult;
import github.jiangbyte.io.profile.modules.portal.result.PublicProfileResult;
import github.jiangbyte.io.profile.modules.portal.result.UserProfileResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * 门户端用户资料对象转换：实体 / Info / 请求参数与 me、公开资料、资料 DTO 之间的 MapStruct 映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileUserPortalConvert {

    /** 将会话用户与资料 DTO 组装为 me 响应（组织名称由调用方补齐）。 */
    @Mapping(target = "accountId", source = "loginUser.accountId")
    @Mapping(target = "account", source = "loginUser.account")
    @Mapping(target = "accountType", source = "loginUser.accountType")
    @Mapping(target = "roleIds", source = "loginUser.roleIds")
    @Mapping(target = "deptIds", source = "loginUser.deptIds")
    @Mapping(target = "groupIds", source = "loginUser.groupIds")
    @Mapping(target = "permissionKeys", source = "loginUser.permissions")
    @Mapping(target = "passwordExpired", source = "loginUser.passwordExpired")
    @Mapping(target = "nickname", source = "profile.nickname")
    @Mapping(target = "avatar", source = "profile.avatar")
    @Mapping(target = "profile", source = "profile")
    @Mapping(target = "identity", ignore = true)
    @Mapping(target = "roleIdNames", ignore = true)
    @Mapping(target = "deptIdNames", ignore = true)
    @Mapping(target = "groupIdNames", ignore = true)
    @Mapping(target = "forceBindEmail", ignore = true)
    @Mapping(target = "forceBindPhone", ignore = true)
    @Mapping(target = "forceBindIdentity", ignore = true)
    MeResult toMe(LoginUser loginUser, UserProfileResult profile);

    /** 实体转为用户中心资料 DTO。 */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    UserProfileResult toDto(ProfileUserPortal profile);

    /** 实体转为门户公开资料响应（对外昵称、账号、头像、签名；不含联系方式）。 */
    @Mapping(target = "account", ignore = true)
    PublicProfileResult toPublic(ProfileUserPortal profile);

    /** 实体转为跨模块 Info。 */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    ProfileUserPortalInfo toInfo(ProfileUserPortal profile);

    /** 用 Info 覆盖实体可变字段（保留主键与审计字段；name 不再写入 profile 表）。 */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE, ignoreUnmappedSourceProperties = {"name"})
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateInfo(ProfileUserPortalInfo info, @MappingTarget ProfileUserPortal profile);

    /** 用资料更新请求覆盖实体（头像/手机/邮箱由调用方单独处理）。 */
    @BeanMapping(
            ignoreUnmappedSourceProperties = {"remark", "avatar"},
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", ignore = true)
    void update(ProfileUpdateParam request, @MappingTarget ProfileUserPortal profile);
}
