package github.jiangbyte.io.common.satoken;

import cn.dev33.satoken.stp.StpLogic;

/**
 * Sa-Token 多账号体系入口：暴露 Admin / Portal 等 StpLogic。
 *
 * Author: Charlie
 */
public final class StpKit {

    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_PORTAL = "portal";

    public static final StpLogic ADMIN = new StpLogic(TYPE_ADMIN);
    public static final StpLogic PORTAL = new StpLogic(TYPE_PORTAL);

    private StpKit() {
    }
}
