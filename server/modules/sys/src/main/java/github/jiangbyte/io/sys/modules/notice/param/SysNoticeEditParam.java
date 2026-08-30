package github.jiangbyte.io.sys.modules.notice.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑公告/通知的请求参数（在新增字段基础上携带主键 ID）。
 *
 * Author: Charlie
 */
@Schema(description = "编辑公告/通知的请求参数（在新增字段基础上携带主键 ID）。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeEditParam extends SysNoticeAddParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
}
