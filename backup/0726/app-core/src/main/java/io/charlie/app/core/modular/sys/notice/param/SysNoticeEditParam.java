package io.charlie.app.core.modular.sys.notice.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 公告 编辑参数
*/
@Data
@Schema(name = "SysNotice", description = "公告 编辑参数")
public class SysNoticeEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "链接")
    private String url;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "内容")
    private String content;

}