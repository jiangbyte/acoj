package io.charlie.web.oj.modular.data.relation.set.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-07
* @description 题集题目 编辑参数
*/
@Data
@Schema(name = "DataSetProblem", description = "题集题目 编辑参数")
public class DataSetProblemEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "排序")
    private Integer sort;

}