package github.jiangbyte.io.common.core.vault;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HashiCorp Vault 配置。
 *
 * Author: Charlie
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hei.vault")
public class HeiVaultProperties {

    private boolean enabled = false;

    private String uri = "";

    private String token = "";

    private String roleId = "";

    private String secretId = "";

    private String kvMount = "secret";

    private String kvPath = "hei/boot";

    private String namespace = "";

    /** 拉取失败时是否中止启动（生产建议 true）。 */
    private boolean failFast = true;
}
