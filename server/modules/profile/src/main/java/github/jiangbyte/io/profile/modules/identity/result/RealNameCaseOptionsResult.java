package github.jiangbyte.io.profile.modules.identity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 实名认证可选项：业务类型与可用通道。
 *
 * Author: Charlie
 */
@Schema(description = "实名认证可选项：业务类型与可用通道。")
@Data
public class RealNameCaseOptionsResult {
    @Schema(description = "businessTypes")

    private List<RealNameBusinessOptionResult> businessTypes = new ArrayList<>();
    @Schema(description = "documentTypes")
    private List<String> documentTypes = new ArrayList<>();

    @Data
    public static class RealNameBusinessOptionResult {
        @Schema(description = "业务类型")
        private String businessType;
        @Schema(description = "label")
        private String label;
        @Schema(description = "channels")
        private List<String> channels = new ArrayList<>();
    }
}
