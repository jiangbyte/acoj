package io.charlie.web.oj.modular.data.similarity.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CloneLevel implements Serializable {
    private String cloneLevel; // 分级英文字典
    private String cloneLevelName; // 分级程度
    private BigDecimal similarity; // 分级的相似度
    private Integer count; // 数量
    private BigDecimal percentage; // 相似度占比
}