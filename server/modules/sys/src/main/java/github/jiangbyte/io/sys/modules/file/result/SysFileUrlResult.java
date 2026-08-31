package github.jiangbyte.io.sys.modules.file.result;

import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class SysFileUrlResult {
    @Schema(description = "objectName")
    private String objectName;
    @Schema(description = "url")
    private String url;
    @Schema(description = "用户上传时的原始文件名")
    private String originalName;
    @Schema(description = "文件大小（字节）")
    private Long size;
    @Schema(description = "MIME 类型")
    private String contentType;

    public static SysFileUrlResult of(String objectKey, String url, SysFile file) {
        SysFileUrlResult result = new SysFileUrlResult();
        result.setObjectName(objectKey);
        result.setUrl(url);
        if (file != null) {
            result.setOriginalName(file.getOriginalName());
            result.setSize(file.getSize());
            result.setContentType(file.getContentType());
        }
        return result;
    }
}
