package io.charlie.app.core.modular.analyse.entity;

import lombok.Data;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 26/07/2025
 * @description 一周提交趋势
 */
@Data
public class SubmitTrend {
    private String data;

    private Long submitCount;

    private Long passCount;
}
