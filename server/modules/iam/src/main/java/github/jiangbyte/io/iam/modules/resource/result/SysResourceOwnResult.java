package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 主体已拥有资源授权结果（授予明细列表）。
 *
 * Author: Charlie
 */
@Schema(description = "主体已拥有资源授权结果（授予明细列表）。")
@Data
public class SysResourceOwnResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "modules")
    private List<SysResourceGrantModuleOptionResult> modules = new ArrayList<>();
    @Schema(description = "grantInfoList")
    private List<SysResourceGrantResult> grantInfoList = new ArrayList<>();
}
