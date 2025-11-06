package io.charlie.web.oj.modular.data.contest.entity;

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
* @date 2025-11-06
* @description 竞赛题目表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_contest_problem")
@Schema(name = "DataContestProblem", description = "竞赛题目表")
public class DataContestProblem extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private String id;

    @Schema(description = "竞赛ID")
    @Trans(type = TransType.SIMPLE, target = DataContest.class, fields = {"title"}, refs = {"contestTitle"})
    private String contestId;

    @Schema(description = "")
    @TableField(exist = false)
    private String contestTitle;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "题目在竞赛中的编号(A,B,C...)")
    private String problemCode;

    @Schema(description = "显示ID")
    private String displayId;

    @Schema(description = "题目分数(OI模式)")
    private Integer score;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "提交次数")
    private Integer submitCount;

    @Schema(description = "通过次数")
    private Integer acceptCount;
}
