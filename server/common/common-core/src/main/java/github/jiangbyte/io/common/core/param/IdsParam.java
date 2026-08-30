package github.jiangbyte.io.common.core.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量 id 请求体，用于批量删除/批量操作等接口。
 *
 * Author: Charlie
 */
@Schema(description = "批量 id 请求体，用于批量删除/批量操作等接口。")
@Data
public class IdsParam {

    @NotEmpty
    private List<@NotBlank @Size(max = 64) String> ids;
}
