package github.jiangbyte.io.oj.modules.judge.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.enums.OjCaseStorage;
import github.jiangbyte.io.oj.modules.problemcase.enums.OjEnableStatus;
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

    /**
     * 加载全部 ENABLED 测例（判题 Worker 整单提交使用）。
     * 边界：空结果抛错；INLINE/OBJECT 在 mapRows 内解析。
     */
    public List<LoadedCase> load(String problemId, Integer caseVersion) {
        // 1. 参数校验
        if (!StringUtils.hasText(problemId) || caseVersion == null) {
            throw new BizException("测例加载参数无效");
        }
        // 2. 按版本拉取全部启用测例，稳定排序
        List<OjProblemCase> rows = ojProblemCaseMapper.selectList(
                Wrappers.<OjProblemCase>lambdaQuery()
                        .eq(OjProblemCase::getProblemId, problemId)
                        .eq(OjProblemCase::getCaseVersion, caseVersion)
                        .eq(OjProblemCase::getStatus, OjEnableStatus.ENABLED.name())
                        .orderByAsc(OjProblemCase::getSortNo)
                        .orderByAsc(OjProblemCase::getCaseKey));
        // 3. 映射为 LoadedCase；整单判题无测例则失败
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
                        .eq(OjProblemCase::getStatus, OjEnableStatus.ENABLED.name())
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
                        .eq(OjProblemCase::getStatus, OjEnableStatus.ENABLED.name())
                        .last("LIMIT 1"));
        if (row == null) {
            return List.of();
        }
        return mapRows(List.of(row), false);
    }

    /**
     * 将 DB 行转为 LoadedCase：按 INLINE/OBJECT 解析入/出，带分值。
     * {@code throwOnEmpty}：整单判题要求至少一测；分页/按 key 允许空。
     */
    private List<LoadedCase> mapRows(List<OjProblemCase> rows, boolean throwOnEmpty) {
        // 1. 空列表：整单判题抛错，分页场景返回空
        if (rows == null || rows.isEmpty()) {
            if (throwOnEmpty) {
                throw new BizException("题目无可用测例");
            }
            return List.of();
        }
        // 2. 逐行解析输入/期望输出（OBJECT 走对象存储）与分值
        List<LoadedCase> result = new ArrayList<>(rows.size());
        for (OjProblemCase row : rows) {
            int score = row.getScore() == null ? 0 : Math.max(0, row.getScore());
            result.add(new LoadedCase(
                    row.getCaseKey(),
                    resolveInput(row),
                    resolveOutput(row),
                    Boolean.TRUE.equals(row.getIsSample()),
                    row.getSortNo(),
                    score));
        }
        return result;
    }

    private String resolveInput(OjProblemCase row) {
        if (OjCaseStorage.OBJECT.matches(row.getInputStorage())) {
            return caseObjectReader.readText(row.getInputObjectKey(), row.getCaseKey(), "input");
        }
        return row.getInputText() == null ? "" : row.getInputText();
    }

    private String resolveOutput(OjProblemCase row) {
        if (OjCaseStorage.OBJECT.matches(row.getOutputStorage())) {
            return caseObjectReader.readText(row.getOutputObjectKey(), row.getCaseKey(), "output");
        }
        return row.getOutputText() == null ? "" : row.getOutputText();
    }

    /**
     * 已加载测例（含期望输出与分值，仅业务侧比对 / 计分使用）。
     */
    public record LoadedCase(
            String caseKey,
            String stdin,
            String expectedStdout,
            boolean sample,
            Integer sortNo,
            int score) {
        public LoadedCase(
                String caseKey,
                String stdin,
                String expectedStdout,
                boolean sample,
                Integer sortNo) {
            this(caseKey, stdin, expectedStdout, sample, sortNo, 0);
        }
    }
}
