package io.charlie.app.core.modular.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.ranking.data.UserRankingInfo;
import io.charlie.app.core.modular.ranking.enums.RankingEnums;
import io.charlie.app.core.modular.ranking.param.UserRankingPageParam;
import io.charlie.app.core.modular.ranking.service.UserRankingService;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class UserRankingServiceImpl implements UserRankingService {
    private final RankingUtil rankingUtil;

    private final SysUserService sysUserService;

    @Override
    public void problemUserRankingUpdate(String id) {
        rankingUtil.addOrUpdateAutoScore(RankingEnums.USER_TOTAL.getValue(), id, 1.0);
    }

    @Override
    public Result.PageData<UserRankingInfo> problemUserRankingPage(UserRankingPageParam param) {
        List<String> stringList = sysUserService.list(new LambdaQueryWrapper<SysUser>().like(SysUser::getNickname, param.getKeyword())).stream().map(SysUser::getId).toList();
        Result.PageData<RankingInfo> rankingPageByEntityIds = rankingUtil.getRankingPageByEntityIds(RankingEnums.USER_TOTAL.getValue(), stringList, param.getCurrent(), param.getSize(), ISortOrderEnum.valueOf(param.getSortOrder()));
        System.out.println(rankingPageByEntityIds);
        return null;
    }

    @Override
    public List<UserRankingInfo> problemUserRankingTopN(int n) {
        return List.of();
    }

    @Override
    public UserRankingInfo problemUserRankingById(String id) {
        return null;
    }
}

