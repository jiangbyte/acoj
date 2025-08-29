package io.charlie.app.core.modular.problem.problem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProblemImport {
    @JsonProperty("display_id") // 添加这个注解
    private Long displayId;

    private String title;

    private Description description;

    private List<String> tags;

    @JsonProperty("input_description") // 如果需要的话
    private Description inputDescription;

    @JsonProperty("output_description") // 如果需要的话
    private Description outputDescription;

    @JsonProperty("test_case_score") // 如果需要的话
    private List<TestCaseScore> testCaseScore;

    private Description hint;

    @JsonProperty("time_limit") // 如果需要的话
    private Integer timeLimit;

    @JsonProperty("memory_limit") // 如果需要的话
    private Integer memoryLimit;

    private List<Sample> samples;

    private Object template;

    private Object spj;

    @JsonProperty("rule_type") // 如果需要的话
    private String ruleType;

    private String source;

    private List<Object> answers;

    // 其他内部类保持不变
    @Data
    public static class Description {
        private String format;
        private String value;
    }

    @Data
    public static class TestCaseScore {
        private Integer score;

        @JsonProperty("input_name") // 如果需要的话
        private String inputName;

        @JsonProperty("output_name") // 如果需要的话
        private String outputName;
    }

    @Data
    public static class Sample {
        private String input;
        private String output;
    }
}