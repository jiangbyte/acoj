package io.charlie.web.modular.llm.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import io.charlie.web.modular.sys.conversation.mapper.SysConversationToolTraceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
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
     * 包装工具调用并落库轨迹；优先从 ToolContext 取会话信息（跨线程可靠）。
     */
    public String trace(String toolName, Object args, ToolContext toolContext, Supplier<String> action) {
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
            persist(toolName, args, result, status, error, System.currentTimeMillis() - start, toolContext);
        }
    }

    private void persist(String toolName, Object args, String result, String status, String error,
                         long durationMs, ToolContext toolContext) {
        try {
            // 1. 优先 ToolContext（流式工具调用常在其他线程，ThreadLocal 会丢）
            // 2. 回退 ThreadLocal
            Ctx ctx = resolveCtx(toolContext);
            SysConversationToolTrace trace = new SysConversationToolTrace();
            trace.setId(IdUtil.getSnowflakeNextIdStr());
            if (ctx != null) {
                // 空串视为未绑定，避免写入 "" 导致精确查询永远 miss
                trace.setConversationId(StrUtil.blankToDefault(ctx.conversationId, null));
                trace.setUserId(StrUtil.blankToDefault(ctx.userId, null));
                trace.setProblemId(StrUtil.blankToDefault(ctx.problemId, null));
            }
            trace.setToolName(toolName);
            trace.setToolArgs(args == null ? null : JSONUtil.toJsonStr(args));
            if (result != null) {
                trace.setToolResult(result.length() > RESULT_MAX ? result.substring(0, RESULT_MAX) : result);
            }
            trace.setDurationMs((int) durationMs);
            trace.setStatus(status);
            trace.setErrorMessage(error);
            // 显式未删除，避免逻辑删除字段为 null 导致分页查不到
            trace.setDeleted(false);
            Date now = new Date();
            trace.setCreateTime(now);
            trace.setUpdateTime(now);
            toolTraceMapper.insert(trace);
            if (StrUtil.isBlank(trace.getConversationId())) {
                log.warn("工具轨迹未绑定 conversationId tool={} toolContextEmpty={} threadLocalEmpty={}",
                        toolName,
                        toolContext == null || toolContext.getContext() == null || toolContext.getContext().isEmpty(),
                        LlmToolTraceContext.get() == null);
            }
        } catch (Exception e) {
            log.warn("保存工具轨迹失败 tool={}", toolName, e);
        }
    }

    private Ctx resolveCtx(ToolContext toolContext) {
        if (toolContext != null && toolContext.getContext() != null && !toolContext.getContext().isEmpty()) {
            Map<String, Object> map = toolContext.getContext();
            Ctx ctx = new Ctx();
            ctx.conversationId = asStr(map.get("conversationId"));
            ctx.userId = asStr(map.get("userId"));
            ctx.problemId = asStr(map.get("problemId"));
            if (StrUtil.isNotBlank(ctx.conversationId)) {
                return ctx;
            }
        }
        LlmToolTraceContext.Holder holder = LlmToolTraceContext.get();
        if (holder == null) {
            return null;
        }
        Ctx ctx = new Ctx();
        ctx.conversationId = holder.conversationId;
        ctx.userId = holder.userId;
        ctx.problemId = holder.problemId;
        return ctx;
    }

    private static String asStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static final class Ctx {
        private String conversationId;
        private String userId;
        private String problemId;
    }
}
