package io.charlie.web.oj.modular.task.similarity.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import lombok.Data;

import java.util.Date;

@Data
public class SimilarityProgressDto {
    private String taskId;
    private Integer totalCount;
    private Integer processedCount;
    private Double progress; // 百分比
    private String status;
    private String currentStep;
    private String errorMessage;

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date startTime;

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date endTime;
}