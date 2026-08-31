package github.jiangbyte.io.oj.modules.stat.service.impl;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.stat.mapper.OjUserHomepageMapper;
import github.jiangbyte.io.oj.modules.stat.result.OjDifficultyCountRow;
import github.jiangbyte.io.oj.modules.stat.result.OjUserHomepageResult;
import github.jiangbyte.io.oj.modules.stat.service.OjUserHomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 门户用户主页公开统计实现。
 * <p>
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjUserHomepageServiceImpl implements OjUserHomepageService {

    private static final ZoneOffset CHINA = ZoneOffset.ofHours(8);
    private static final int RECENT_LIMIT = 20;
    private static final int HEATMAP_DAYS = 365;

    private final OjUserHomepageMapper ojUserHomepageMapper;

    @Override
    @ReadDataSource
    public OjUserHomepageResult homepage(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            throw new BizException(400, "account_id is required");
        }
        String id = accountId.trim();
        OjUserHomepageResult result = new OjUserHomepageResult();
        result.setAccountId(id);

        fillSolveProgress(result, id);
        List<OjUserHomepageResult.LanguageStatItem> languages = ojUserHomepageMapper.selectLanguageSolved(id);
        result.setLanguages(languages != null ? languages : List.of());

        OffsetDateTime since = LocalDate.now(CHINA)
                .minusDays(HEATMAP_DAYS - 1L)
                .atStartOfDay()
                .atOffset(CHINA)
                .withOffsetSameInstant(ZoneOffset.UTC);
        List<OjUserHomepageResult.HeatmapDayItem> dayRows = ojUserHomepageMapper.selectHeatmapDays(id, since);
        result.setHeatmap(buildHeatmap(dayRows != null ? dayRows : List.of()));

        List<OjUserHomepageResult.RecentAcceptedItem> recent = ojUserHomepageMapper.selectRecentAccepted(id, RECENT_LIMIT);
        result.setRecentAccepted(recent != null ? recent : List.of());
        return result;
    }

    private void fillSolveProgress(OjUserHomepageResult result, String accountId) {
        OjUserHomepageResult.SolveProgress solved = result.getSolved();
        Map<String, Long> totals = toDifficultyMap(ojUserHomepageMapper.selectPublishedByDifficulty());
        Map<String, Long> accepted = toDifficultyMap(ojUserHomepageMapper.selectAcceptedByDifficulty(accountId));

        applyBucket(solved.getEasy(), "EASY", totals, accepted);
        applyBucket(solved.getMedium(), "MEDIUM", totals, accepted);
        applyBucket(solved.getHard(), "HARD", totals, accepted);

        long acceptedTotal = accepted.values().stream().mapToLong(Long::longValue).sum();
        Long attempting = ojUserHomepageMapper.countAttempting(accountId);
        solved.setAccepted(acceptedTotal);
        solved.setAttempting(attempting == null ? 0L : attempting);
    }

    private static void applyBucket(
            OjUserHomepageResult.DifficultyBucket bucket,
            String difficulty,
            Map<String, Long> totals,
            Map<String, Long> accepted) {
        bucket.setTotal(totals.getOrDefault(difficulty, 0L));
        bucket.setSolved(accepted.getOrDefault(difficulty, 0L));
    }

    private static Map<String, Long> toDifficultyMap(List<OjDifficultyCountRow> rows) {
        Map<String, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (OjDifficultyCountRow row : rows) {
            if (row == null || !StringUtils.hasText(row.getDifficulty())) {
                continue;
            }
            map.put(row.getDifficulty().trim().toUpperCase(), row.getCount() == null ? 0L : row.getCount());
        }
        return map;
    }

    private OjUserHomepageResult.HeatmapStat buildHeatmap(List<OjUserHomepageResult.HeatmapDayItem> dayRows) {
        OjUserHomepageResult.HeatmapStat heatmap = new OjUserHomepageResult.HeatmapStat();
        Set<LocalDate> active = new HashSet<>();
        long total = 0L;
        for (OjUserHomepageResult.HeatmapDayItem row : dayRows) {
            if (row == null || !StringUtils.hasText(row.getDate())) {
                continue;
            }
            long count = Math.max(0L, row.getCount());
            row.setCount(count);
            total += count;
            if (count > 0) {
                try {
                    active.add(LocalDate.parse(row.getDate().trim()));
                } catch (Exception ignored) {
                    // skip malformed date
                }
            }
        }
        heatmap.setDays(dayRows);
        heatmap.setTotalSubmissions(total);
        heatmap.setActiveDays(active.size());
        heatmap.setMaxStreak(maxStreak(active));
        heatmap.setCurrentStreak(currentStreak(active, LocalDate.now(CHINA)));
        return heatmap;
    }

    private static long maxStreak(Set<LocalDate> active) {
        if (active.isEmpty()) {
            return 0L;
        }
        LocalDate start = active.stream().min(LocalDate::compareTo).orElse(null);
        LocalDate end = active.stream().max(LocalDate::compareTo).orElse(null);
        if (start == null || end == null) {
            return 0L;
        }
        long best = 0L;
        long run = 0L;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (active.contains(d)) {
                run += 1;
                best = Math.max(best, run);
            } else {
                run = 0L;
            }
        }
        return best;
    }

    private static long currentStreak(Set<LocalDate> active, LocalDate today) {
        if (active.isEmpty()) {
            return 0L;
        }
        LocalDate cursor = active.contains(today) ? today : today.minusDays(1);
        if (!active.contains(cursor)) {
            return 0L;
        }
        long streak = 0L;
        while (active.contains(cursor)) {
            streak += 1;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
