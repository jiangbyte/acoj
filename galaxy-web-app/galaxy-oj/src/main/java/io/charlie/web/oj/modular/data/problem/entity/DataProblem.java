package io.charlie.web.oj.modular.data.problem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.charlie.web.oj.modular.sys.category.entity.SysCategory;
import io.charlie.web.oj.modular.task.judge.dto.TestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题目表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "data_problem", autoResultMap = true)
@Schema(name = "DataProblem", description = "题目表")
public class DataProblem extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "展示ID")
    private String displayId;

    @Schema(description = "分类")
    @Trans(type = TransType.SIMPLE, target = SysCategory.class, fields = "name", refs = "categoryName")
    private String categoryId;

    @TableField(exist = false)
    private String categoryName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "链接")
    private String url;

    @Schema(description = "时间限制")
    private BigDecimal maxTime;

    @Schema(description = "内存限制")
    private BigDecimal maxMemory;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "用例")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<TestCase> testCase;

    @Schema(description = "开放语言")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allowedLanguages;

    @Schema(description = "难度")
    @Trans(type = TransType.DICTIONARY, key = "PROBLEM_DIFFICULTY")
    private Integer difficulty;

    @Schema(description = "阈值")
    private BigDecimal threshold;

    @Schema(description = "使用模板")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean useTemplate;

    @Schema(description = "模板代码")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CodeTemplate> codeTemplate;

    @Schema(description = "是否公开")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isPublic;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisible;

    @Schema(description = "是否使用AI")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean useAi;

    @Schema(description = "解决")
    private Long solved;

    @Schema(description = "排行")
    @TableField(exist = false)
    private Long rank;

    @Schema(description = "标签ID")
    @TableField(exist = false)
    private List<String> tagIds;

    @Schema(description = "标签名称")
    @TableField(exist = false)
    private List<String> tagNames;

    @Schema(description = "当前用户是否已解决")
    @TableField(exist = false)
    private Boolean currentUserSolved;

    // =====================
    @Schema(description = "排行榜分数")
    @TableField(exist = false)
    private Double score;

    @Schema(description = "通过率")
    @TableField(exist = false)
    private Double acceptance;

    // 提交人数
    @Schema(description = "提交人数")
    @TableField(exist = false)
    private Long submitUserCount;

    // 参与人数
    @Schema(description = "参与人数")
    @TableField(exist = false)
    private Long participantUserCount;

}
