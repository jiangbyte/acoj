package io.charlie.web.oj.modular.data.contest.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 07/11/2025
 * @description TODO
 */
@Data
@Schema(name = "DataContestAuth", description = "竞赛认证 增加参数")
public class DataContestAuthParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "竞赛ID")
    private String contestId;

    private String password;
}
