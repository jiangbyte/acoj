package github.jiangbyte.io.sys.modules.codegen.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成预览结果（多文件）。
 *
 * Author: Charlie
 */
@Schema(description = "代码生成预览结果（多文件）。")
@Data
public class SysCodegenPreviewResult {
    @Schema(description = "files")
    private List<SysCodegenPreviewFileResult> files = new ArrayList<>();
}
