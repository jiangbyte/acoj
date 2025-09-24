package io.charlie.web.oj.modular.data.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.ranking.UserRankingPageParam;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description TODO
 */
public interface SetUserRankingService {
    void processSubmission(DataSubmit dataSubmit);

    List<SysUser> getTopUsers(String setId, int limit);

    SysUser getUserRank(String setId, String userId);

    Page<SysUser> getRankingPage(String setId, UserRankingPageParam userRankingPageParam);

    List<SysUser> getActiveTop(int limit);

    Boolean isSolved(String setId, String userId, String problemId);
}
