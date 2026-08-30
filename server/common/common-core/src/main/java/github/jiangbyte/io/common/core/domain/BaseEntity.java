package github.jiangbyte.io.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.dromara.core.trans.vo.TransPojo;

import java.time.OffsetDateTime;

/**
 * 默认业务实体基类（单继承）。
 * 提供通用审计字段与 MyBatis-Plus 雪花 id，无软删除标记。
 * 表缺少完整审计列时，可在 {@code @TableName(excludeProperty = ...)} 中排除多余字段。
 *
 * Author: Charlie
 */
@Schema(description = "默认业务实体基类（单继承）。")
@Data
public abstract class BaseEntity implements TransPojo {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人（账户ID）")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;

}
