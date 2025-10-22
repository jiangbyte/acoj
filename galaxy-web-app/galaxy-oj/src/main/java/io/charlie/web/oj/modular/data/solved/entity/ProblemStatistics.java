package io.charlie.web.oj.modular.data.solved.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
    @AllArgsConstructor
    public  class ProblemStatistics {
        // 通过率
        private BigDecimal acceptanceRate;
        // 参与总人数
        private Integer totalParticipants;
        // 通过人数
        private Integer acceptedParticipants;
    }