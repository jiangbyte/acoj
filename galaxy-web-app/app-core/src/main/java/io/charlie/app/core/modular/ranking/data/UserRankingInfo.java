package io.charlie.app.core.modular.ranking.data;

import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserRankingInfo extends RankingInfo {
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;
}
