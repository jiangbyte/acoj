package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号身份结果 DTO，用于详情中展示登录标识列表。
 *
 * Author: Charlie
 */
@Schema(description = "账号身份结果 DTO，用于详情中展示登录标识列表。")
@Data
public class AccountIdentityResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "identityType")
    private String identityType;
    @Schema(description = "identifier")
    private String identifier;
    @Schema(description = "是否已验证：1 是 / 0 否")
    private Boolean verified;
    @Schema(description = "是否主记录：1 是 / 0 否")
    private Boolean isPrimary;
    @Schema(description = "bindStatus")
    private String bindStatus;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
}
