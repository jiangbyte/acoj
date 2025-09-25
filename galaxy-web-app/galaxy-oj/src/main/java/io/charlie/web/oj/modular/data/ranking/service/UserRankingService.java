package io.charlie.web.oj.modular.data.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import io.charlie.web.oj.modular.data.ranking.UserRankingPageParam;
import io.charlie.web.oj.modular.data.ranking.enums.RankingEnums;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 * @description 用户排行榜
 */
public interface UserRankingService {
    void processSubmission(DataSubmit dataSubmit);

    List<SysUser> getTopUsers(int limit);

    SysUser getUserRank(String userId);

    Page<SysUser> getRankingPage(UserRankingPageParam userRankingPageParam);

    List<SysUser> getActiveTop(int limit);

    Boolean isSolved(String userId, String problemId);
}
