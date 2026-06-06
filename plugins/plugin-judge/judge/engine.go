package judge

import (
	"context"
	"log"
	"strings"

	"hei-gin/sdk/db"
	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/langconf"
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
	log.Printf("[judge] worker processing submission %s (judge_type=%s, contest_type=%s)",
		task.SubmissionID, task.JudgeType, task.ContestType)

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

// ---------- 比赛模式辅助 ----------

// shouldBreakOnFirstFail 判断是否应在遇到首个失败测试用例时立即终止
// ACM模式: 是. OI/IOI: 否（需要跑完所有用例以获取部分分）
func shouldBreakOnFirstFail(task *JudgeTask) bool {
	return task.ContestType == "ACM"
}

// ---------- 语言判断（委托 langconf 实现动态扩展） ----------

func isInterpretedLanguage(lang string) bool {
	return langconf.IsInterpreted(lang)
}

// compileAndGetBinary 编译用户代码并返回二进制 (解释型语言返回 nil)
func compileAndGetBinary(backend judgetypes.SandboxBackend, code, language string) (*judgetypes.ExecResult, []byte) {
	if isInterpretedLanguage(language) {
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	if code == "" {
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	result, err := backend.Exec(&judgetypes.ExecRequest{
		Code:        code,
		Language:    language,
		MaxCPUTime:  10000 * 1e6,
		MaxRealTime: 30000 * 1e6,
		MaxMemory:   512 * 1024 * 1024,
		MaxStack:    256 * 1024 * 1024,
		MaxOutput:   64 * 1024 * 1024,
	})
	if err != nil {
		return &judgetypes.ExecResult{Status: judgetypes.StatusSE, Error: err.Error()}, nil
	}
	if result.Status == judgetypes.StatusCompileError {
		return result, nil
	}
	return result, result.Binary
}

// compileCodeWithCheck 执行编译并检查编译结果, 返回编译结果和二进制.
// 如果编译失败, 直接更新提交状态为 CompileError 或 SE 并返回 false.
func compileCodeWithCheck(backend judgetypes.SandboxBackend, submissionID, code, language string) (*judgetypes.ExecResult, []byte, bool) {
	compileResult, binary := compileAndGetBinary(backend, code, language)
	if compileResult.Status == judgetypes.StatusCompileError {
		updateJudgeResult(submissionID, judgetypes.StatusCompileError, 0, 0, 0, compileResult.Stderr)
		return compileResult, nil, false
	}
	if compileResult.Status == judgetypes.StatusSE {
		updateJudgeResult(submissionID, judgetypes.StatusSE, 0, 0, 0, compileResult.Error)
		return compileResult, nil, false
	}
	return compileResult, binary, true
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

	_, binary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.Code, task.Language)
	if !ok {
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
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}

		if runResult.Status == judgetypes.StatusAccepted {
			if !compareOutput(runResult.Stdout, tc.Output) {
				runResult.Status = judgetypes.StatusWrongAnswer
			}
		}

		details = append(details, *runResult)

		// ACM 模式: 遇到首个非 AC 立即终止
		if shouldBreakOnFirstFail(task) && runResult.Status != judgetypes.StatusAccepted {
			break
		}
	}

	result := e.aggregateResult(details, testcases)
	e.updateSimple(task.SubmissionID, result.Status, result.Score, result.TimeUsed, result.MemoryUsed, result.Error)
}

// ---------- 输出比对 ----------

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

// ---------- 结果聚合 ----------

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
//
//	ACM: 首个非 AC 停止, 分数=通过测试用例分数之和
//	OI/IOI: 执行所有用例, 分数=通过测试用例分数之和
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

// ---------- 更新判题结果 ----------

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
