package io.charlie.web.oj.modular.sys.auth.result;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-22
 * @description 登录用户
 */
@Data
@Schema(name = "LoginUser", description = "登录用户")
public class LoginUser implements Serializable, TransPojo {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户组")
    private String groupId;

    @Schema(description = "用户组名称")
    private String groupIdName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "背景图片")
    private String background;

    @Schema(description = "签名")
    private String quote;

    @Schema(description = "性别")
    @Trans(type = TransType.DICTIONARY, key = "SYS_GENDER", ref = "genderName")
    private Integer gender;

    @Schema(description = "性别名称")
    @TableField(exist = false)
    private String genderName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "学号")
    private String studentNumber;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建用户")
    private String createUser;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新用户")
    private String updateUser;

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date loginTime;

    private List<String> roleNames;
}
