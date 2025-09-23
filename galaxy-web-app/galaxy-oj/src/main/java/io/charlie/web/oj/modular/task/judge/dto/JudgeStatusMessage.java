package io.charlie.web.oj.modular.task.judge.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 判题状态消息
 */
@Data
public class JudgeStatusMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String status;

    private String message;
}
