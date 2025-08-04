package io.charlie.app.core.modular.problem.submit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 提交表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pro_submit")
@Schema(name = "ProSubmit", description = "提交表")
public class ProSubmit extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "源代码")
    private String code;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "最大耗时")
    private Integer maxTime;

    @Schema(description = "最大内存使用")
    private Integer maxMemory;

    @Schema(description = "执行结果消息")
    private String message;

    @Schema(description = "测试用例结果")
    private List<SubmitTestCase> testCases;

    @Schema(description = "执行状态")
    private String status;

    @Schema(description = "相似度")
    private BigDecimal similarity;

    @Schema(description = "相似检测任务ID")
    private String taskId;
}
