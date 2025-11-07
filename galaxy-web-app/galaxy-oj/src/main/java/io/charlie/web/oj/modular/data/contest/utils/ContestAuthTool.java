package io.charlie.web.oj.modular.data.contest.utils;

import io.charlie.web.oj.modular.data.contest.mapper.DataContestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 06/11/2025
 */
@Component
@RequiredArgsConstructor
public class ContestAuthTool {
    private final DataContestMapper dataContestMapper;

    public boolean userIsAuth(String contestId, String userId) {
        return false;
    }
}
