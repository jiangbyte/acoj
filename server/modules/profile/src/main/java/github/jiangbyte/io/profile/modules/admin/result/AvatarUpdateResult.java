package github.jiangbyte.io.profile.modules.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端头像上传结果：可访问 URL、文件 ID 与对象存储路径。
 *
 * Author: Charlie
 */
@Schema(description = "管理端头像上传结果：可访问 URL、文件 ID 与对象存储路径。")
@Data
public class AvatarUpdateResult {
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "fileId")
    private String fileId;
    @Schema(description = "objectName")
    private String objectName;
    @Schema(description = "url")
    private String url;
}
