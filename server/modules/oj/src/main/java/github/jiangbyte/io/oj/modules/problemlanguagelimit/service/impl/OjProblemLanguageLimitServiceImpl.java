package github.jiangbyte.io.oj.modules.problemlanguagelimit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.mapper.OjProblemLanguageLimitMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.param.OjProblemLanguageLimitItemParam;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * OJ 题目语言限额服务实现。
 * <p>
 * Author: Charlie
 */
@Service
public class OjProblemLanguageLimitServiceImpl
        extends ServiceImpl<OjProblemLanguageLimitMapper, OjProblemLanguageLimit>
        implements OjProblemLanguageLimitService {

    private static final long MIB = 1024L * 1024L;

    @Override
    @ReadDataSource
    public List<OjProblemLanguageLimit> listByProblemId(String problemId) {
        if (!StringUtils.hasText(problemId)) {
            return List.of();
        }
        return this.list(Wrappers.<OjProblemLanguageLimit>lambdaQuery()
                .eq(OjProblemLanguageLimit::getProblemId, problemId)
                .orderByAsc(OjProblemLanguageLimit::getLanguage));
    }

    @Override
    @ReadDataSource
    public OjProblemLanguageLimit findByProblemAndLanguage(String problemId, String language) {
        if (!StringUtils.hasText(problemId) || !StringUtils.hasText(language)) {
            return null;
        }
        String want = language.trim();
        List<OjProblemLanguageLimit> rows = listByProblemId(problemId);
        for (OjProblemLanguageLimit row : rows) {
            if (row.getLanguage() != null && row.getLanguage().equalsIgnoreCase(want)) {
                return row;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void replaceAll(String problemId, List<OjProblemLanguageLimitItemParam> items) {
        if (!StringUtils.hasText(problemId)) {
            throw new BizException(400, "题目无效");
        }
        if (items == null || items.isEmpty()) {
            throw new BizException(400, "至少配置 1 种语言限额");
        }
        List<OjProblemLanguageLimit> toSave = new ArrayList<>(items.size());
        Set<String> seen = new HashSet<>();
        for (OjProblemLanguageLimitItemParam item : items) {
            if (item == null || !StringUtils.hasText(item.getLanguage())) {
                throw new BizException(400, "语言不能为空");
            }
            String lang = item.getLanguage().trim();
            String key = lang.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new BizException(400, "语言限额重复: " + lang);
            }
            if (item.getTimeLimitMs() == null || item.getTimeLimitMs() < 1) {
                throw new BizException(400, "时限无效: " + lang);
            }
            if (item.getMemoryLimitBytes() == null || item.getMemoryLimitBytes() < MIB) {
                throw new BizException(400, "内存限额无效（至少 1MiB）: " + lang);
            }
            OjProblemLanguageLimit row = new OjProblemLanguageLimit();
            row.setProblemId(problemId);
            row.setLanguage(lang);
            row.setTimeLimitMs(item.getTimeLimitMs());
            row.setMemoryLimitBytes(item.getMemoryLimitBytes());
            row.setStackLimitBytes(item.getStackLimitBytes());
            row.setOutputLimitBytes(item.getOutputLimitBytes());
            row.setExtra(new HashMap<>());
            toSave.add(row);
        }
        this.remove(Wrappers.<OjProblemLanguageLimit>lambdaQuery()
                .eq(OjProblemLanguageLimit::getProblemId, problemId));
        this.saveBatch(toSave);
    }

    @Override
    @Transactional
    public void deleteByProblemIds(Collection<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return;
        }
        List<String> ids = problemIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        for (int i = 0; i < ids.size(); i += 500) {
            List<String> batch = ids.subList(i, Math.min(i + 500, ids.size()));
            this.remove(Wrappers.<OjProblemLanguageLimit>lambdaQuery()
                    .in(OjProblemLanguageLimit::getProblemId, batch));
        }
    }

    @Override
    @ReadDataSource
    public void fillLanguageLimits(OjProblem problem) {
        if (problem == null) {
            return;
        }
        fillLanguageLimits(List.of(problem));
    }

    @Override
    @ReadDataSource
    public void fillLanguageLimits(List<OjProblem> problems) {
        if (problems == null || problems.isEmpty()) {
            return;
        }
        for (OjProblem problem : problems) {
            if (problem != null) {
                problem.setLanguageLimits(List.of());
            }
        }
        List<String> problemIds = problems.stream()
                .filter(p -> p != null && StringUtils.hasText(p.getId()))
                .map(OjProblem::getId)
                .distinct()
                .toList();
        if (problemIds.isEmpty()) {
            return;
        }
        Map<String, List<OjProblemLanguageLimit>> byProblem = new HashMap<>();
        for (int i = 0; i < problemIds.size(); i += 500) {
            List<String> batch = problemIds.subList(i, Math.min(i + 500, problemIds.size()));
            List<OjProblemLanguageLimit> rows = this.list(Wrappers.<OjProblemLanguageLimit>lambdaQuery()
                    .in(OjProblemLanguageLimit::getProblemId, batch)
                    .orderByAsc(OjProblemLanguageLimit::getLanguage));
            for (OjProblemLanguageLimit row : rows) {
                byProblem.computeIfAbsent(row.getProblemId(), k -> new ArrayList<>()).add(row);
            }
        }
        for (OjProblem problem : problems) {
            if (problem == null || !StringUtils.hasText(problem.getId())) {
                continue;
            }
            problem.setLanguageLimits(byProblem.getOrDefault(problem.getId(), List.of()));
        }
    }
}
