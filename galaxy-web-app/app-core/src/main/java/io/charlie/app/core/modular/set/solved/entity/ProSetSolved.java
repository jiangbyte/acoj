package io.charlie.app.core.modular.set.solved.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.set.set.entity.ProSet;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 用户题集解决记录表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pro_set_solved")
@Schema(name = "ProSetSolved", description = "用户题集解决记录表")
public class ProSetSolved extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = "nickname", ref = "userIdName")
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = ProProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "题集ID")
    @Trans(type = TransType.SIMPLE, target = ProSet.class, fields = "title", ref = "problemSetIdName")
    private String problemSetId;

    @Schema(description = "题集名称")
    @TableField(exist = false)
    private String problemSetIdName;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "是否解决")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO", ref = "solvedName")
    private Boolean solved;

    @Schema(description = "是否解决名称")
    @TableField(exist = false)
    private String solvedName;
}
