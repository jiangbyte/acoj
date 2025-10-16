package io.charlie.web.oj.modular.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 16/10/2025
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class UserSolvedCacheRecord {
    // 增加 用户解决记录
    public void addUserSolvedRecord(String userId, String problemId) {
        // 时间戳、解决状态（已解决、未解决）、用户下全部题目，题目下全部用户，用户下已解决题目，用户下未解决题目，题目下已解决用户，题目下未解决用户，用户下题目通过率，题目下解决用户通过率
    }
}
