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
public class LabelOption<T> implements Serializable {

    private T value;

    private String label;

    private List<LabelOption<T>> children;

    public LabelOption(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public LabelOption(T value, String label, List<LabelOption<T>> children) {
        this.value = value;
        this.label = label;
        this.children = children;
    }
}
