package io.charlie.web.oj.modular.data.problem.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description 题目动态数据统计
 */
@Data
public class DataProblemCount {
    private Long total;
    private Long monthAdd; // 本月新增
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date lastAddTime; // 上次新增时间
    private BigDecimal growthRate; // 增长率

    private Long solved; // 用户登录会追踪，默认0

    private BigDecimal avgPassRate; // 平均通过率
}
