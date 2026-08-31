package github.jiangbyte.io.oj.modules.judge.node.param;

/**
 * 编辑 OJ 执行机入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "OjJudgeNode 编辑入参")
@Data
public class OjJudgeNodeEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "节点编码")
    private String code;
    @NotBlank
    @Schema(description = "展示名")
    private String name;
    @NotBlank
    @Schema(description = "SparkSandbox 根地址")
    private String baseUrl;
    @Schema(description = "是否对该节点验签")
    private Boolean signingEnabled;
    @Schema(description = "节点密钥密文；空则用全局默认")
    private String signingSecretCipher;
    @NotBlank
    @Schema(description = "管理状态：ENABLED/DISABLED/DRAINING")
    private String adminStatus;
    @Schema(description = "调度权重，越大越易被选中")
    private Integer weight;
    @Schema(description = "并列时优先级，越小越优先")
    private Integer priority;
    @Schema(description = "ACOJ 侧最大在途")
    private Integer maxConcurrency;
    @Schema(description = "支持语言；空数组=全语言")
    private List<String> supportedLanguages;
    @Schema(description = "机房/AZ/备注等")
    private Map<String, Object> extra;
}
