package github.jiangbyte.io.oj.modules.stat.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 门户用户主页公开 OJ 统计。
 * <p>
 * Author: Charlie
 */
@Schema(description = "门户用户主页公开 OJ 统计")
@Data
public class OjUserHomepageResult {
    @Schema(description = "账户 ID")
    private String accountId;

    @Schema(description = "解题进度")
    private SolveProgress solved = new SolveProgress();

    @Schema(description = "语言通过题数（按语言去重题目）")
    private List<LanguageStatItem> languages = new ArrayList<>();

    @Schema(description = "近一年提交热力图")
    private HeatmapStat heatmap = new HeatmapStat();

    @Schema(description = "最近通过题目")
    private List<RecentAcceptedItem> recentAccepted = new ArrayList<>();

    @Data
    @Schema(description = "解题进度")
    public static class SolveProgress {
        @Schema(description = "已通过题数（已发布题）")
        private long accepted;
        @Schema(description = "尝试中题数（已发布且未通过）")
        private long attempting;
        @Schema(description = "简单")
        private DifficultyBucket easy = new DifficultyBucket();
        @Schema(description = "中等")
        private DifficultyBucket medium = new DifficultyBucket();
        @Schema(description = "困难")
        private DifficultyBucket hard = new DifficultyBucket();
    }

    @Data
    @Schema(description = "难度桶")
    public static class DifficultyBucket {
        @Schema(description = "已通过")
        private long solved;
        @Schema(description = "已发布总数")
        private long total;
    }

    @Data
    @Schema(description = "语言统计项")
    public static class LanguageStatItem {
        @Schema(description = "语言 key")
        private String language;
        @Schema(description = "通过题数")
        private long solvedCount;
    }

    @Data
    @Schema(description = "热力图统计")
    public static class HeatmapStat {
        @Schema(description = "近一年提交总次数")
        private long totalSubmissions;
        @Schema(description = "累计有提交的天数")
        private long activeDays;
        @Schema(description = "历史最长连续提交天数")
        private long maxStreak;
        @Schema(description = "当前连续提交天数（截至今天）")
        private long currentStreak;
        @Schema(description = "有提交的日期（稀疏）")
        private List<HeatmapDayItem> days = new ArrayList<>();
    }

    @Data
    @Schema(description = "热力图单日")
    public static class HeatmapDayItem {
        @Schema(description = "日期 YYYY-MM-DD（东八区）")
        private String date;
        @Schema(description = "当日提交次数")
        private long count;
    }

    @Data
    @Schema(description = "最近通过项")
    public static class RecentAcceptedItem {
        @Schema(description = "题目 ID")
        private String problemId;
        @Schema(description = "题号")
        private String problemKey;
        @Schema(description = "标题")
        private String title;
        @Schema(description = "难度")
        private String difficulty;
        @Schema(description = "首次通过时间")
        private String acceptedAt;
    }
}
