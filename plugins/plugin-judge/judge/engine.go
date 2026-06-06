package judge

import (
	"context"
	"log"

	"hei-gin/sdk/db"
	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

// JudgeEngine 判题引擎
type JudgeEngine struct {
	workerCount int
	taskCh      chan *JudgeTask
	stopCh      chan struct{}
}

// NewJudgeEngine 创建判题引擎
func NewJudgeEngine(workerCount int) *JudgeEngine {
	return &JudgeEngine{
		workerCount: workerCount,
		taskCh:      make(chan *JudgeTask, 100),
		stopCh:      make(chan struct{}),
	}
}

// Start 启动判题 Worker
func (e *JudgeEngine) Start() {
	for i := 0; i < e.workerCount; i++ {
		go e.worker(i)
	}
	log.Printf("[judge] started %d workers", e.workerCount)
}

// Stop 停止判题引擎
func (e *JudgeEngine) Stop() {
	close(e.stopCh)
}

// Submit 提交判题任务
func (e *JudgeEngine) Submit(task *JudgeTask) {
	e.taskCh <- task
}

func (e *JudgeEngine) worker(id int) {
	for {
		select {
		case task := <-e.taskCh:
			e.processTask(task)
		case <-e.stopCh:
			return
		}
	}
}

func (e *JudgeEngine) processTask(task *JudgeTask) {
	log.Printf("[judge] worker processing submission %s", task.SubmissionID)

	ctx := context.Background()
	var testcases []testcase.JudgeTestcase
	if err := db.DB.WithContext(ctx).
		Where("problem_id = ?", task.ProblemID).
		Order("`order` ASC, created_at ASC").
		Find(&testcases).Error; err != nil {
		log.Printf("[judge] fetch testcases error: %v", err)
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
		return
	}

	if len(testcases) == 0 {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "没有测试用例")
		return
	}

	switch task.JudgeType {
	case "spj":
		e.judgeSPJ(task, testcases)
	case "interactive":
		e.judgeInteractive(task, testcases)
	default:
		e.judgeDefault(task, testcases)
	}
}

func (e *JudgeEngine) judgeDefault(task *JudgeTask, testcases []testcase.JudgeTestcase) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	compileResult, err := backend.Exec(&judgetypes.ExecRequest{
		Code:        task.Code,
		Language:    task.Language,
		MaxCPUTime:  10000 * 1e6,
		MaxMemory:   512 * 1024 * 1024,
		MaxStack:    256 * 1024 * 1024,
		MaxOutput:   64 * 1024 * 1024,
	})
	if err != nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
		return
	}
	if compileResult.Status == judgetypes.StatusCompileError {
		e.updateSimple(task.SubmissionID, judgetypes.StatusCompileError, 0, 0, 0, compileResult.Stderr)
		return
	}

	var details []judgetypes.ExecResult
	for _, tc := range testcases {
		runResult, err := backend.Exec(&judgetypes.ExecRequest{
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
			details = append(details, judgetypes.ExecResult{
				Status: judgetypes.StatusSE,
				Error:  err.Error(),
			})
			continue
		}
		details = append(details, *runResult)
	}

	result := e.aggregateResult(details, testcases)
	e.updateSimple(task.SubmissionID, result.Status, result.Score, result.TimeUsed, result.MemoryUsed, result.Error)
}

func (e *JudgeEngine) aggregateResult(details []judgetypes.ExecResult, testcases []testcase.JudgeTestcase) JudgeResult {
	result := JudgeResult{
		Status:   judgetypes.StatusAccepted,
		Score:    0,
		TimeUsed: 0,
	}

	totalScore := 0
	for i, d := range details {
		if d.Status != judgetypes.StatusAccepted {
			result.Status = d.Status
		}
		if d.Status == judgetypes.StatusAccepted {
			if i < len(testcases) {
				totalScore += testcases[i].Score
			}
		}
		if d.TimeUsed > result.TimeUsed {
			result.TimeUsed = d.TimeUsed
		}
		if d.MemoryUsed > result.MemoryUsed {
			result.MemoryUsed = d.MemoryUsed
		}
	}

	if len(testcases) > 0 {
		result.Score = totalScore / len(testcases)
	} else {
		result.Score = 100
	}

	return result
}

func (e *JudgeEngine) updateSimple(submissionID, status string, score int, timeUsed, memoryUsed int64, errMsg string) {
	updateJudgeResult(submissionID, status, score, timeUsed, memoryUsed, errMsg)
}

func updateJudgeResult(submissionID, status string, score int, timeUsed, memoryUsed int64, errMsg string) {
	ctx := context.Background()
	db.DB.WithContext(ctx).Table("judge_submission").
		Where("id = ?", submissionID).
		Updates(map[string]any{
			"status":        status,
			"score":         score,
			"time_used":     timeUsed,
			"memory_used":   memoryUsed,
			"error_message": errMsg,
		})

	log.Printf("[judge] submission %s result: status=%s, score=%d, time=%d, memory=%d",
		submissionID, status, score, timeUsed/1e6, memoryUsed/1024)
}
