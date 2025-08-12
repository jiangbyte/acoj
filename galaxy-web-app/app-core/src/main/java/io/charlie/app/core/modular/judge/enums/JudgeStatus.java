package io.charlie.app.core.modular.judge.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JudgeStatus implements ILabelEnum<String> {
    // 基本状态
    PENDING("PENDING", "等待判题"),
    JUDGING("JUDGING", "判题中"),
    COMPILING("COMPILING", "编译中"),
    COMPILED_OK("COMPILED_OK", "编译成功"),
    RUNNING("RUNNING", "运行中"),

    // 结果状态
    ACCEPTED("ACCEPTED", "答案正确"),
    WRONG_ANSWER("WRONG_ANSWER", "答案错误"),
    TIME_LIMIT_EXCEEDED("TIME_LIMIT_EXCEEDED", "时间超出限制"),
    MEMORY_LIMIT_EXCEEDED("MEMORY_LIMIT_EXCEEDED", "内存超出限制"),
    RUNTIME_ERROR("RUNTIME_ERROR", "运行时错误"),
    COMPILATION_ERROR("COMPILATION_ERROR", "编译错误"),
    PRESENTATION_ERROR("PRESENTATION_ERROR", "格式错误"),
    OUTPUT_LIMIT_EXCEEDED("OUTPUT_LIMIT_EXCEEDED", "输出超出限制"),

    // 特殊状态
    PARTIAL_ACCEPTED("PARTIAL_ACCEPTED", "部分正确"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统错误"),
    RESTRICTED_FUNCTION("RESTRICTED_FUNCTION", "使用了限制函数"),
    DANGEROUS_OPERATION("DANGEROUS_OPERATION", "危险操作"),
    QUEUING("QUEUING", "排队中"),
    HIDDEN("HIDDEN", "隐藏"),
    SKIPPED("SKIPPED", "跳过"),
    FROZEN("FROZEN", "冻结"),

    // 竞赛特有状态
    FIRST_BLOOD("FIRST_BLOOD", "第一个通过该题目"),
    HACKED("HACKED", "被成功hack"),
    PENDING_REJUDGE("PENDING_REJUDGE", "等待重新判题"),
    REJUDGING("REJUDGING", "重新判题中"),

    // 其他状态
    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误"),
    VALIDATOR_ERROR("VALIDATOR_ERROR", "验证器错误"),
    CHECKER_ERROR("CHECKER_ERROR", "检查器错误"),
    IDLENESS_LIMIT_EXCEEDED("IDLENESS_LIMIT_EXCEEDED", "空闲时间超出限制"),
    SECURITY_VIOLATION("SECURITY_VIOLATION", "安全违规"),
    IGNORED("IGNORED", "被忽略"),

    // 代码相似状态
    SIMILARITY_SUSPICIOUS("SIMILARITY_SUSPICIOUS", "可疑提交"),
    SIMILARITY_ACCEPTED("SIMILARITY_ACCEPTED", "通过"),
    ;

    private final String value;
    private final String label;


    // 判断是否是最终状态（不再变化的状态）
    public boolean isFinalState() {
        return this != PENDING &&
                this != JUDGING &&
                this != COMPILING &&
                this != RUNNING &&
                this != QUEUING &&
                this != PENDING_REJUDGE &&
                this != REJUDGING;
    }

    // 判断是否是错误状态
    public boolean isErrorState() {
        return this == WRONG_ANSWER ||
                this == TIME_LIMIT_EXCEEDED ||
                this == MEMORY_LIMIT_EXCEEDED ||
                this == RUNTIME_ERROR ||
                this == COMPILATION_ERROR ||
                this == PRESENTATION_ERROR ||
                this == OUTPUT_LIMIT_EXCEEDED ||
                this == SYSTEM_ERROR ||
                this == RESTRICTED_FUNCTION ||
                this == DANGEROUS_OPERATION ||
                this == UNKNOWN_ERROR ||
                this == VALIDATOR_ERROR ||
                this == CHECKER_ERROR ||
                this == IDLENESS_LIMIT_EXCEEDED ||
                this == SECURITY_VIOLATION;
    }

    // 根据显示名称查找枚举
    public static JudgeStatus fromDisplayName(String displayName) {
        for (JudgeStatus status : values()) {
            if (status.label.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return UNKNOWN_ERROR;
    }
}