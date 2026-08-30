package github.jiangbyte.io.biz.modules.cg_test_activity.entity;

/**
 * Activity实体，对应表 {@code cg_test_activity}。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "代码生成测试-活动")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cg_test_activity", autoResultMap = true)
public class CgTestActivity extends BaseEntity {
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "封面地址")
    private String coverUrl;
    @Schema(description = "描述说明")
    private String description;
    @Schema(description = "开始时间")
    private OffsetDateTime startAt;
    @Schema(description = "结束时间")
    private OffsetDateTime endAt;
    @Schema(description = "最大参与人数")
    private Integer maxParticipants;
    @Schema(description = "报名费用")
    private BigDecimal price;
    @Schema(description = "是否公开：1 公开 / 0 不公开")
    private Boolean isPublic;
    @Schema(description = "是否需要审批：1 需要 / 0 不需要")
    private Boolean needApproval;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "规则配置（JSON）")
    private java.util.Map<String, Object> ruleConfig;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
    @Schema(description = "所属部门ID（数据范围）")
    private String ownerDeptId;
}
