package io.charlie.web.modular.llm.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Supplier;

/**
 * 记录 LLM 工具调用轨迹。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmToolTraceRecorder {

    private static final int RESULT_MAX = 4000;

    private final SysConversationToolTraceMapper toolTraceMapper;

    /**
     * 包装工具调用并落库轨迹。
     */
    public String trace(String toolName, Object args, Supplier<String> action) {
        long start = System.currentTimeMillis();
        String result = null;
        String error = null;
        String status = "SUCCESS";
        try {
            result = action.get();
            return result;
        } catch (Exception e) {
            status = "ERROR";
            error = e.getMessage();
            throw e;
        } finally {
            persist(toolName, args, result, status, error, System.currentTimeMillis() - start);
        }
    }

    private void persist(String toolName, Object args, String result, String status, String error, long durationMs) {
        try {
            LlmToolTraceContext.Holder holder = LlmToolTraceContext.get();
            SysConversationToolTrace trace = new SysConversationToolTrace();
            trace.setId(IdUtil.getSnowflakeNextIdStr());
            if (holder != null) {
                trace.setConversationId(holder.conversationId);
                trace.setUserId(holder.userId);
                trace.setProblemId(holder.problemId);
            }
            trace.setToolName(toolName);
            trace.setToolArgs(args == null ? null : JSONUtil.toJsonStr(args));
            if (result != null) {
                trace.setToolResult(result.length() > RESULT_MAX ? result.substring(0, RESULT_MAX) : result);
            }
            trace.setDurationMs((int) durationMs);
            trace.setStatus(status);
            trace.setErrorMessage(error);
            Date now = new Date();
            trace.setCreateTime(now);
            trace.setUpdateTime(now);
            toolTraceMapper.insert(trace);
        } catch (Exception e) {
            log.warn("保存工具轨迹失败 tool={}", toolName, e);
        }
    }
}
