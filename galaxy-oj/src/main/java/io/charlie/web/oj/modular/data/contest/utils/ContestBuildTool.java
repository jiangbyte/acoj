package io.charlie.web.oj.modular.data.contest.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import io.charlie.web.oj.modular.data.contest.entity.DataContestAuth;
import io.charlie.web.oj.modular.data.contest.entity.DataContestParticipant;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestAuthMapper;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestParticipantMapper;
import io.charlie.web.oj.modular.data.contest.service.DataContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/11/2025
 */
@Component
@RequiredArgsConstructor
public class ContestBuildTool {
    private final DataContestAuthMapper dataContestAuthMapper;
    private final DataContestParticipantMapper dataContestParticipantMapper;

    public void buildContests(List<DataContest> dataContests) {
        if (CollectionUtil.isEmpty(dataContests)) {
            return;
        }
        buildContestsBatch(dataContests);
    }

    public void buildContest(DataContest dataContest) {
        buildContestsBatch(Collections.singletonList(dataContest));
    }

    public void buildContestsBatch(List<DataContest> dataContests) {
        // 空值检查
        if (CollectionUtil.isEmpty(dataContests)) {
            return;
        }

        // 提取竞赛ID并去重
        List<String> contestIds = dataContests.stream()
                .map(DataContest::getId)
                .filter(Objects::nonNull) // 过滤空ID
                .distinct()
                .toList();

        if (CollectionUtil.isEmpty(contestIds)) {
            return;
        }

        // 获取当前用户ID
        String userId = null;
        try {
            userId = StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            // 用户未登录，userId保持为null
        }

        // 查询权限信息
        Map<String, Boolean> haveAuthMap = new HashMap<>();
        if (userId != null) {
            List<DataContestAuth> dataContestAuths = dataContestAuthMapper.selectList(
                    new LambdaQueryWrapper<DataContestAuth>()
                            .in(DataContestAuth::getContestId, contestIds)
                            .eq(DataContestAuth::getUserId, userId)
            );

            // 构建权限映射，确保每个contestId都有对应的权限值
            haveAuthMap = dataContestAuths.stream()
                    .filter(auth -> auth != null && auth.getContestId() != null) // 过滤空对象和空ID
                    .collect(Collectors.toMap(
                            DataContestAuth::getContestId,
                            DataContestAuth::getIsAuth,
                            (existing, replacement) -> existing // 处理重复key
                    ));
        }

        // 查询报名信息
        Map<String, Boolean> haveRegisterMap = new HashMap<>();
        if (userId != null) {
            List<DataContestParticipant> dataContestParticipants = dataContestParticipantMapper.selectList(
                    new LambdaQueryWrapper<DataContestParticipant>()
                            .in(DataContestParticipant::getContestId, contestIds)
                            .eq(DataContestParticipant::getUserId, userId)
            );
            haveRegisterMap = contestIds.stream()
                    .collect(Collectors.toMap(
                            contestId -> contestId,
                            contestId -> dataContestParticipants.stream()
                                    .anyMatch(participant -> participant != null
                                            && contestId.equals(participant.getContestId()))
                    ));
        }

        // 设置每个竞赛的权限状态
        for (DataContest dataContest : dataContests) {
            Boolean haveRegister = haveRegisterMap.getOrDefault(dataContest.getId(), false);
            dataContest.setIsRegister(haveRegister);

            if (dataContest == null || dataContest.getId() == null) {
                continue; // 跳过空对象或空ID的竞赛
            }

            // 私有竞赛必须检查权限
            if (Boolean.FALSE.equals(dataContest.getIsPublic())) {
                // 检查不到权限时默认给false
                boolean hasAuth = haveAuthMap.getOrDefault(dataContest.getId(), false);
                dataContest.setIsAuth(hasAuth);
            } else {
                // 公开竞赛默认给权限
                dataContest.setIsAuth(true);
            }
        }
    }
}
