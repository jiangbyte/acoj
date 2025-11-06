package io.charlie.web.oj.modular.data.contest.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛题目 编辑参数
*/
@Data
@Schema(name = "DataContestProblem", description = "竞赛题目 编辑参数")
public class DataContestProblemEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "竞赛ID")
    private String contestId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题目在竞赛中的编号(A,B,C...)")
    private String problemCode;

    @Schema(description = "显示ID")
    private String displayId;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目分数(OI模式)")
    private Integer score;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "提交次数")
    private Integer submitCount;

    @Schema(description = "通过次数")
    private Integer acceptCount;

}