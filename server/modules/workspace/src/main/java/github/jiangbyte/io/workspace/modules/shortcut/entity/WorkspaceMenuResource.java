package github.jiangbyte.io.workspace.modules.shortcut.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作台菜单资源投影，对应表 sys_resource。
 *
 * Author: Charlie
 */
@Schema(description = "工作台菜单资源投影，对应表 sys_resource。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource")
public class WorkspaceMenuResource extends BaseEntity {
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "资源类型：MENU/BUTTON/API 等")
    private String resourceType;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "资源状态：ENABLED/DISABLED")
    private String status;
}
