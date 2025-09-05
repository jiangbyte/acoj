package io.charlie.app.core.modular.analyse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.app.core.modular.analyse.entity.TotalUserAnalyse;
import io.charlie.app.core.modular.analyse.service.UserAnalyseService;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 分析服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserAnalyseServiceImpl implements UserAnalyseService {
    private final SysUserMapper sysUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVITY_KEY_PREFIX = "user:activity:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public TotalUserAnalyse getTotalUserAnalyse() {
        TotalUserAnalyse totalUserAnalyse = new TotalUserAnalyse();
        totalUserAnalyse.setRegisterUser(sysUserMapper.selectCount(new LambdaQueryWrapper<>()));
        totalUserAnalyse.setDailyActiveUser(getDailyActiveUser());
        totalUserAnalyse.setWeeklyActiveUser(getWeeklyActiveUser());
        totalUserAnalyse.setMonthlyActiveUser(getMonthlyActiveUser());
        return totalUserAnalyse;
    }

    /**
     * 获取日活跃用户数(DAU)
     */
    private Long getDailyActiveUser() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String pattern = ACTIVITY_KEY_PREFIX + "*:" + today;
        return countDistinctUsersByPattern(pattern);
    }

    /**
     * 获取周活跃用户数(WAU)
     */
    private Long getWeeklyActiveUser() {
        LocalDate now = LocalDate.now();
        Set<String> keys = new java.util.HashSet<>();

        // 获取最近7天的key模式
        for (int i = 0; i < 7; i++) {
            String date = now.minusDays(i).format(DATE_FORMATTER);
            keys.add(ACTIVITY_KEY_PREFIX + "*:" + date);
        }

        return countDistinctUsersByPatterns(keys);
    }

    /**
     * 获取月活跃用户数(MAU)
     */
    private Long getMonthlyActiveUser() {
        LocalDate now = LocalDate.now();
        Set<String> keys = new java.util.HashSet<>();

        // 获取最近30天的key模式
        for (int i = 0; i < 30; i++) {
            String date = now.minusDays(i).format(DATE_FORMATTER);
            keys.add(ACTIVITY_KEY_PREFIX + "*:" + date);
        }

        return countDistinctUsersByPatterns(keys);
    }

    /**
     * 根据key模式统计不重复用户数
     */
    private Long countDistinctUsersByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }

        // 从key中提取用户ID(格式: user:activity:{userId}:{date})
        return keys.stream()
                .map(key -> key.split(":")[2])
                .distinct()
                .count();
    }

    /**
     * 根据多个key模式统计不重复用户数
     */
    private Long countDistinctUsersByPatterns(Set<String> patterns) {
        Set<String> userIds = new java.util.HashSet<>();

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null) {
                keys.forEach(key -> {
                    String[] parts = key.split(":");
                    if (parts.length >= 4) {
                        userIds.add(parts[2]); // 获取用户ID部分
                    }
                });
            }
        }

        return (long) userIds.size();
    }
}
