package io.charlie.web.oj.modular.data.ranking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.ranking.param.RankingPageParam;
import io.charlie.web.oj.modular.data.ranking.service.ProblemSetCacheService;
import io.charlie.web.oj.modular.data.ranking.data.PageResult;
import io.charlie.web.oj.modular.data.ranking.data.RankItem;
import io.charlie.web.oj.modular.data.ranking.service.ProblemCacheService;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.param.SysUserIdParam;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "排行榜控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class RankController {
    private final UserCacheService userCacheService;
    private final ProblemCacheService problemCacheService;
    private final SysUserService sysUserService;
    private final DataSubmitMapper dataSubmitMapper;

    @Operation(summary = "获取用户排行榜")
    @GetMapping("/data/user/rank/top")
    public Result<?> getUserTopN() {
        List<RankItem> userRankTopN = userCacheService.getUserRankTopN(10);
        List<SysUser> sysUsers = new ArrayList<>();
        for (RankItem rankItem : userRankTopN) {
            SysUserIdParam sysUserIdParam = new SysUserIdParam();
            sysUserIdParam.setId(rankItem.getId());
            SysUser sysUser = sysUserService.appDetail(sysUserIdParam);
            sysUser.setRank(rankItem.getRank());
            sysUser.setScore(rankItem.getScore());
            Long l = dataSubmitMapper.selectCount(new LambdaQueryWrapper<DataSubmit>()
                    .eq(DataSubmit::getUserId, sysUser.getId())
                    .eq(DataSubmit::getIsSet, false)
                    .eq(DataSubmit::getSubmitType, true)
            );
            sysUser.setSubmitCount(l);
            sysUsers.add(sysUser);
        }
        return Result.success(sysUsers);
    }

    @GetMapping("/data/user/rank/active/top")
    public Result<?> getUserRankActiveTop() {
        List<RankItem> userRankTopN = userCacheService.getUserActivityTopN(10);
        List<SysUser> sysUsers = new ArrayList<>();
        for (RankItem rankItem : userRankTopN) {
            SysUserIdParam sysUserIdParam = new SysUserIdParam();
            sysUserIdParam.setId(rankItem.getId());
            SysUser sysUser = sysUserService.appDetail(sysUserIdParam);
            sysUser.setRank(rankItem.getRank());
            sysUser.setScore(userCacheService.getUserActivity(sysUser.getId()));
            Long l = dataSubmitMapper.selectCount(new LambdaQueryWrapper<DataSubmit>()
                    .eq(DataSubmit::getUserId, sysUser.getId())
                    .eq(DataSubmit::getIsSet, false)
                    .eq(DataSubmit::getSubmitType, true)
            );
            sysUser.setSubmitCount(l);
            sysUsers.add(sysUser);
        }

        return Result.success(sysUsers);
    }

    @GetMapping("/data/user/rank/page")
    public Result<?> getUserRankPage(@ParameterObject RankingPageParam rankingPageParam) {
        PageResult<RankItem> userRankPage = userCacheService.getUserRankPage(rankingPageParam.getCurrent(), rankingPageParam.getSize());
        List<RankItem> items = userRankPage.getItems();
        Page<SysUser> sysUserPage = new Page<>();
        sysUserPage.setCurrent(rankingPageParam.getCurrent());
        sysUserPage.setSize(rankingPageParam.getSize());
        sysUserPage.setTotal(userRankPage.getTotal());
        List<SysUser> sysUsers = new ArrayList<>();
        for (RankItem rankItem : items) {
            SysUserIdParam sysUserIdParam = new SysUserIdParam();
            sysUserIdParam.setId(rankItem.getId());
            SysUser sysUser = sysUserService.appDetail(sysUserIdParam);
            sysUser.setRank(rankItem.getRank());
            sysUser.setScore(rankItem.getScore());

            Long l = dataSubmitMapper.selectCount(new LambdaQueryWrapper<DataSubmit>()
                    .eq(DataSubmit::getUserId, sysUser.getId())
                    .eq(DataSubmit::getIsSet, false)
                    .eq(DataSubmit::getSubmitType, true)
            );
            sysUser.setSubmitCount(l);
            sysUsers.add(sysUser);
        }
        sysUserPage.setRecords(sysUsers);
        return Result.success(sysUserPage);
    }

}