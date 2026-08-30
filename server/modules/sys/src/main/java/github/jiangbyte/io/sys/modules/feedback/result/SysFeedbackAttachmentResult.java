package github.jiangbyte.io.sys.modules.feedback.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈附件展示结果：对象名、文件元数据与可访问 URL。
 *
 * Author: Charlie
 */
@Schema(description = "反馈附件展示结果：对象名、文件元数据与可访问 URL。")
@Data
public class SysFeedbackAttachmentResult {
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
