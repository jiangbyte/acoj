package github.jiangbyte.io.oj.modules.stat.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * OJ 用户题目统计实体，对应表 {@code oj_user_problem_stat}。
 *
 * Author: Charlie
 */
@Schema(description = "OJ 用户题目统计")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_user_problem_stat", autoResultMap = true)
public class OjUserProblemStat extends BaseEntity {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "题目ID")
    private String problemId;
    @Schema(description = "状态：ATTEMPTED/ACCEPTED")
    private String status;
    @Schema(description = "提交次数")
    private Integer attemptCount;
    @Schema(description = "AC 次数")
    private Integer acceptedCount;
    @Schema(description = "首次 AC 时间")
    private OffsetDateTime firstAcceptedAt;
    @Schema(description = "最近提交时间")
    private OffsetDateTime lastSubmitAt;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
}
