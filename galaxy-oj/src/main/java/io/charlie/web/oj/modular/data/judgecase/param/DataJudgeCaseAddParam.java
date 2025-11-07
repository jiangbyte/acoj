package io.charlie.web.oj.modular.data.judgecase.param;

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
* @date 2025-10-26
* @description 判题结果用例 增加参数
*/
@Data
@Schema(name = "DataJudgeCase", description = "判题结果用例 增加参数")
public class DataJudgeCaseAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "提交ID")
    private String submitId;
    @Schema(description = "用例标识")
    private String caseSign;
    @Schema(description = "输入数据")
    private String inputData;

    @Schema(description = "输出数据")
    private String outputData;

    @Schema(description = "期望输出")
    private String expectedOutput;

    @Schema(description = "输入文件路径")
    private String inputFilePath;

    @Schema(description = "输入文件大小")
    private Long inputFileSize;

    @Schema(description = "输出文件路径")
    private String outputFilePath;

    @Schema(description = "输出文件大小")
    private Long outputFileSize;

    @Schema(description = "最大耗时")
    private BigDecimal maxTime;

    @Schema(description = "最大内存使用")
    private BigDecimal maxMemory;

    @Schema(description = "是否样例判题")
    private Boolean isSample;

    @Schema(description = "分值")
    private BigDecimal score;

    @Schema(description = "执行状态")
    private String status;

    @Schema(description = "信息")
    private String message;

    @Schema(description = "退出码")
    private Integer exitCode;

}