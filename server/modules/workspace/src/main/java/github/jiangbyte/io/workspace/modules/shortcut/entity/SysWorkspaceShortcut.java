package github.jiangbyte.io.workspace.modules.shortcut.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作台个人快捷应用，对应表 sys_workspace_shortcut。
 *
 * Author: Charlie
 */
@Schema(description = "工作台个人快捷应用，对应表 sys_workspace_shortcut。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_workspace_shortcut")
public class SysWorkspaceShortcut extends BaseEntity {
    @Schema(description = "所属账号ID")
    private String accountId;
    @Schema(description = "快捷菜单资源ID（sys_resource）")
    private String resourceId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
}
