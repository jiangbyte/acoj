package io.charlie.app.core.modular.problem.ranking.param;

import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 27/08/2025
 * @description 活跃用户
 */
@Data
public class ActiveUser {
    private String userId;
    private String nickname;
    private String avatar;
    private String score;
}
