package github.jiangbyte.io.iam.modules.resource.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端资源树查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "管理端资源树查询入参。")
@Data
public class SysResourceTreeParam {
    @Schema(description = "所属模块ID")

    private String moduleId;
    @Schema(description = "模块所属客户端（展示）")
    private String moduleClient;
}
