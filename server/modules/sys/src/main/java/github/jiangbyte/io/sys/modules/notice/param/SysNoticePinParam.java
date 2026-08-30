package github.jiangbyte.io.sys.modules.notice.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 公告置顶请求参数（是否置顶与置顶截止时间）。
 *
 * Author: Charlie
 */
@Schema(description = "公告置顶请求参数（是否置顶与置顶截止时间）。")
@Data
public class SysNoticePinParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotNull
    @Schema(description = "是否置顶：1 置顶 / 0 不置顶")
    private Boolean isPinned;
    @Schema(description = "pinnedUntil")
    private OffsetDateTime pinnedUntil;
}
