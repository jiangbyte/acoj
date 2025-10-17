package io.charlie.web.oj.modular.sys.log.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.Date;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录 编辑参数
*/
@Data
@Schema(name = "SysLog", description = "系统活动/日志记录 编辑参数")
public class SysLogEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "操作")
    private String operation;

    @Schema(description = "方法")
    private String method;

    @Schema(description = "参数")
    private String params;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "操作时间")
    private Date operationTime;

    @Schema(description = "操作分类")
    private String category;

    @Schema(description = "模块名称")
    private String module;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作结果")
    private String status;

    @Schema(description = "操作消息")
    private String message;
}