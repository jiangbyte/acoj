package github.jiangbyte.io.sys.modules.config.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 系统配置查询结果（含解密后展示值）。
 *
 * Author: Charlie
 */
@Schema(description = "系统配置查询结果（含解密后展示值）。")
@Data
public class SysConfigResult {
    @Schema(description = "主键ID")

    private String id;
    @Schema(description = "配置项唯一键")
    private String configKey;
    @Schema(description = "配置项值（按 value_type 解析）")
    private String configValue;
    @Schema(description = "配置分类/分组")
    private String category;
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "同分类下排序码")
    private Integer sortCode;
    @Schema(description = "值类型：STRING/JSON/BOOL/NUMBER")
    private String valueType;
    @Schema(description = "配置项展示名称")
    private String label;
    @Schema(description = "作用域账户类型：GLOBAL/ADMIN/PORTAL")
    private String scope;
    @Schema(description = "业务场景编码")
    private String scene;
    @Schema(description = "是否内置配置：1 内置不可删 / 0 可维护")
    private Boolean isBuiltin;
    @Schema(description = "扩展配置（JSON）")
    private Map<String, Object> extJson;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
}
