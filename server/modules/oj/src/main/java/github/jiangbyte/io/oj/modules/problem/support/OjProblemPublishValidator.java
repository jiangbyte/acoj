package github.jiangbyte.io.oj.modules.problem.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.mapper.OjProblemDryRunMapper;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.mapper.OjProblemCaseMapper;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.mapper.OjProblemSolutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 题目发布门禁：测例 + 参考答案 + PROBLEM 全测例 AC 试跑。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class OjProblemPublishValidator {

    private final OjProblemCaseMapper ojProblemCaseMapper;
    private final OjProblemSolutionMapper ojProblemSolutionMapper;
    private final OjProblemDryRunMapper ojProblemDryRunMapper;

    /** 校验题目可发布为 PUBLISHED。 */
    public void assertCanPublish(OjProblem problem) {
        if (problem == null || problem.getId() == null) {
            throw new BizException("题目无效");
        }
        Integer caseVersion = problem.getCaseVersion() == null ? 1 : problem.getCaseVersion();
        long caseCount = ojProblemCaseMapper.selectCount(Wrappers.<OjProblemCase>lambdaQuery()
                .eq(OjProblemCase::getProblemId, problem.getId())
                .eq(OjProblemCase::getCaseVersion, caseVersion)
                .eq(OjProblemCase::getStatus, "ENABLED"));
        if (caseCount < 1) {
            throw new BizException("发布前需至少 1 条启用测例");
        }
        long solutionCount = ojProblemSolutionMapper.selectCount(Wrappers.<OjProblemSolution>lambdaQuery()
                .eq(OjProblemSolution::getProblemId, problem.getId())
                .eq(OjProblemSolution::getStatus, "ENABLED"));
        if (solutionCount < 1) {
            throw new BizException("发布前需至少 1 条启用参考答案");
        }
        long dryRunOk = ojProblemDryRunMapper.selectCount(Wrappers.<OjProblemDryRun>lambdaQuery()
                .eq(OjProblemDryRun::getProblemId, problem.getId())
                .eq(OjProblemDryRun::getCaseVersion, caseVersion)
                .eq(OjProblemDryRun::getMode, "ALL")
                .eq(OjProblemDryRun::getLimitMode, "PROBLEM")
                .eq(OjProblemDryRun::getOverallStatus, "AC"));
        if (dryRunOk < 1) {
            throw new BizException("发布前需在当前测例版本下完成一次「全测例 + 题目限额」试跑且整单 AC");
        }
    }
}
