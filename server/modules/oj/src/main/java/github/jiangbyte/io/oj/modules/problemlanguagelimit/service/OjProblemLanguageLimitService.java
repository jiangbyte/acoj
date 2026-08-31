package github.jiangbyte.io.oj.modules.problemlanguagelimit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.param.OjProblemLanguageLimitItemParam;

import java.util.Collection;
import java.util.List;

/**
 * OJ 题目语言限额服务。
 * <p>
 * Author: Charlie
 */
public interface OjProblemLanguageLimitService extends IService<OjProblemLanguageLimit> {

    /**
     * 题目下全部语言限额。
     */
    List<OjProblemLanguageLimit> listByProblemId(String problemId);

    /**
     * 按题目 + 语言查询（大小写不敏感）。
     */
    OjProblemLanguageLimit findByProblemAndLanguage(String problemId, String language);

    /**
     * 全量替换题目语言限额。
     */
    void replaceAll(String problemId, List<OjProblemLanguageLimitItemParam> items);

    /**
     * 批量删除题目下限额。
     */
    void deleteByProblemIds(Collection<String> problemIds);

    /**
     * 填充题目 languageLimits。
     */
    void fillLanguageLimits(OjProblem problem);

    /**
     * 批量填充。
     */
    void fillLanguageLimits(List<OjProblem> problems);
}
