package io.charlie.web.oj.modular.data.contest.utils;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.contest.entity.DataContestAuth;
import io.charlie.web.oj.modular.data.contest.mapper.DataContestAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/11/2025
 */
@Component
@RequiredArgsConstructor
public class ContestAuthTool {
    private final DataContestAuthMapper dataContestAuthMapper;

    public boolean userIsAuth(String contestId, String userId) {
        List<DataContestAuth> dataContestAuths = dataContestAuthMapper.selectList(
                new LambdaQueryWrapper<DataContestAuth>()
                        .eq(DataContestAuth::getContestId, contestId)
                        .eq(DataContestAuth::getUserId, userId)
        );

        if (ObjectUtil.isEmpty(dataContestAuths)) {
            return false;
        }

        Map<String, Boolean> haveAuthMap = dataContestAuths.stream()
                .filter(auth -> auth != null && auth.getContestId() != null) // 过滤空对象和空ID
                .collect(Collectors.toMap(
                        DataContestAuth::getContestId,
                        DataContestAuth::getIsAuth,
                        (existing, replacement) -> existing // 处理重复key
                ));

        return haveAuthMap.getOrDefault(contestId, false);
    }
}
