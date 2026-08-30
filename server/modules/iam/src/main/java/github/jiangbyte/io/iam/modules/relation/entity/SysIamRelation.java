package github.jiangbyte.io.iam.modules.relation.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * IAM 关系实体，对应表 sys_iam_relation。
 * 唯一键：(subject_type, subject_id, relation_type, target_type,
 * target_id, target_key, account_type)。
 *
 * Author: Charlie
 */
@Schema(description = "IAM 关系实体，对应表 sys_iam_relation。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_iam_relation", autoResultMap = true)
public class SysIamRelation extends BaseEntity {
    @Schema(description = "主体类型：ACCOUNT/DEPT/ROLE/GROUP/POSITION")
    private String subjectType;
    @Schema(description = "主体记录ID")
    private String subjectId;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "关系类型：MEMBER/GRANT/OWN 等")
    private String relationType;
    @Schema(description = "目标类型：RESOURCE/ROLE/DEPT/DATA_SCOPE 等")
    private String targetType;
    @Schema(description = "目标记录ID")
    private String targetId;
    @Schema(description = "目标业务标识（如权限 code）")
    private String targetKey;
    @Schema(description = "授权模式：DIRECT/INHERIT 等")
    private String grantMode;
    @Schema(description = "数据范围：ALL/DEPT/DEPT_AND_CHILD/CUSTOM/SELF")
    private String dataScope;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "自定义数据范围部门ID列表（JSON 数组）")
    private List<String> customScopeDeptIds;
    @Schema(description = "是否主关系/主岗位：1 是 / 0 否")
    private Boolean isPrimary;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "关系状态：ACTIVE/INACTIVE")
    private String status;
    @Schema(description = "IAM 关系说明")
    private String description;
    @Schema(description = "授权/变更原因")
    private String reason;
    @Schema(description = "关系失效时间（空表示永久）")
    private OffsetDateTime expiredAt;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;
}
