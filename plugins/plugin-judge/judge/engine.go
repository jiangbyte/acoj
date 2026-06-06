package judge

import (
	"context"
	"log"
	"strings"

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

func NewJudgeEngine(workerCount int) *JudgeEngine {
	return &JudgeEngine{
		workerCount: workerCount,
		taskCh:      make(chan *JudgeTask, 100),
		stopCh:      make(chan struct{}),
	}
}

func (e *JudgeEngine) Start() {
	for i := 0; i < e.workerCount; i++ {
		go e.worker(i)
	}
	log.Printf("[judge] started %d workers", e.workerCount)
}

func (e *JudgeEngine) Stop() {
	close(e.stopCh)
}

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

	updateJudgeResult(task.SubmissionID, "JUDGING", 0, 0, 0, "")

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

// isInterpretedLanguage 判断是否为解释型语言
func isInterpretedLanguage(lang string) bool {
	switch lang {
	case "python", "python3", "bash", "sh", "javascript", "js", "node":
		return true
	}
	return false
}

// compileAndGetBinary 编译用户代码并返回二进制 (解释型语言返回 nil)
func compileAndGetBinary(backend judgetypes.SandboxBackend, code, language string) (*judgetypes.ExecResult, []byte) {
	if isInterpretedLanguage(language) {
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	if code == "" {
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	// 执行一次编译+运行 (无 stdin), 获取二进制
	result, err := backend.Exec(&judgetypes.ExecRequest{
		Code:        code,
		Language:    language,
		MaxCPUTime:  10000 * 1e6,        // 10s ns
		MaxRealTime: 30000 * 1e6,        // 30s ns
		MaxMemory:   512 * 1024 * 1024,  // 512MB
		MaxStack:    256 * 1024 * 1024,  // 256MB
		MaxOutput:   64 * 1024 * 1024,   // 64MB
	})
	if err != nil {
		return &judgetypes.ExecResult{Status: judgetypes.StatusSE, Error: err.Error()}, nil
	}
	if result.Status == judgetypes.StatusCompileError {
		return result, nil
	}
	// 编译成功, 缓存二进制
	return result, result.Binary
}

// runTestCase 运行单个测试用例 (使用缓存的二进制)
func runTestCase(backend judgetypes.SandboxBackend, task *JudgeTask, binary []byte, input string) (*judgetypes.ExecResult, error) {
	req := &judgetypes.ExecRequest{
		Language:    task.Language,
		Stdin:       input,
		MaxCPUTime:  task.TimeLimit * 1e6,
		MaxRealTime: task.TimeLimit * 2 * 1e6,
		MaxMemory:   task.MemoryLimit * 1024,
		MaxStack:    task.StackLimit * 1024,
		MaxOutput:   task.OutputLimit * 1024,
	}
	if len(binary) > 0 {
		req.Binary = binary
	} else {
		req.Code = task.Code
	}
	return backend.Exec(req)
}

// ---------- 标准判题 ----------

func (e *JudgeEngine) judgeDefault(task *JudgeTask, testcases []testcase.JudgeTestcase) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	// 编译一次, 缓存二进制
	compileResult, binary := compileAndGetBinary(backend, task.Code, task.Language)
	if compileResult.Status == judgetypes.StatusCompileError {
		e.updateSimple(task.SubmissionID, judgetypes.StatusCompileError, 0, 0, 0, compileResult.Stderr)
		return
	}
	if compileResult.Status == judgetypes.StatusSE {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, compileResult.Error)
		return
	}

	var details []judgetypes.ExecResult
	for _, tc := range testcases {
		runResult, err := runTestCase(backend, task, binary, tc.Input)
		if err != nil {
			details = append(details, judgetypes.ExecResult{
				Status: judgetypes.StatusSE,
				Error:  err.Error(),
			})
			continue
		}

		if runResult.Status == judgetypes.StatusAccepted {
			if !compareOutput(runResult.Stdout, tc.Output) {
				runResult.Status = judgetypes.StatusWrongAnswer
			}
		}

		details = append(details, *runResult)
	}

	result := e.aggregateResult(details, testcases)
	e.updateSimple(task.SubmissionID, result.Status, result.Score, result.TimeUsed, result.MemoryUsed, result.Error)
}

// compareOutput 比对用户输出与标准答案（忽略行尾空白和末尾空行差异）
func compareOutput(userOutput, expectedOutput string) bool {
	userLines := strings.Split(strings.TrimRight(userOutput, "\n"), "\n")
	expLines := strings.Split(strings.TrimRight(expectedOutput, "\n"), "\n")

	if len(userLines) != len(expLines) {
		return false
	}

	for i := 0; i < len(userLines); i++ {
		if strings.TrimRight(userLines[i], " \t\r\n") != strings.TrimRight(expLines[i], " \t\r\n") {
			return false
		}
	}
	return true
}

// statusPriority 判题结果优先级（数值越小优先级越高）
var statusPriority = map[string]int{
	judgetypes.StatusSE:          0,
	judgetypes.StatusMLE:         1,
	judgetypes.StatusTLE:         2,
	judgetypes.StatusRE:          3,
	judgetypes.StatusWrongAnswer: 4,
	judgetypes.StatusAccepted:    5,
}

// aggregateResult 聚合所有测试用例的判题结果
// 分数为通过测试用例的分数之和
func (e *JudgeEngine) aggregateResult(details []judgetypes.ExecResult, testcases []testcase.JudgeTestcase) JudgeResult {
	result := JudgeResult{
		Status:   judgetypes.StatusAccepted,
		Score:    0,
		TimeUsed: 0,
	}

	for i, d := range details {
		if p, ok := statusPriority[d.Status]; ok && p < statusPriority[result.Status] {
			result.Status = d.Status
		} else if !ok {
			result.Status = judgetypes.StatusSE
		}

		if d.Status == judgetypes.StatusAccepted {
			if i < len(testcases) {
				result.Score += testcases[i].Score
			}
		}

		if d.TimeUsed > result.TimeUsed {
			result.TimeUsed = d.TimeUsed
		}
		if d.MemoryUsed > result.MemoryUsed {
			result.MemoryUsed = d.MemoryUsed
		}
	}

	return result
}

func (e *JudgeEngine) updateSimple(submissionID, status string, score int, timeUsed, memoryUsed int64, errMsg string) {
	updateJudgeResult(submissionID, status, score, timeUsed, memoryUsed, errMsg)
}

func updateJudgeResult(submissionID, status string, score int, timeUsed, memoryUsed int64, errMsg string) {
	db.DB.Table("judge_submission").
		Where("id = ?", submissionID).
		Updates(map[string]any{
			"status":        status,
			"score":         score,
			"time_used":     timeUsed,
			"memory_used":   memoryUsed,
			"error_message": errMsg,
		})

	log.Printf("[judge] submission %s result: status=%s, score=%d, time=%dns, memory=%dB",
		submissionID, status, score, timeUsed, memoryUsed)
}

func isCompileError(result *judgetypes.ExecResult) bool {
	if result == nil {
		return false
	}
	if result.Status == judgetypes.StatusCompileError {
		return true
	}
	return result.Status == judgetypes.StatusRE && result.Stderr != ""
}
