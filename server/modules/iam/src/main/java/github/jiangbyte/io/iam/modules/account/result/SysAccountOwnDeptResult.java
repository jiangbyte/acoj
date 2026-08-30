package github.jiangbyte.io.iam.modules.account.result;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.iam.modules.dept.result.SysDeptGrantResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号已拥有部门授权结果（部门授予明细列表）。
 *
 * Author: Charlie
 */
@Schema(description = "账号已拥有部门授权结果（部门授予明细列表）。")
@Data
public class SysAccountOwnDeptResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "grantInfoList")
    private List<SysDeptGrantResult> grantInfoList = new ArrayList<>();
}
