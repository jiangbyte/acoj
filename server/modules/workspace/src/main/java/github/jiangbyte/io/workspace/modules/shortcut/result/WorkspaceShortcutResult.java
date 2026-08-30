package github.jiangbyte.io.workspace.modules.shortcut.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作台个人快捷应用项。
 *
 * Author: Charlie
 */
@Schema(description = "工作台个人快捷应用项。")
@Data
public class WorkspaceShortcutResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "resourceId")
    private String resourceId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "resourceType")
    private String resourceType;
    @Schema(description = "状态")
    private String status;
}
