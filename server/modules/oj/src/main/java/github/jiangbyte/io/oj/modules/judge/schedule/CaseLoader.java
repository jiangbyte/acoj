package github.jiangbyte.io.oj.modules.judge.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.mapper.OjProblemCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 按题目测例版本加载 ENABLED 测例（INLINE / OBJECT）。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class CaseLoader {

    private final OjProblemCaseMapper ojProblemCaseMapper;
    private final CaseObjectReader caseObjectReader;

    /** 加载全部 ENABLED 测例（判题 Worker 整单提交使用）。 */
    public List<LoadedCase> load(String problemId, Integer caseVersion) {
        if (!StringUtils.hasText(problemId) || caseVersion == null) {
            throw new BizException("测例加载参数无效");
        }
        List<OjProblemCase> rows = ojProblemCaseMapper.selectList(
                Wrappers.<OjProblemCase>lambdaQuery()
                        .eq(OjProblemCase::getProblemId, problemId)
                        .eq(OjProblemCase::getCaseVersion, caseVersion)
                        .eq(OjProblemCase::getStatus, "ENABLED")
                        .orderByAsc(OjProblemCase::getSortNo)
                        .orderByAsc(OjProblemCase::getCaseKey));
        return mapRows(rows, true);
    }

    /** 分页加载 ENABLED 测例（试跑等场景，避免一次拉全量）。 */
    public List<LoadedCase> loadPage(String problemId, Integer caseVersion, long current, long size) {
        if (!StringUtils.hasText(problemId) || caseVersion == null) {
            throw new BizException("测例加载参数无效");
        }
        long pageNo = Math.max(1L, current);
        long pageSize = Math.max(1L, Math.min(size, 200L));
        Page<OjProblemCase> page = ojProblemCaseMapper.selectPage(
                new Page<>(pageNo, pageSize),
                Wrappers.<OjProblemCase>lambdaQuery()
                        .eq(OjProblemCase::getProblemId, problemId)
                        .eq(OjProblemCase::getCaseVersion, caseVersion)
                        .eq(OjProblemCase::getStatus, "ENABLED")
                        .orderByAsc(OjProblemCase::getSortNo)
                        .orderByAsc(OjProblemCase::getCaseKey));
        return mapRows(page.getRecords(), false);
    }

    /** 按测例号加载单条 ENABLED 测例。 */
    public List<LoadedCase> loadByKey(String problemId, Integer caseVersion, String caseKey) {
        if (!StringUtils.hasText(problemId) || caseVersion == null || !StringUtils.hasText(caseKey)) {
            throw new BizException("测例加载参数无效");
        }
        OjProblemCase row = ojProblemCaseMapper.selectOne(
                Wrappers.<OjProblemCase>lambdaQuery()
                        .eq(OjProblemCase::getProblemId, problemId)
                        .eq(OjProblemCase::getCaseVersion, caseVersion)
                        .eq(OjProblemCase::getCaseKey, caseKey.trim())
                        .eq(OjProblemCase::getStatus, "ENABLED")
                        .last("LIMIT 1"));
        if (row == null) {
            return List.of();
        }
        return mapRows(List.of(row), false);
    }

    private List<LoadedCase> mapRows(List<OjProblemCase> rows, boolean throwOnEmpty) {
        if (rows == null || rows.isEmpty()) {
            if (throwOnEmpty) {
                throw new BizException("题目无可用测例");
            }
            return List.of();
        }
        List<LoadedCase> result = new ArrayList<>(rows.size());
        for (OjProblemCase row : rows) {
            result.add(new LoadedCase(
                    row.getCaseKey(),
                    resolveInput(row),
                    resolveOutput(row),
                    Boolean.TRUE.equals(row.getIsSample()),
                    row.getSortNo()));
        }
        return result;
    }

    private String resolveInput(OjProblemCase row) {
        if ("OBJECT".equalsIgnoreCase(row.getInputStorage())) {
            return caseObjectReader.readText(row.getInputObjectKey(), row.getCaseKey(), "input");
        }
        return row.getInputText() == null ? "" : row.getInputText();
    }

    private String resolveOutput(OjProblemCase row) {
        if ("OBJECT".equalsIgnoreCase(row.getOutputStorage())) {
            return caseObjectReader.readText(row.getOutputObjectKey(), row.getCaseKey(), "output");
        }
        return row.getOutputText() == null ? "" : row.getOutputText();
    }

    /** 已加载测例（含期望输出，仅业务侧比对使用）。 */
    public record LoadedCase(
            String caseKey,
            String stdin,
            String expectedStdout,
            boolean sample,
            Integer sortNo) {
    }
}
