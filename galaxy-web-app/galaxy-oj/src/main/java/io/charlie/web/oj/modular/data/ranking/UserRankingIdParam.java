package io.charlie.web.oj.modular.data.ranking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description TODO
 */
@Data
public class UserRankingIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

}
