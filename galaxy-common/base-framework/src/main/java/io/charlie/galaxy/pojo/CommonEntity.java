package io.charlie.galaxy.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import lombok.Data;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 21/07/2025
 * @description 基础实体类
 */
@Data
@JsonIgnoreProperties(value = "transMap") // Easy-Trans 会添加 transMap 属性，避免反序列化报错
public class CommonEntity implements Serializable, TransPojo {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 记录创建的时间戳
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date createTime;

    /**
     * 创建者ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Trans(type = TransType.SIMPLE, targetClassName = "io.charlie.web.oj.modular.sys.user.entity.SysUser", fields = {"avatar", "nickname"}, refs = {"createUserAvatar", "createUserName"})
    private String createUser;

    /**
     * 头像
     */
    @TableField(exist = false)
    private String createUserAvatar;

    /**
     * 创建者名称
     */
    @TableField(exist = false)
    private String createUserName;

    /**
     * 记录最后更新的时间戳
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date updateTime;

    /**
     * 记录最后更新的用户ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Trans(type = TransType.SIMPLE, targetClassName = "io.charlie.web.oj.modular.sys.user.entity.SysUser", fields = {"avatar", "nickname"}, refs = {"updateUserAvatar", "updateUserName"})
    private String updateUser;

    /**
     * 头像
     */
    @TableField(exist = false)
    private String updateUserAvatar;

    /**
     * 更新者名称
     */
    @TableField(exist = false)
    private String updateUserName;
}
