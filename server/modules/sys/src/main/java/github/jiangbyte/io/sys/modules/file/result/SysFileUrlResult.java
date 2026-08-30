package github.jiangbyte.io.sys.modules.file.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件访问 URL 结果。
 *
 * Author: Charlie
 */
@Schema(description = "文件访问 URL 结果。")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysFileUrlResult {
    @Schema(description = "objectName")

    private String objectName;
    @Schema(description = "url")
    private String url;
}
