package io.charlie.web.oj.modular.data.relation.set.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.set.entity.DataSet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.TransPojo;

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
public class DataSetProblem implements Serializable, TransPojo {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private String id;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataSet.class, fields = "title", ref = "setIdName")
    private String setId;

    @TableField(exist = false)
    private String setIdName;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    private Integer sort;
}
