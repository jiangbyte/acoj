package io.charlie.web.oj.modular.task.similarity.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 批量检测参数
 */
@Data
public class BatchSimilarityParam {

    private List<String> problemIds;
    private String setId;
    private String language;
    private Boolean isSet;
    private List<String> userIds;
    private String taskId;
    private Boolean isGroup;
    private String groupId;
    private String batchTaskId;
    private Integer minMatchLength;
    private BigDecimal threshold;
}
