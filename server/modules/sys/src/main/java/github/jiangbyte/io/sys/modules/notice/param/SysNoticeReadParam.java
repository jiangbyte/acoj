package github.jiangbyte.io.sys.modules.notice.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 标记消息已读的请求参数（消息 ID 列表）。
 *
 * Author: Charlie
 */
@Schema(description = "标记消息已读的请求参数（消息 ID 列表）。")
@Data
public class SysNoticeReadParam {

    @NotEmpty
    private List<@NotBlank @Size(max = 64) String> ids;
}
