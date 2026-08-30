package github.jiangbyte.io.profile.modules.portal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.auth.login.AuthApi;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.profile.modules.portal.param.BindCodeParam;
import github.jiangbyte.io.profile.modules.portal.param.EmailUpdateParam;
import github.jiangbyte.io.profile.modules.portal.param.PasswordUpdateParam;
import github.jiangbyte.io.profile.modules.portal.param.PhoneUpdateParam;
import github.jiangbyte.io.profile.modules.portal.param.ProfileUpdateParam;
import github.jiangbyte.io.profile.modules.portal.result.AvatarUpdateResult;
import github.jiangbyte.io.profile.modules.portal.result.MeResult;
import github.jiangbyte.io.profile.modules.portal.result.PublicProfileResult;
import github.jiangbyte.io.profile.modules.portal.service.ProfileUserPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 门户端用户中心 API：当前用户、资料/头像维护、改密与换绑，以及公开空间资料查询。
 *
 * Author: Charlie
 */
@Tag(name = "门户端用户中心 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalProfileController {

    private final ProfileUserPortalService portalUserProfileService;
    private final AuthApi authApi;

    /** 获取当前登录门户用户摘要（含资料与组织名称）。 */
    @Operation(summary = "获取当前登录门户用户摘要（含资料与组织名称）。")
    @GetMapping("/v1/portal/me")
    public ApiResponse<MeResult> me() {
        return ApiResponse.ok(portalUserProfileService.currentMe());
    }

    /** 更新当前用户个人资料（姓名、昵称、签名等）。 */
    @Operation(summary = "更新当前用户个人资料（姓名、昵称、签名等）。")
    @PostMapping("/v1/portal/profile/update")
    @OperationAudit(resourceType = "profile_center", action = "update_profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateParam request) {
        portalUserProfileService.updateProfile(request);
        return ApiResponse.ok();
    }

    /** 上传并替换当前用户头像。 */
    @Operation(summary = "上传并替换当前用户头像。")
    @PostMapping("/v1/portal/profile/avatar/upload")
    public ApiResponse<AvatarUpdateResult> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(portalUserProfileService.uploadAvatar(file));
    }

    /** 按系统配置向绑定邮箱/手机发送改密验证码。 */
    @Operation(summary = "按系统配置向绑定邮箱/手机发送改密验证码。")
    @PostMapping("/v1/portal/profile/password/send-code")
    public ApiResponse<Void> sendPasswordCode() {
        authApi.sendChangePasswordCode();
        return ApiResponse.ok();
    }

    /** 修改当前用户登录密码（旧密码或 OTP 校验）。 */
    @Operation(summary = "修改当前用户登录密码（旧密码或 OTP 校验）。")
    @PostMapping("/v1/portal/profile/password/update")
    @OperationAudit(resourceType = "profile_center", action = "update_password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateParam request) {
        authApi.updateCurrentPassword(
                request.getPasswordKeyId(),
                request.getOldPassword(),
                request.getNewPassword(),
                request.getOtpCode());
        return ApiResponse.ok();
    }

    /** 向待绑定手机发送验证码。 */
    @Operation(summary = "向待绑定手机发送验证码。")
    @PostMapping("/v1/portal/profile/phone/send-code")
    public ApiResponse<Void> sendPhoneCode(@Valid @RequestBody BindCodeParam request) {
        authApi.sendBindPhoneCode(request.getTarget());
        return ApiResponse.ok();
    }

    /** 更新当前用户手机号及是否允许手机登录。 */
    @Operation(summary = "更新当前用户手机号及是否允许手机登录。")
    @PostMapping("/v1/portal/profile/phone/update")
    @OperationAudit(resourceType = "profile_center", action = "update_phone")
    public ApiResponse<Void> updatePhone(@Valid @RequestBody PhoneUpdateParam request) {
        authApi.updateCurrentPhone(
                request.getPasswordKeyId(),
                request.getPassword(),
                request.getPhone(),
                Boolean.TRUE.equals(request.getPhoneLoginEnabled()),
                request.getOtpCode());
        return ApiResponse.ok();
    }

    /** 向待绑定邮箱发送验证码。 */
    @Operation(summary = "向待绑定邮箱发送验证码。")
    @PostMapping("/v1/portal/profile/email/send-code")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody BindCodeParam request) {
        authApi.sendBindEmailCode(request.getTarget());
        return ApiResponse.ok();
    }

    /** 更新当前用户邮箱及是否允许邮箱登录。 */
    @Operation(summary = "更新当前用户邮箱及是否允许邮箱登录。")
    @PostMapping("/v1/portal/profile/email/update")
    @OperationAudit(resourceType = "profile_center", action = "update_email")
    public ApiResponse<Void> updateEmail(@Valid @RequestBody EmailUpdateParam request) {
        authApi.updateCurrentEmail(
                request.getPasswordKeyId(),
                request.getPassword(),
                request.getEmail(),
                Boolean.TRUE.equals(request.getEmailLoginEnabled()),
                request.getOtpCode());
        return ApiResponse.ok();
    }

    /** 按账号 ID 查询门户公开资料（空间详情）。 */
    @Operation(summary = "按账号 ID 查询门户公开资料（空间详情）。")
    @GetMapping("/v1/portal/spaces/detail")
    public ApiResponse<PublicProfileResult> spacesDetail(@RequestParam("account_id") String accountId) {
        return ApiResponse.ok(portalUserProfileService.publicProfile(accountId));
    }
}