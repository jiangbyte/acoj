package io.charlie.web.oj.modular.data.submit.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交 编辑参数
*/
@Data
@Schema(name = "DataSubmit", description = "提交 编辑参数")
public class DataSubmitEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "是否是题集提交")
    private Boolean isSet;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "源代码")
    private String code;

    @Schema(description = "源代码长度")
    private Integer codeLength;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "最大耗时")
    private Integer maxTime;

    @Schema(description = "最大内存使用")
    private Integer maxMemory;

    @Schema(description = "执行结果消息")
    private String message;

    @Schema(description = "测试用例结果")
    private String testCase;

    @Schema(description = "执行状态")
    private String status;

    @Schema(description = "流程流转是否完成")
    private Boolean isFinish;

    @Schema(description = "相似度")
    private BigDecimal similarity;

    @Schema(description = "相似检测任务ID")
    private String taskId;

    @Schema(description = "报告ID")
    private String reportId;

    @Schema(description = "相似分级")
    private String similarityCategory;

}