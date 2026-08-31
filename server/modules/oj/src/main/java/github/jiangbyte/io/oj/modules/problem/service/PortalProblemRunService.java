package github.jiangbyte.io.oj.modules.problem.service;

import github.jiangbyte.io.oj.modules.problem.param.OjProblemPortalRunParam;
import github.jiangbyte.io.oj.modules.problem.result.OjProblemPortalRunResult;

/**
 * 门户样例试跑（同步沙箱，不入提交表）。
 * <p>
 * Author: Charlie
 */
public interface PortalProblemRunService {

    /**
     * 对题面样例 / 自定义用例同步试跑。
     */
    OjProblemPortalRunResult run(OjProblemPortalRunParam param);
}
