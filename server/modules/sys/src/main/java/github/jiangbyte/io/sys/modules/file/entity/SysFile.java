package github.jiangbyte.io.sys.modules.file.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件元数据实体，对应表 sys_file。
 *
 * Author: Charlie
 */
@Schema(description = "文件元数据实体，对应表 sys_file。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {
    @Schema(description = "对象存储中的对象键/路径")
    private String objectName;
    @Schema(description = "用户上传时的原始文件名")
    private String originalName;
    @Schema(description = "存储服务商：minio/rustfs/oss/s3")
    private String storageProvider;
    @Schema(description = "对象存储桶名称")
    private String bucket;
    @Schema(description = "MIME 类型")
    private String contentType;
    @Schema(description = "每页条数")
    private Long size;
    @Schema(description = "文件访问 URL（可为签名地址）")
    private String url;
}
