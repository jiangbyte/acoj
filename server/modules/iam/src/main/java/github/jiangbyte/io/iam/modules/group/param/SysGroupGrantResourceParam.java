package github.jiangbyte.io.iam.modules.group.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceGrantResult;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户组授权资源（管理端或客户端）入参。
 *
 * Author: Charlie
 */
@Schema(description = "用户组授权资源（管理端或客户端）入参。")
@Data
public class SysGroupGrantResourceParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType = "ADMIN";
    @Schema(description = "grantInfoList")
    private List<SysResourceGrantResult> grantInfoList = new ArrayList<>();
}
