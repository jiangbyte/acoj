package github.jiangbyte.io.oj.modules.problemlanguagelimit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * OJ 题目 × 语言资源限额，对应表 {@code oj_problem_language_limit}。
 * <p>
 * Author: Charlie
 */
@Schema(description = "OJ 题目语言限额")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oj_problem_language_limit", autoResultMap = true)
public class OjProblemLanguageLimit extends BaseEntity {
    @Schema(description = "所属题目ID")
    private String problemId;
    @Schema(description = "语言 key")
    private String language;
    @Schema(description = "CPU 时限毫秒")
    private Integer timeLimitMs;
    @Schema(description = "内存限额字节")
    private Long memoryLimitBytes;
    @Schema(description = "栈限额字节，空则用沙箱默认")
    private Long stackLimitBytes;
    @Schema(description = "输出限额字节，空则用沙箱默认")
    private Long outputLimitBytes;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息")
    private Map<String, Object> extra;
}
