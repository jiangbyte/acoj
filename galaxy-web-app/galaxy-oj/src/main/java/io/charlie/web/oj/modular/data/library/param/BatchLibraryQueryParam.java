package io.charlie.web.oj.modular.data.library.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
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
public class BatchLibraryQueryParam {

    private String setId; // 检测的集合id

    private String problemId; // 检测的题目
    private String language; // 检测的语言

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date startTime;

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date endTime;

    // 对比模式
    @Schema(description = "对比模式")
    private String compareMode; // 两两对比（PAIR_BY_PAIR），组内对比(GROUP_BY_GROUP)，多人对比(MULTI_BY_MULTI)

    private String groupId;

    private List<String> userIds;

    // 代码筛选
    @Schema(description = "代码筛选")
    private String codeFilter; // 有效提交(VALID_SUBMIT)，时间窗口(TIME_WINDOW)

    @Schema(description = "代码筛选时间窗口")
    private Integer codeFilterTimeWindow;

    // 是否启用采样
    @Schema(description = "是否启用采样")
    private Boolean enableSampling;

    // 采样比例
    @Schema(description = "采样比例")
    private BigDecimal samplingRatio;

    // 匹配敏感度
    @Schema(description = "匹配敏感度")
    private Integer minMatchLength; // 5-15,宽松，标准，严格

    private String taskId;
}
