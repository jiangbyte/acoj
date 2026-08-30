package github.jiangbyte.io.iam.modules.client.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 客户端资源模块实体，对应表 sys_client_module。
 *
 * Author: Charlie
 */
@Schema(description = "客户端资源模块实体，对应表 sys_client_module。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_client_module", autoResultMap = true)
public class SysClientModule extends BaseEntity {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "适用账户体系：ADMIN/PORTAL")
    private String accountType;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "颜色值")
    private String color;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "模块状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "客户端模块描述")
    private String description;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;
}
