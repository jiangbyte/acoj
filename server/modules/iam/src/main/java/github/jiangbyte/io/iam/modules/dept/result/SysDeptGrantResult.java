package github.jiangbyte.io.iam.modules.dept.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门授予结果/明细：部门 id、是否主部门、排序等。
 *
 * Author: Charlie
 */
@Schema(description = "部门授予结果/明细：部门 id、是否主部门、排序等。")
@Data
public class SysDeptGrantResult {

    @NotBlank
    @Schema(description = "deptId")
    private String deptId;
    @Schema(description = "是否主记录：1 是 / 0 否")
    private Boolean isPrimary = false;
}
