package github.jiangbyte.io.sys.modules.file.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按对象名查询文件入参。
 *
 * Author: Charlie
 */
@Schema(description = "按对象名查询文件入参。")
@Data
public class SysFileObjectNameParam {

    @NotBlank
    @Schema(description = "objectName")
    private String objectName;
}
