package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源授权树中的菜单选项节点。
 *
 * Author: Charlie
 */
@Schema(description = "资源授权树中的菜单选项节点。")
@Data
public class SysResourceGrantMenuOptionResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "所属模块ID")
    private String moduleId;
    @Schema(description = "父级ID")
    private String parentId;
    @Schema(description = "父级名称（展示）")
    private String parentIdName;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "button")
    private List<SysResourcePermissionOptionResult> button = new ArrayList<>();
}
