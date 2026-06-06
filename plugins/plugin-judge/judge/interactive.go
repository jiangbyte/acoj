package judge

import (
	"log"
	"strings"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

// judgeInteractive 交互式判题
// 顺序执行方案:
//  1. 编译用户代码一次 + 编译交互器代码一次
//  2. 对每个测试用例: 运行用户程序 → 运行交互器验证
//  支持比赛模式: ACM 模式下首个非 AC 终止, OI/IOI 跑完全部用例
func (e *JudgeEngine) judgeInteractive(task *JudgeTask, testcases []testcase.JudgeTestcase) {
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

	// 编译交互器代码
	interCompileResult, interactorBinary := compileAndGetBinary(backend, task.InteractiveCode, task.InteractiveLang)
	if interCompileResult.Status == judgetypes.StatusCompileError {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "交互器编译失败: "+interCompileResult.Stderr)
		return
	}
	if interCompileResult.Status == judgetypes.StatusSE {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "交互器编译失败: "+interCompileResult.Error)
		return
	}

	totalScore := 0
	overallStatus := judgetypes.StatusAccepted
	maxTime := int64(0)
	maxMemory := int64(0)

	for _, tc := range testcases {
		// 步骤 1: 运行用户程序
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

		// 步骤 2: 运行交互器验证
		interInput := strings.TrimRight(tc.Input, "\n") + "\n---USER_OUT---\n" +
			strings.TrimRight(userResult.Stdout, "\n") + "\n"

		interResult, err := backend.Exec(&judgetypes.ExecRequest{
			Code:        task.InteractiveCode,
			Language:    task.InteractiveLang,
			Binary:      interactorBinary,
			Stdin:       interInput,
			MaxCPUTime:  task.TimeLimit * 3 * 1e6,
			MaxRealTime: task.TimeLimit * 6 * 1e6,
			MaxMemory:   task.MemoryLimit * 1024,
			MaxStack:    task.StackLimit * 1024,
			MaxOutput:   task.OutputLimit * 1024,
		})
		if err != nil {
			e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
			return
		}
		if interResult.TimeUsed > maxTime {
			maxTime = interResult.TimeUsed
		}
		if interResult.MemoryUsed > maxMemory {
			maxMemory = interResult.MemoryUsed
		}
		if interResult.Status != judgetypes.StatusAccepted {
			if p, ok := statusPriority[interResult.Status]; ok && p < statusPriority[overallStatus] {
				overallStatus = interResult.Status
			}
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}
		if interResult.ExitCode == 0 {
			totalScore += tc.Score
		} else if overallStatus == judgetypes.StatusAccepted {
			overallStatus = judgetypes.StatusWrongAnswer
			if shouldBreakOnFirstFail(task) {
				break
			}
		}
	}

	e.updateSimple(task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, "")
	log.Printf("[judge] Interactive result for submission %s: status=%s, score=%d",
		task.SubmissionID, overallStatus, totalScore)
}
