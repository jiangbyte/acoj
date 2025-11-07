package io.charlie.web.oj.modular.data.ranking.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.data.ranking.service.UserActivityService;
import io.charlie.web.oj.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@DS("slave")
public class UserActivityServiceImpl implements UserActivityService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private SysUserMapper userMapper;
    
    private static final String ACTIVITY_KEY_PREFIX = "oj:activity:";
    private static final String DAILY_LIMIT_KEY_PREFIX = "oj:daily_limit:";
    private static final String ACTIVITY_RANK_KEY = "oj:activity_rank";
    
    /**
     * 获取当前月份键
     */
    private String getCurrentMonthKey() {
        LocalDate now = LocalDate.now();
        return ACTIVITY_KEY_PREFIX + now.getYear() + "-" + now.getMonthValue();
    }
    
    /**
     * 获取每日限制键
     */
    private String getDailyLimitKey(String userId) {
        LocalDate today = LocalDate.now();
        return DAILY_LIMIT_KEY_PREFIX + today.toString() + ":" + userId;
    }
    
    @Override
    public void addActivity(String userId, String actionType, boolean isSolved) {
        String monthKey = getCurrentMonthKey();
        String dailyLimitKey = getDailyLimitKey(userId);
        
        // 计算本次得分
        int scoreToAdd = ActivityScoreCalculator.calculateScore(actionType, isSolved);
        
        if (scoreToAdd <= 0) {
            return;
        }
        
        // 检查每日限制
        Integer dailyScore = (Integer) redisTemplate.opsForValue().get(dailyLimitKey);
        if (dailyScore != null && dailyScore >= ActivityScoreCalculator.DAILY_LIMIT) {
            log.debug("用户 {} 今日活跃度已达上限", userId);
            return;
        }
        
        // 使用Lua脚本保证原子性操作
        String luaScript = """
                local dailyKey = KEYS[1]
                local monthKey = KEYS[2]
                local userId = ARGV[1]
                local scoreToAdd = tonumber(ARGV[2])
                local dailyLimit = tonumber(ARGV[3])
                
                -- 获取当前每日分数
                local dailyScore = redis.call('GET', dailyKey) or 0
                dailyScore = tonumber(dailyScore)
                
                -- 检查是否超过每日限制
                if dailyScore >= dailyLimit then
                    return 0
                end
                
                -- 计算实际可添加的分数
                local actualAdd = math.min(scoreToAdd, dailyLimit - dailyScore)
                
                -- 更新每日分数
                redis.call('INCRBY', dailyKey, actualAdd)
                redis.call('EXPIRE', dailyKey, 86400) -- 24小时过期
                
                -- 更新月度分数和排名
                redis.call('ZINCRBY', monthKey, actualAdd, userId)
                
                return actualAdd
                """;
        
        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        List<String> keys = Arrays.asList(dailyLimitKey, monthKey);
        Long actualAdded = redisTemplate.execute(script, keys, userId, scoreToAdd, 
                                               ActivityScoreCalculator.DAILY_LIMIT);
        
        if (actualAdded != null && actualAdded > 0) {
            log.debug("用户 {} {} 操作增加活跃度 {} 分", userId, actionType, actualAdded);
        }
    }
    
    @Override
    public BigDecimal getUserActivityScore(String userId) {
        String monthKey = getCurrentMonthKey();
        Double score = redisTemplate.opsForZSet().score(monthKey, userId);
        return score != null ? BigDecimal.valueOf(score) : BigDecimal.ZERO;
    }
    
    @Override
    public Page<SysUser> getActiveUsersPage(Page<SysUser> page) {
        String monthKey = getCurrentMonthKey();
        
        // 获取总记录数
        Long total = redisTemplate.opsForZSet().size(monthKey);
        if (total == null || total == 0) {
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return page;
        }
        
        // 计算分页参数
        long start = (page.getCurrent() - 1) * page.getSize();
        long end = start + page.getSize() - 1;
        
        // 获取用户ID和分数（按分数降序）
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(monthKey, start, end);
        
        if (tuples == null || tuples.isEmpty()) {
            page.setRecords(new ArrayList<>());
            page.setTotal(total);
            return page;
        }
        
        // 提取用户ID
        List<String> userIds = tuples.stream()
            .map(tuple -> tuple.getValue().toString())
            .collect(Collectors.toList());
        
        // 查询用户信息
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Map<String, SysUser> userMap = users.stream()
            .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        
        // 设置活跃度分数和排名
        List<SysUser> result = new ArrayList<>();
        long rank = start + 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String userId = tuple.getValue().toString();
            SysUser user = userMap.get(userId);
            if (user != null) {
                user.setActiveScore(BigDecimal.valueOf(tuple.getScore()));
                user.setRank(rank++);
                result.add(user);
            }
        }
        
        page.setRecords(result);
        page.setTotal(total);
        return page;
    }
    
    @Override
    public List<SysUser> getTopNActiveUsers(int n) {
        Page<SysUser> page = new Page<>(1, n);
        return getActiveUsersPage(page).getRecords();
    }
    
    @Override
    public Long getUserActivityRank(String userId) {
        String monthKey = getCurrentMonthKey();
        Long rank = redisTemplate.opsForZSet().reverseRank(monthKey, userId);
        return rank != null ? rank + 1 : null; // 转换为1-based排名
    }
    
    @Override
    @Scheduled(cron = "0 0 0 1 * ?") // 每月第一天执行
    public void resetMonthlyActivity() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String lastMonthKey = ACTIVITY_KEY_PREFIX + lastMonth.getYear() + "-" + lastMonth.getMonthValue();
        
        // 删除上个月的数据
        redisTemplate.delete(lastMonthKey);
        log.info("已重置 {} 月的活跃度数据", lastMonthKey);
        
        // 可以在这里添加持久化逻辑，将上个月的活跃度数据保存到数据库
    }
}