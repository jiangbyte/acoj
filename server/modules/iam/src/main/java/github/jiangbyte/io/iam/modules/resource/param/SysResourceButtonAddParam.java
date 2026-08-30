package github.jiangbyte.io.iam.modules.resource.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建管理端按钮资源入参。
 *
 * Author: Charlie
 */
@Schema(description = "创建管理端按钮资源入参。")
@Data
public class SysResourceButtonAddParam {

    @NotBlank
    @Schema(description = "父级ID")
    private String parentId;
    @NotBlank
    @Schema(description = "编码")
    private String code;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "权限键")
    private String permissionKey;
    @Schema(description = "dataScope")
    private String dataScope = "ALL";
    @Schema(description = "自定义数据范围部门ID列表（JSON 数组）")
    private List<String> customScopeDeptIds = new ArrayList<>();
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort = 99;
    @Schema(description = "状态")
    private String status = "ENABLED";
    @Schema(description = "描述说明")
    private String description;
}
