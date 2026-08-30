package github.jiangbyte.io.biz.modules.cg_test_activity.param;

/**
 * 编辑Activity入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CgTestActivity编辑入参")
@Data
public class CgTestActivityEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "编码")
    private String code;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "分类")
    private String category;
    @NotBlank
    @Schema(description = "类型")
    private String type;
    @NotBlank
    @Schema(description = "状态")
    private String status;
    @NotBlank
    @Schema(description = "封面地址")
    private String coverUrl;
    @NotBlank
    @Schema(description = "描述说明")
    private String description;
    @Schema(description = "开始时间")
    private OffsetDateTime startAt;
    @Schema(description = "结束时间")
    private OffsetDateTime endAt;
    @NotNull
    @Schema(description = "最大参与人数")
    private Integer maxParticipants;
    @NotNull
    @Schema(description = "报名费用")
    private BigDecimal price;
    @NotNull
    @Schema(description = "是否公开：1 公开 / 0 不公开")
    private Boolean isPublic;
    @NotNull
    @Schema(description = "是否需要审批：1 需要 / 0 不需要")
    private Boolean needApproval;
    @Schema(description = "规则配置（JSON）")
    private java.util.Map<String, Object> ruleConfig;
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}
