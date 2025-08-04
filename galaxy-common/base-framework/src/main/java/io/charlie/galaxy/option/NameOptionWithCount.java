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
public class NameOptionWithCount<T> implements Serializable {

    private T value;

    private String name;

    private Integer count;

    private List<NameOptionWithCount<T>> children;

    public NameOptionWithCount(T value, String name) {
        this.value = value;
        this.name = name;
    }

    public NameOptionWithCount(T value, String name, Integer count) {
        this.value = value;
        this.name = name;
        this.count = count;
    }

    public NameOptionWithCount(T value, String name, List<NameOptionWithCount<T>> children) {
        this.value = value;
        this.name = name;
        this.children = children;
    }

    public NameOptionWithCount(T value, String label, Integer count, List<NameOptionWithCount<T>> children) {
        this.value = value;
        this.name = name;
        this.count = count;
        this.children = children;
    }
}
