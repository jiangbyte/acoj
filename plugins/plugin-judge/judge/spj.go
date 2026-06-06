package judge

import (
	"log"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

func (e *JudgeEngine) judgeSPJ(task *JudgeTask, testcases []testcase.JudgeTestcase) {
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

	spjCompile, err := backend.Exec(&judgetypes.ExecRequest{
		Code:       task.SpjCode,
		Language:   task.SpjLanguage,
		MaxCPUTime: 10000 * 1e6,
		MaxMemory:  512 * 1024 * 1024,
	})
	if err != nil || spjCompile.Status == judgetypes.StatusCompileError {
		errMsg := ""
		if err != nil {
			errMsg = err.Error()
		} else {
			errMsg = spjCompile.Stderr
		}
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "SPJ编译失败: "+errMsg)
		return
	}

	totalScore := 0
	overallStatus := judgetypes.StatusAccepted
	maxTime := int64(0)
	maxMemory := int64(0)

	for _, tc := range testcases {
		userResult, err := backend.Exec(&judgetypes.ExecRequest{
			Code:        task.Code,
			Language:    task.Language,
			Stdin:       tc.Input,
			MaxCPUTime:  task.TimeLimit * 1e6,
			MaxRealTime: task.TimeLimit * 2 * 1e6,
			MaxMemory:   task.MemoryLimit * 1024,
			MaxStack:    task.StackLimit * 1024,
			MaxOutput:   task.OutputLimit * 1024,
		})
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

		spjInput := tc.Input + "\n---SPLIT---\n" + userResult.Stdout + "\n---SPLIT---\n" + tc.Output
		spjResult, err := backend.Exec(&judgetypes.ExecRequest{
			Code:        task.SpjCode,
			Language:    task.SpjLanguage,
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
		} else {
			overallStatus = judgetypes.StatusWrongAnswer
		}
	}

	if len(testcases) > 0 {
		totalScore = totalScore / len(testcases)
	}

	e.updateSimple(task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, "")
	log.Printf("[judge] SPJ result for submission %s: status=%s, score=%d", task.SubmissionID, overallStatus, totalScore)
}
