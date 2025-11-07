package io.charlie.web.oj.modular.sys.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 公告表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_notice")
@Schema(name = "SysNotice", description = "公告表")
public class SysNotice extends CommonEntity {
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
    private Byte sort;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisible;

}
