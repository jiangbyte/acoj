package io.charlie.web.oj.modular.data.submit.handle.set;

import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description TODO
 */
public record SetSubmitMessage(DataSubmitExeParam dataSubmitExeParam, DataSubmit dataSubmit) {
}
