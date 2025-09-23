package io.charlie.web.oj.modular.data.relation.set.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
@Data
@TableName("data_set_problem")
@Schema(name = "DataSetProblem")
public class DataSetProblem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String setId;

    private String problemId;

    private Integer sort;
}
