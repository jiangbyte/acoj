package github.jiangbyte.io.sys.modules.weakpassword.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 弱密码实体，对应表 sys_weak_password。
 *
 * Author: Charlie
 */
@Schema(description = "弱密码实体，对应表 sys_weak_password。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_weak_password")
public class SysWeakPassword extends BaseEntity {
    @Schema(description = "弱口令明文（用于注册/改密校验）")
    private String password;
}
