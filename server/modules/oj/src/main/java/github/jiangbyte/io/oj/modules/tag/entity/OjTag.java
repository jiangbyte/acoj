package github.jiangbyte.io.oj.modules.tag.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OJ 题目标签实体，对应表 {@code oj_tag}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 题目标签")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oj_tag")
public class OjTag extends BaseEntity {
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "状态：ENABLED/DISABLED")
    private String status;
}
