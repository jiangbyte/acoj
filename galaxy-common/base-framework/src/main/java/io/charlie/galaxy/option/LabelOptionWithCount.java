package io.charlie.galaxy.option;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 下拉选项对象
 */
@Data
@NoArgsConstructor
public class LabelOptionWithCount<T> implements Serializable {

    private T value;

    private String label;

    private Integer count;

    private List<LabelOptionWithCount<T>> children;

    public LabelOptionWithCount(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public LabelOptionWithCount(T value, String label, Integer count) {
        this.value = value;
        this.label = label;
        this.count = count;
    }

    public LabelOptionWithCount(T value, String label, List<LabelOptionWithCount<T>> children) {
        this.value = value;
        this.label = label;
        this.children = children;
    }

    public LabelOptionWithCount(T value, String label, Integer count, List<LabelOptionWithCount<T>> children) {
        this.value = value;
        this.label = label;
        this.count = count;
        this.children = children;
    }
}
