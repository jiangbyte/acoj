package io.charlie.web.oj.modular.data.ranking.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 分页结果
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> items;
    private Long total;
    private Integer page;
    private Integer size;
    private Long totalPages;
}