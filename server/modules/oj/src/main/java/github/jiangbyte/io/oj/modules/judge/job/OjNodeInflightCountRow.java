package github.jiangbyte.io.oj.modules.judge.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * JUDGING 按节点聚合行。
 *
 * Author: Charlie
 */
@Schema(description = "JUDGING 按节点聚合")
@Data
public class OjNodeInflightCountRow {
    @Schema(description = "节点 ID")
    private String nodeId;
    @Schema(description = "JUDGING 数量")
    private Long cnt;
}
