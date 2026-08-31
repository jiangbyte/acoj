package github.jiangbyte.io.oj.modules.stat.service;

import github.jiangbyte.io.oj.modules.stat.result.OjUserHomepageResult;

/**
 * 门户用户主页公开统计。
 * <p>
 * Author: Charlie
 */
public interface OjUserHomepageService {

    /**
     * 按账户聚合公开解题进度、语言、热力图与最近通过。
     */
    OjUserHomepageResult homepage(String accountId);
}
