package github.jiangbyte.io.sys.modules.weakpassword.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 弱密码分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "弱密码分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysWeakPasswordPageParam extends PageQuery {
    @Schema(description = "弱口令明文（用于注册/改密校验）")

    private String password;
    @Schema(description = "keyword")
    private String keyword;
}
