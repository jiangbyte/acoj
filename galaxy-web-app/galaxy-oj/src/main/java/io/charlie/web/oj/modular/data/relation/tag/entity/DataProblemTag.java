package io.charlie.web.oj.modular.data.relation.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description 题目标签实体类
 */
@Data
@TableName("data_problem_tag")
@Schema(name = "DataProblemTag")
public class DataProblemTag implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private String id;

    private String problemId;

    private String tagId;
}
