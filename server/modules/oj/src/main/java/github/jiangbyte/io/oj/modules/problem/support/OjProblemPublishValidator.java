package github.jiangbyte.io.oj.modules.problem.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.mapper.OjProblemLanguageLimitMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import github.jiangbyte.io.oj.modules.problemdryrun.enums.OjDryRunLimitMode;
import github.jiangbyte.io.oj.modules.problemdryrun.enums.OjDryRunMode;
import github.jiangbyte.io.oj.modules.problemdryrun.mapper.OjProblemDryRunMapper;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.enums.OjEnableStatus;
import github.jiangbyte.io.oj.modules.problemcase.mapper.OjProblemCaseMapper;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.mapper.OjProblemSolutionMapper;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 题目发布门禁：测例 + 语言限额 + 参考答案 + PROBLEM 全测例 AC 试跑。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class OjProblemPublishValidator {

    private final OjProblemCaseMapper ojProblemCaseMapper;
    private final OjProblemSolutionMapper ojProblemSolutionMapper;
    private final OjProblemDryRunMapper ojProblemDryRunMapper;
    private final OjProblemLanguageLimitMapper ojProblemLanguageLimitMapper;

    /**
     * 校验题目可发布为 PUBLISHED。
     * 边界：测例 + 语言限额 + 启用参考答案 + 当前版本「全测例+题目限额」AC 试跑；任一项失败即拒绝。
     */
    public void assertCanPublish(OjProblem problem) {
        // 1. 题目实体有效
        if (problem == null || problem.getId() == null) {
            throw new BizException("题目无效");
        }
        Integer caseVersion = problem.getCaseVersion() == null ? 1 : problem.getCaseVersion();
        // 2. 当前测例版本至少 1 条启用测例
        long caseCount = ojProblemCaseMapper.selectCount(Wrappers.<OjProblemCase>lambdaQuery()
                .eq(OjProblemCase::getProblemId, problem.getId())
                .eq(OjProblemCase::getCaseVersion, caseVersion)
                .eq(OjProblemCase::getStatus, OjEnableStatus.ENABLED.name()));
        if (caseCount < 1) {
            throw new BizException("发布前需至少 1 条启用测例");
        }
        // 3. 至少 1 条语言限额（即允许语言）
        long limitCount = ojProblemLanguageLimitMapper.selectCount(Wrappers.<OjProblemLanguageLimit>lambdaQuery()
                .eq(OjProblemLanguageLimit::getProblemId, problem.getId()));
        if (limitCount < 1) {
            throw new BizException("发布前需至少 1 条语言限额");
        }
        // 4. 至少 1 条启用参考答案
        long solutionCount = ojProblemSolutionMapper.selectCount(Wrappers.<OjProblemSolution>lambdaQuery()
                .eq(OjProblemSolution::getProblemId, problem.getId())
                .eq(OjProblemSolution::getStatus, OjEnableStatus.ENABLED.name()));
        if (solutionCount < 1) {
            throw new BizException("发布前需至少 1 条启用参考答案");
        }
        // 5. 当前版本须有一次 ALL + PROBLEM 限额且整单 AC 的试跑
        long dryRunOk = ojProblemDryRunMapper.selectCount(Wrappers.<OjProblemDryRun>lambdaQuery()
                .eq(OjProblemDryRun::getProblemId, problem.getId())
                .eq(OjProblemDryRun::getCaseVersion, caseVersion)
                .eq(OjProblemDryRun::getMode, OjDryRunMode.ALL.name())
                .eq(OjProblemDryRun::getLimitMode, OjDryRunLimitMode.PROBLEM.name())
                .eq(OjProblemDryRun::getOverallStatus, OjVerdict.AC.name()));
        if (dryRunOk < 1) {
            throw new BizException("发布前需在当前测例版本下完成一次「全测例 + 题目限额」试跑且整单 AC");
        }
    }
}
