package github.jiangbyte.io.sys.modules.config.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测试审计告警 Webhook 入参。
 *
 * Author: Charlie
 */
@Schema(description = "测试审计告警 Webhook 入参。")
@Data
public class SysConfigTestWebhookParam {
    @Schema(description = "webhookUrl")

    private String webhookUrl;
    @Schema(description = "webhookSecret")
    private String webhookSecret;
}
