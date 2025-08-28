package io.charlie.app.core.modular.set.progress.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description TODO
 */
@Data
public class ProgressData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 题目标题
    private String title;

    // 题目ID
    private String id;

    // 该题目的用户的状态
    private String status;

    // 该题目的用户的完成时间
    private String completedAt;
}
