package io.charlie.web.oj.modular.data.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;

import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_library")
@Schema(name = "DataLibrary", description = "提交样本库")
public class DataLibrary extends CommonEntity {
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

    @Schema(description = "题集ID")
    @Trans(type = TransType.SIMPLE, target = DataSet.class, fields = "title", ref = "setIdName")
    private String setId;

    @Schema(description = "题集名称")
    @TableField(exist = false)
    private String setIdName;

    @Schema(description = "是否是题集提交")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isSet;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "提交时间")
    private Date submitTime;

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

    @Schema(description = "访问次数")
    private Integer accessCount;
}
