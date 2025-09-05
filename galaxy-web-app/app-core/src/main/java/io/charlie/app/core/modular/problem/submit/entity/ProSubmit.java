package io.charlie.app.core.modular.problem.submit.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
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
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 提交表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "pro_submit", autoResultMap = true)
@Schema(name = "ProSubmit", description = "提交表")
public class ProSubmit extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"userIdName", "userAvatar"})
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String userAvatar;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = ProProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "编程语言")
    @Trans(type = TransType.DICTIONARY, key = "ALLOW_LANGUAGE", ref = "languageName")
    private String language;

    @Schema(description = "语言名称")
    @TableField(exist = false)
    private String languageName;

    @Schema(description = "源代码")
    private String code;

    @Schema(description = "源代码长度")
    private Integer codeLength;

    @Schema(description = "执行类型")
    @Trans(type = TransType.DICTIONARY, key = "SUBMIT_TYPE", ref = "submitTypeName")
    private Boolean submitType;

    @Schema(description = "执行类型名称")
    @TableField(exist = false)
    private String submitTypeName;

    @Schema(description = "最大耗时")
    private Integer maxTime;

    @Schema(description = "最大内存使用")
    private Integer maxMemory;

    @Schema(description = "执行结果消息")
    private String message;

    @Schema(description = "测试用例结果")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SubmitTestCase> testCase;

    @Schema(description = "执行状态")
    @Trans(type = TransType.DICTIONARY, key = "JUDGE_STATUS", ref = "statusName")
    private String status;

    @Schema(description = "是否完成")
    private Boolean isFinish;

    @Schema(description = "执行状态名称")
    @TableField(exist = false)
    private String statusName;

    @Schema(description = "相似度")
    private BigDecimal similarity;

    @Schema(description = "相似检测任务ID")
    private String taskId;

    @Schema(description = "相似度行为")
    @Trans(type = TransType.DICTIONARY, key = "CLONE_LEVEL", ref = "similarityBehaviorName")
    private String similarityBehavior;

    @Schema(description = "相似度行为名称")
    @TableField(exist = false)
    private String similarityBehaviorName;

}
