package github.jiangbyte.io.sys.modules.file.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "文件分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFilePageParam extends PageQuery {
    @Schema(description = "用户上传时的原始文件名")

    private String originalName;
    @Schema(description = "对象存储中的对象键/路径")
    private String objectName;
    @Schema(description = "存储服务商：minio/rustfs/oss/s3")
    private String storageProvider;
    @Schema(description = "MIME 类型")
    private String contentType;
}
