package io.charlie.app.core.modular.problem.submit.param;

import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 23/08/2025
 * @description 提交状态统计
 */
@Data
public class StatusCount {
    private String status;
    private String statusName;
    private String count;
}
