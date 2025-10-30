package io.charlie.web.oj.modular.data.submit.event;

import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;
import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description TODO
 */
@Data
public class ProblemSubmitEvent {
    private final DataSubmitExeParam dataSubmitExeParam;
    private final DataSubmit dataSubmit;
    private final Boolean isSet;
    private final String userId;

    public ProblemSubmitEvent(DataSubmitExeParam dataSubmitExeParam, DataSubmit dataSubmit, Boolean isSet, String userId) {
        this.dataSubmitExeParam = dataSubmitExeParam;
        this.dataSubmit = dataSubmit;
        this.isSet = isSet;
        this.userId = userId;
    }
}
