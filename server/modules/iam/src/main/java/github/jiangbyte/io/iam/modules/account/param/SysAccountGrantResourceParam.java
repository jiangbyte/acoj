package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantResult;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号授权资源（管理端或客户端）入参：账号 id + 资源授予明细。
 *
 * Author: Charlie
 */
@Schema(description = "账号授权资源（管理端或客户端）入参：账号 id + 资源授予明细。")
@Data
public class SysAccountGrantResourceParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "grantInfoList")
    private List<SysResourceGrantResult> grantInfoList = new ArrayList<>();
}
