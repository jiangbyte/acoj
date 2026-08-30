package github.jiangbyte.io.sys.modules.notice.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告/通知分页查询参数（标题、状态、类型及分页字段）。
 *
 * Author: Charlie
 */
@Schema(description = "公告/通知分页查询参数（标题、状态、类型及分页字段）。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticePageParam extends PageQuery {
    @Schema(description = "标题")

    private String title;
    @Schema(description = "发布状态：DRAFT/PUBLISHED/REVOKED 等")
    private String status;
    @Schema(description = "消息种类：NOTIFICATION（通知）/ ANNOUNCEMENT（公告）")
    private String kind;
}
