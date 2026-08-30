package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源授权树中的模块选项（含菜单子节点）。
 *
 * Author: Charlie
 */
@Schema(description = "资源授权树中的模块选项（含菜单子节点）。")
@Data
public class SysResourceGrantModuleOptionResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "menu")
    private List<SysResourceGrantMenuOptionResult> menu = new ArrayList<>();
}
