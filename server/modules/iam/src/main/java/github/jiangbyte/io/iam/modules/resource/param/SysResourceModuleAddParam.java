package github.jiangbyte.io.iam.modules.resource.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 创建管理端资源模块入参。
 *
 * Author: Charlie
 */
@Schema(description = "创建管理端资源模块入参。")
@Data
public class SysResourceModuleAddParam {

    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "编码")
    private String code;
    @Schema(description = "所属客户端：admin/portal 等")
    private String client;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "模块状态：ENABLED/DISABLED")
    private String status = "ENABLED";
    @Schema(description = "资源模块描述")
    private String description;
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}
