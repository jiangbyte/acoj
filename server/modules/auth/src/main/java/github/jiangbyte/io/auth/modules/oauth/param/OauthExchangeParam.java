/** Author: Charlie */

package github.jiangbyte.io.auth.modules.oauth.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "OauthExchangeParam")
@Data
public class OauthExchangeParam {
    @NotBlank
    @Schema(description = "编码")
    private String code;
}
