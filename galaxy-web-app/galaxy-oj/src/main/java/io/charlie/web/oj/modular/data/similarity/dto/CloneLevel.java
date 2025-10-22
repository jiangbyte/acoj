package io.charlie.web.oj.modular.data.similarity.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CloneLevel implements Serializable {
        private String cloneLevel;
        private String cloneLevelName;
        private BigDecimal similarity;
        private Integer count;
        private BigDecimal percentage; // 相似度占比
    }