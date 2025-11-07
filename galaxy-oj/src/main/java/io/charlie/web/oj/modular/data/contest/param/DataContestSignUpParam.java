package io.charlie.web.oj.modular.data.contest.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与 增加参数
*/
@Data
@Schema(name = "DataContestParticipant", description = "竞赛参与 增加参数")
public class DataContestSignUpParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "竞赛ID")
    private String contestId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "团队ID")
    private String teamId;

}