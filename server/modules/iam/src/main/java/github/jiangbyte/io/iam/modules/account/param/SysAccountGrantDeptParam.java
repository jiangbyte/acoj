package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号授权部门入参：账号 id + 部门授予列表。
 *
 * Author: Charlie
 */
@Schema(description = "账号授权部门入参：账号 id + 部门授予列表。")
@Data
public class SysAccountGrantDeptParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "grantInfoList")
    private List<SysDeptGrantResult> grantInfoList = new ArrayList<>();
}
