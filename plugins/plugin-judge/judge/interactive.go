package judge

import (
	"log"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

func (e *JudgeEngine) judgeInteractive(task *JudgeTask, testcases []testcase.JudgeTestcase) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	userCompile, err := backend.Exec(&judgetypes.ExecRequest{
		Code:       task.Code,
		Language:   task.Language,
		MaxCPUTime: 10000 * 1e6,
		MaxMemory:  512 * 1024 * 1024,
	})
	if err != nil || userCompile.Status == judgetypes.StatusCompileError {
		errMsg := ""
		if err != nil {
			errMsg = err.Error()
		} else {
			errMsg = userCompile.Stderr
		}
		e.updateSimple(task.SubmissionID, judgetypes.StatusCompileError, 0, 0, 0, errMsg)
		return
	}

	interCompile, err := backend.Exec(&judgetypes.ExecRequest{
		Code:       task.InteractiveCode,
		Language:   task.InteractiveLang,
		MaxCPUTime: 10000 * 1e6,
		MaxMemory:  512 * 1024 * 1024,
	})
	if err != nil || interCompile.Status == judgetypes.StatusCompileError {
		errMsg := ""
		if err != nil {
			errMsg = err.Error()
		} else {
			errMsg = interCompile.Stderr
		}
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "交互器编译失败: "+errMsg)
		return
	}

	totalScore := 0
	overallStatus := judgetypes.StatusAccepted
	maxTime := int64(0)
	maxMemory := int64(0)

	for _, tc := range testcases {
		interResult, err := backend.Exec(&judgetypes.ExecRequest{
			Code:        task.InteractiveCode,
			Language:    task.InteractiveLang,
			Stdin:       tc.Input + "\n---USER_OUTPUT---\n",
			MaxCPUTime:  task.TimeLimit * 3 * 1e6,
			MaxRealTime: task.TimeLimit * 6 * 1e6,
			MaxMemory:   task.MemoryLimit * 1024,
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

		if interResult.ExitCode == 0 {
			totalScore += tc.Score
		} else {
			overallStatus = judgetypes.StatusWrongAnswer
		}
	}

	if len(testcases) > 0 {
		totalScore = totalScore / len(testcases)
	}

	e.updateSimple(task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, "")
	log.Printf("[judge] Interactive result for submission %s: status=%s, score=%d", task.SubmissionID, overallStatus, totalScore)
}
