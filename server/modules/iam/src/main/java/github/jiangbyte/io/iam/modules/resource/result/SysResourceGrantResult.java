package github.jiangbyte.io.iam.modules.resource.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 主体资源授予明细：资源 id、数据范围与自定义部门等。
 *
 * Author: Charlie
 */
@Schema(description = "主体资源授予明细：资源 id、数据范围与自定义部门等。")
@Data
public class SysResourceGrantResult {

    @NotBlank
    @Schema(description = "resourceId")
    private String resourceId;
    @Schema(description = "permissionKeys")
    private List<String> permissionKeys = new ArrayList<>();
}
