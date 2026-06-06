package judge

import (
	"log"
	"strings"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

const spjSeparator = "---SPLIT---"

// judgeSPJ SPJ 判题
// 编译用户代码和 SPJ 代码各一次, 缓存二进制, 对每个测试用例复用
// 支持比赛模式: ACM 模式下首个 WA/非AC 终止, OI/IOI 跑完全部用例
func (e *JudgeEngine) judgeSPJ(task *JudgeTask, testcases []testcase.JudgeTestcase) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	// 编译用户代码
	_, userBinary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.Code, task.Language)
	if !ok {
		return
	}

	// 编译 SPJ 代码
	_, spjBinary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.SpjCode, task.SpjLanguage)
	if !ok {
		return
	}

	totalScore := 0
	overallStatus := judgetypes.StatusAccepted
	maxTime := int64(0)
	maxMemory := int64(0)

	for _, tc := range testcases {
		// 运行用户程序
		userResult, err := runTestCase(backend, task, userBinary, tc.Input)
		if err != nil {
			e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
			return
		}

		if userResult.TimeUsed > maxTime {
			maxTime = userResult.TimeUsed
		}
		if userResult.MemoryUsed > maxMemory {
			maxMemory = userResult.MemoryUsed
		}

		if userResult.Status != judgetypes.StatusAccepted {
			if p, ok := statusPriority[userResult.Status]; ok && p < statusPriority[overallStatus] {
				overallStatus = userResult.Status
			}
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}

		// 运行 SPJ 验证用户输出
		spjInput := strings.TrimRight(tc.Input, "\n") + "\n" + spjSeparator + "\n" +
			strings.TrimRight(userResult.Stdout, "\n") + "\n" + spjSeparator + "\n" +
			strings.TrimRight(tc.Output, "\n") + "\n"

		spjResult, err := backend.Exec(&judgetypes.ExecRequest{
			Code:        task.SpjCode,
			Language:    task.SpjLanguage,
			Binary:      spjBinary,
			Stdin:       spjInput,
			MaxCPUTime:  5000 * 1e6,
			MaxRealTime: 10000 * 1e6,
			MaxMemory:   256 * 1024 * 1024,
		})
		if err != nil {
			e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
			return
		}

		if spjResult.ExitCode == 0 {
			totalScore += tc.Score
		} else if overallStatus == judgetypes.StatusAccepted {
			overallStatus = judgetypes.StatusWrongAnswer
			if shouldBreakOnFirstFail(task) {
				break
			}
		}
	}

	e.updateSimple(task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, "")
	log.Printf("[judge] SPJ result for submission %s: status=%s, score=%d",
		task.SubmissionID, overallStatus, totalScore)
}
