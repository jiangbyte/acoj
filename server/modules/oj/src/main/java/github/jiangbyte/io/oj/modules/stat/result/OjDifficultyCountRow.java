package github.jiangbyte.io.oj.modules.stat.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 难度聚合行。
 * <p>
 * Author: Charlie
 */
@Schema(description = "难度聚合行")
@Data
public class OjDifficultyCountRow {
    @Schema(description = "难度")
    private String difficulty;
    @Schema(description = "数量")
    private Long count;
}
