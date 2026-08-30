package github.jiangbyte.io.sys.config;

import lombok.Data;

/**
 * 站点页脚公开信息：版权与备案。
 *
 * Author: Charlie
 */
@Data
public class SiteFooterResult {

    private String copyrightText = "";
    private String copyrightUrl = "";
    /** ICP 备案号，如「京ICP备xxxxxxxx号」 */
    private String icpNumber = "";
    /** ICP 备案查询链接 */
    private String icpUrl = "";
    /** 公安备案号 */
    private String psbNumber = "";
    /** 公安备案查询链接 */
    private String psbUrl = "";
}
