package io.charlie.web.oj.modular.data.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛认证表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_contest_auth")
@Schema(name = "DataContestAuth", description = "竞赛认证表")
public class DataContestAuth extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private String id;

    @Schema(description = "竞赛ID")
    private String contestId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "是否已经认证")
    private Boolean isAuth;
}
