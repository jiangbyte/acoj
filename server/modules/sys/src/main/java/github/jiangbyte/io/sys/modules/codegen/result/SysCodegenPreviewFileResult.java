package github.jiangbyte.io.sys.modules.codegen.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成预览单文件结果。
 *
 * Author: Charlie
 */
@Schema(description = "代码生成预览单文件结果。")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysCodegenPreviewFileResult {
    @Schema(description = "路径")
    private String path;
    @Schema(description = "language")
    private String language;
    @Schema(description = "内容")
    private String content;
}
