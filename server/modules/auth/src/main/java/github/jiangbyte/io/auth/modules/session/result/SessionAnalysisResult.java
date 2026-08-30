package github.jiangbyte.io.auth.modules.session.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 在线会话分析统计：账号/Token 数量、管理端与门户分布、近一小时新增等。
 *
 * Author: Charlie
 */
@Schema(description = "在线会话分析统计：账号/Token 数量、管理端与门户分布、近一小时新增等。")
@Data
public class SessionAnalysisResult {
    @Schema(description = "onlineAccountCount")

    private int onlineAccountCount;
    @Schema(description = "onlineTokenCount")
    private int onlineTokenCount;
    @Schema(description = "adminAccountCount")
    private int adminAccountCount;
    @Schema(description = "portalAccountCount")
    private int portalAccountCount;
    @Schema(description = "oneHourNewCount")
    private int oneHourNewCount;
    @Schema(description = "maxTokenCount")
    private int maxTokenCount;
}
