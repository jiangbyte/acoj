package github.jiangbyte.io.oj.modules.tag.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 门户题库标签筛选项聚合结果。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户题库标签选项聚合")
@Data
public class OjPortalTagOptionsResult {
    @Schema(description = "启用中标签及已发布题数")
    private List<OjTagOptionItem> tags = new ArrayList<>();
    @Schema(description = "已发布题目总数")
    private Long publishedCount = 0L;
    @Schema(description = "当前用户已通过题数；未登录为 null")
    private Long acceptedCount;
}
