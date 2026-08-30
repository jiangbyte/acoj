package github.jiangbyte.io.auth.modules.login.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.sys.config.SiteFooterResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录页公开配置：可用登录方式、注册开关/通道、强制绑定、三方入口与版权文案。
 *
 * Author: Charlie
 */
@Schema(description = "登录页公开配置：可用登录方式、注册开关/通道、强制绑定、三方入口与版权文案。")
@Data
public class AuthOptionsResult {
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private AccountType accountType;
    @Schema(description = "allowAccount")
    private Boolean allowAccount = true;
    @Schema(description = "allowEmail")
    private Boolean allowEmail = true;
    @Schema(description = "allowPhone")
    private Boolean allowPhone = true;
    @Schema(description = "allowOtp")
    private Boolean allowOtp = true;
    @Schema(description = "registerEnabled")
    private Boolean registerEnabled = false;
    @Schema(description = "门户注册是否允许用户名通道")
    /** 门户注册是否允许用户名通道 */
    private Boolean registerAllowAccount = false;
    @Schema(description = "门户注册是否允许邮箱通道")
    /** 门户注册是否允许邮箱通道 */
    private Boolean registerAllowEmail = false;
    @Schema(description = "门户注册是否允许手机通道")
    /** 门户注册是否允许手机通道 */
    private Boolean registerAllowPhone = false;
    @Schema(description = "用户名通道注册是否必填邮箱")
    /** 用户名通道注册是否必填邮箱 */
    private Boolean registerRequireEmail = false;
    @Schema(description = "用户名通道注册是否必填手机号")
    /** 用户名通道注册是否必填手机号 */
    private Boolean registerRequirePhone = false;
    @Schema(description = "当前端是否强制绑定邮箱")
    /** 当前端是否强制绑定邮箱 */
    private Boolean forceBindEmail = false;
    @Schema(description = "当前端是否强制绑定手机")
    /** 当前端是否强制绑定手机 */
    private Boolean forceBindPhone = false;
    @Schema(description = "oauthProviders")
    private List<OauthProviderOptionResult> oauthProviders = new ArrayList<>();
    @Schema(description = "passwordChangeVerifyMethod")
    private String passwordChangeVerifyMethod = "OLD_PASSWORD";
    @Schema(description = "copyrightText")
    private String copyrightText = "";
    @Schema(description = "copyrightUrl")
    private String copyrightUrl = "";
    @Schema(description = "版权与备案（与顶层 copyright 字段同源，便于前端统一消费）")
    /** 版权与备案（与顶层 copyright 字段同源，便于前端统一消费） */
    private SiteFooterResult siteFooter = new SiteFooterResult();
}
