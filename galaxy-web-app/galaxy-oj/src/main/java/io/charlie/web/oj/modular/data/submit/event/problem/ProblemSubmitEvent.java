package io.charlie.web.oj.modular.data.submit.event.problem;

import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;
import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description TODO
 */
public record ProblemSubmitEvent(DataSubmitExeParam dataSubmitExeParam, DataSubmit dataSubmit) {
}
