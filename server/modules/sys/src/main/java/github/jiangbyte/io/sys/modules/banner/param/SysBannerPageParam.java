package github.jiangbyte.io.sys.modules.banner.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Banner 分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "Banner 分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysBannerPageParam extends PageQuery {
    @Schema(description = "targetAccountType")

    private String targetAccountType;
    @Schema(description = "Banner 分类（字典 BANNER_CATEGORY）")
    private String category;
    @Schema(description = "Banner 类型（字典 BANNER_TYPE）")
    private String type;
    @Schema(description = "展示位置（字典 BANNER_POSITION）")
    private String position;
    @Schema(description = "Banner 状态：ENABLED/DISABLED 等")
    private String status;
}
