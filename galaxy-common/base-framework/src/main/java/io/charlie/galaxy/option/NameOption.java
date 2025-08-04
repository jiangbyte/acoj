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
public class NameOption<T> implements Serializable {

    private T value;

    private String name;

    private List<NameOption<T>> children;

    public NameOption(T value, String name) {
        this.value = value;
        this.name = name;
    }

    public NameOption(T value, String name, List<NameOption<T>> children) {
        this.value = value;
        this.name = name;
        this.children = children;
    }
}
