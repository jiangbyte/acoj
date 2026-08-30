package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号授权用户组入参：账号 id + 用户组 id 列表。
 *
 * Author: Charlie
 */
@Schema(description = "账号授权用户组入参：账号 id + 用户组 id 列表。")
@Data
public class SysAccountGrantGroupParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "groupIds")
    private List<String> groupIds = new ArrayList<>();
}
