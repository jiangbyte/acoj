package github.jiangbyte.io.sys.modules.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "系统配置分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigPageParam extends PageQuery {
    @Schema(description = "配置项唯一键")

    private String configKey;
    @Schema(description = "配置分类/分组")
    private String category;
}
