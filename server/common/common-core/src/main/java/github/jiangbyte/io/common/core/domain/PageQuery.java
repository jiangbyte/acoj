package github.jiangbyte.io.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页查询入参：当前页与每页条数（带校验边界）。
 *
 * Author: Charlie
 */
@Schema(description = "通用分页查询入参：当前页与每页条数（带校验边界）。")
@Data
public class PageQuery {

    @Min(1)
    @Schema(description = "当前页码（从 1 开始）")
    private int current = 1;

    @Min(1)
    @Max(100)
    @Schema(description = "每页条数")
    private int size = 20;
}
