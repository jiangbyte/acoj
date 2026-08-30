package github.jiangbyte.io.sys.modules.config.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 系统配置实体，对应表 sys_config。
 *
 * Author: Charlie
 */
@Schema(description = "系统配置实体，对应表 sys_config。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_config", autoResultMap = true)
public class SysConfig extends BaseEntity {
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
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展配置（JSON）")
    private Map<String, Object> extJson;
}
