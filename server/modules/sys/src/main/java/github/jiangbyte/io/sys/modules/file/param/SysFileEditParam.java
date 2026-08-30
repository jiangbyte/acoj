package github.jiangbyte.io.sys.modules.file.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑文件元数据入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑文件元数据入参。")
@Data
public class SysFileEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "用户上传时的原始文件名")
    private String originalName;
}
