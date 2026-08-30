package github.jiangbyte.io.iam.resource;

import java.util.List;

/**
 * 跨模块资源菜单只读门面：供 codegen 等选择父级菜单，不暴露 iam 实体。
 *
 * Author: Charlie
 */
public interface ResourceMenuApi {

    /**
     * 列出可作为父级的启用资源（CATALOG/MENU/PAGE）。
     *
     * @param client   客户端，如 ADMIN；空则默认 ADMIN
     * @param moduleId 可选模块 id；空则该 client 下全部模块
     */
    List<ResourceMenuNode> listParentMenus(String client, String moduleId);
}
