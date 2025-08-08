package io.charlie.app.core.modular.problem.judge.service;

import io.charlie.app.core.modular.judge.dto.JudgeResultDto;
import io.charlie.app.core.modular.judge.dto.JudgeSubmitDto;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitDto;
import io.charlie.app.core.modular.problem.judge.dto.ProJudgeSubmitResultDto;

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