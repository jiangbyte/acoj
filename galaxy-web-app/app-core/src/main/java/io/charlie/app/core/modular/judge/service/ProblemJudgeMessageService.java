package io.charlie.app.core.modular.judge.service;

import io.charlie.app.core.modular.judge.dto.JudgeResultDto;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 提交服务接口
 */
public interface ProblemJudgeMessageService {
    /**
     * 发送判题请求消息
     */
    void sendJudgeRequest(JudgeSubmitDto judgeSubmitDto);
    
    /**
     * 处理判题结果消息
     */
    void handleJudgeResult(JudgeResultDto judgeResultDto);
}