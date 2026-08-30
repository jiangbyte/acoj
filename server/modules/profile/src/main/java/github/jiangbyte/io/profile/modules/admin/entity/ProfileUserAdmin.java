package github.jiangbyte.io.profile.modules.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.core.trans.vo.TransPojo;

import java.time.OffsetDateTime;

/**
 * 管理端用户资料实体，对应表 {@code profile_user_admin}；主键为账号 ID。
 *
 * Author: Charlie
 */
@Schema(description = "管理端用户资料实体，对应表 profile_user_admin；主键为账号 ID。")
@Data
@TableName("profile_user_admin")
public class ProfileUserAdmin implements TransPojo {
    @TableId(value = "account_id", type = IdType.INPUT)
    @Schema(description = "关联系统账号ID（主键）")
    private String accountId;
    @Schema(description = "管理端显示昵称")
    private String nickname;
    @Schema(description = "头像 object_name 或 URL")
    private String avatar;
    @Schema(description = "个性签名")
    private String signature;
    @Schema(description = "绑定手机号")
    private String phone;
    @Schema(description = "绑定邮箱")
    private String email;
    @Schema(description = "备注说明")
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人（账户ID）")
    private String createdBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人（账户ID）")
    private String updatedBy;
}
