package io.charlie.web.oj.modular.data.testcase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;

import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 题目测试用例表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_test_case")
@Schema(name = "DataTestCase", description = "题目测试用例表")
public class DataTestCase extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "用例标识")
    private String caseSign;

    @Schema(description = "输入数据")
    private String inputData;

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

    @Schema(description = "是否样例")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isSample;

    @Schema(description = "分值")
    private Integer score;
}
