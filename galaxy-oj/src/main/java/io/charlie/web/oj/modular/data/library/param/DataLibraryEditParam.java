package io.charlie.web.oj.modular.data.library.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库 编辑参数
*/
@Data
@Schema(name = "DataLibrary", description = "提交样本库 编辑参数")
public class DataLibraryEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "模块类型")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "提交时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date submitTime;

    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "源代码")
    private String code;

    @Schema(description = "源代码长度")
    private Integer codeLength;

    @Schema(description = "访问次数")
    private Integer accessCount;

    private List<Integer> codeToken;
    private List<String> codeTokenName;
    private List<String> codeTokenTexts;

}