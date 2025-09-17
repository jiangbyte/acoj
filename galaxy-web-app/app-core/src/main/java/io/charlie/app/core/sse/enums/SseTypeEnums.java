package io.charlie.app.core.sse.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 10/09/2025
 * @description SSE类型枚举
 */
@AllArgsConstructor
@Getter
public enum SseTypeEnums implements ILabelEnum<String> {
    // 心跳活跃
    HEARTBEAT("heartbeat", "心跳活跃", true, false),
    // 题目提交
    PROBLEM_SUBMIT("problem_submit", "题目提交", false, true),
    // 题集提交
    PROBLEM_SET_SUBMIT("problem_set_submit", "题集提交", false, true),
    // 题目报告生成
    PROBLEM_REPORT_GENERATE("problem_report_generate", "题目报告生成", true, false),
    // 题集报告生成
    PROBLEM_SET_REPORT_GENERATE("problem_set_report_generate", "题集报告生成", true, false),
    ;

    private final String value;
    private final String label;
    private final boolean requiresHeartbeat; // 是否需要心跳保活
    private final boolean requiresDataQuery; // 是否需要数据状态实时查询

    private static final Map<String, SseTypeEnums> VALUE_MAP = new ConcurrentHashMap<>();

    static {
        Arrays.stream(values()).forEach(type -> VALUE_MAP.put(type.value, type));
    }

    public static SseTypeEnums fromValue(String value) {
        return VALUE_MAP.get(value);
    }
}
