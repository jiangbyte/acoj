package io.charlie.web.modular.data.contest.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与
*/
@Data
@Schema(name = "DataContestParticipant", description = "竞赛参与 ID参数")
public class DataContestParticipantIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}