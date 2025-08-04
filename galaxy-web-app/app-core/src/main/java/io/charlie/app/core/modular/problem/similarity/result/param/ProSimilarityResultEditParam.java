package io.charlie.app.core.modular.problem.similarity.result.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 代码相似度检测结果详情 编辑参数
*/
@Data
@Schema(name = "ProSimilarityResult", description = "代码相似度检测结果详情 编辑参数")
public class ProSimilarityResultEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "关联的任务ID")
    private String taskId;

    @Schema(description = "提交ID")
    private String originSubmitId;

    @Schema(description = "被比较的提交ID")
    private String comparedSubmitId;

    @Schema(description = "相似度值")
    private BigDecimal similarity;

    @Schema(description = "详细比对结果")
    private String details;

    @Schema(description = "匹配部分详情")
    private String matchDetails;

    @Schema(description = "相似度阈值")
    private BigDecimal threshold;

}