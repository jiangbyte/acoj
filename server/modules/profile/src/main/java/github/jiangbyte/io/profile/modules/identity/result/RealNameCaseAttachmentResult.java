package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 实名工单附件展示结果。
 *
 * Author: Charlie
 */
@Schema(description = "实名工单附件展示结果。")
@Data
public class RealNameCaseAttachmentResult {
    @Schema(description = "objectName")
    private String objectName;
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "originalName")
    private String originalName;
    @Schema(description = "contentType")
    private String contentType;
    @Schema(description = "每页条数")
    private Long size;
    @Schema(description = "url")
    private String url;
}
