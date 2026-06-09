package judge

import (
	"context"
	"log"
	"strings"

	"hei-gin/sdk/db"
	"hei-gin/plugins/plugin-judge/judgetypes"
	"gorm.io/gorm"
	"hei-gin/plugins/plugin-judge/langconf"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/testcase"
)

// detailWriter 判题详情写入回调（由外部注入，避免循环导入）
var detailWriter func(submissionID, problemID string, details []testcase.TestCaseResult)

// SetDetailWriter 设置判题详情写入回调
func SetDetailWriter(w func(submissionID, problemID string, details []testcase.TestCaseResult)) {
	detailWriter = w
}

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

	// 批量加载测试用例文件内容（文件存储 or DB 兼容）
	inCache, outCache, err := testcase.BatchLoadFiles(task.ProblemID, testcases)
	if err != nil {
		log.Printf("[judge] load testcase files error: %v", err)
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, err.Error())
		return
	}

	// 解析严格比对模式（题目级，测试用例级可覆盖）
	strictCompare := task.StrictCompare

	switch task.JudgeType {
	case "spj":
		e.judgeSPJ(task, testcases, inCache, outCache, strictCompare)
	case "interactive":
		e.judgeInteractive(task, testcases, inCache, outCache, strictCompare)
	default:
		e.judgeDefault(task, testcases, inCache, outCache, strictCompare)
	}
}

// ---------- 比赛模式辅助 ----------

func shouldBreakOnFirstFail(task *JudgeTask) bool {
	return task.ContestType == "ACM"
}

// ---------- 语言判断 ----------

func isInterpretedLanguage(lang string) bool {
	return langconf.IsInterpreted(lang)
}

// ---------- 编译 ----------

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

// ---------- 输出比对 ----------

// compareOutput 比对用户输出与标准答案
// strictCompare=true: 逐字节精确比对
// strictCompare=false: 忽略行尾空白和末尾空行差异（宽容模式）
// 返回 (是否通过, 是否格式错误) — 格式错误指宽容比对通过但严格比对不通过
func compareOutput(userOutput, expectedOutput string, strictCompare bool) (passed bool, isPE bool) {
	if strictCompare {
		passed = userOutput == expectedOutput
		return passed, false // 严格模式下无PE概念
	}

	// 宽容比对: 先检查严格是否一致（用于PE检测）
	trimUser := judgetypes.NormalizeOutput(userOutput, false)
	trimExp := judgetypes.NormalizeOutput(expectedOutput, false)

	if trimUser == trimExp {
		return true, false
	}

	// 内容一致但格式不同 → PE
	normUser := judgetypes.NormalizeOutput(userOutput, true)
	normExp := judgetypes.NormalizeOutput(expectedOutput, true)
	if normUser == normExp {
		return false, true
	}

	return false, false
}

// ---------- 标准判题 ----------

func (e *JudgeEngine) judgeDefault(task *JudgeTask, testcases []testcase.JudgeTestcase, inCache, outCache map[string][]byte, strictCompare bool) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	_, binary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.Code, task.Language)
	if !ok {
		return
	}

	var details []testcase.TestCaseResult
	for _, tc := range testcases {
		runResult, err := runTestCase(backend, task, binary, getTestCaseInput(tc, inCache))
		if err != nil {
			details = append(details, testcase.TestCaseResult{
				Status:   judgetypes.StatusSE,
				Error: err.Error(),
			})
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}

		// 沙箱层已上报的运行时状态
		tcStatus := runResult.Status

		// 运行时正常 → 比对输出
		if tcStatus == judgetypes.StatusAccepted {
			passed, isPE := compareOutput(runResult.Stdout, getTestCaseOutput(tc, outCache), strictCompare || tc.StrictCompare)
			if passed {
				tcStatus = judgetypes.StatusAccepted
			} else if isPE {
				tcStatus = judgetypes.StatusPE
			} else {
				tcStatus = judgetypes.StatusWrongAnswer
			}
		}

		details = append(details, testcase.TestCaseResult{
			TestCaseID: tc.ID,
			GroupID:    tc.GroupID,
			Order:      tc.Order,
			Status:     tcStatus,
			TimeUsed:   runResult.TimeUsed,
			MemoryUsed: runResult.MemoryUsed,
			Stderr:     runResult.Stderr,
		})

		if shouldBreakOnFirstFail(task) && tcStatus != judgetypes.StatusAccepted {
			break
		}
	}

	e.finalize(task, details, testcases)
}

// ---------- SPJ 判题 ----------

func (e *JudgeEngine) judgeSPJ(task *JudgeTask, testcases []testcase.JudgeTestcase, inCache, outCache map[string][]byte, strictCompare bool) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	_, userBinary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.Code, task.Language)
	if !ok {
		return
	}

	_, spjBinary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.SpjCode, task.SpjLanguage)
	if !ok {
		return
	}

	var details []testcase.TestCaseResult
	for _, tc := range testcases {
		userResult, err := runTestCase(backend, task, userBinary, getTestCaseInput(tc, inCache))
		if err != nil {
			details = append(details, testcase.TestCaseResult{
				TestCaseID: tc.ID,
				GroupID:    tc.GroupID,
				Order:      tc.Order,
				Status:     judgetypes.StatusSE,
				Error:      err.Error(),
			})
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}

		tcStatus := userResult.Status

		if tcStatus == judgetypes.StatusAccepted {
			// 运行 SPJ 验证用户输出
			spjInput := strings.TrimRight(getTestCaseInput(tc, inCache), "\n") + "\n---SPLIT---\n" +
				strings.TrimRight(userResult.Stdout, "\n") + "\n---SPLIT---\n" +
				strings.TrimRight(getTestCaseOutput(tc, outCache), "\n") + "\n"

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
				details = append(details, testcase.TestCaseResult{
					TestCaseID: tc.ID,
					GroupID:    tc.GroupID,
					Order:      tc.Order,
					Status:     judgetypes.StatusSE,
					Error:      err.Error(),
				})
				if shouldBreakOnFirstFail(task) {
					break
				}
				continue
			}

			if spjResult.ExitCode == 0 {
				tcStatus = judgetypes.StatusAccepted
			} else {
				tcStatus = judgetypes.StatusWrongAnswer
			}
		}

		details = append(details, testcase.TestCaseResult{
			TestCaseID: tc.ID,
			GroupID:    tc.GroupID,
			Order:      tc.Order,
			Status:     tcStatus,
			TimeUsed:   userResult.TimeUsed,
			MemoryUsed: userResult.MemoryUsed,
		})

		if shouldBreakOnFirstFail(task) && tcStatus != judgetypes.StatusAccepted {
			break
		}
	}

	e.finalize(task, details, testcases)
}

// ---------- 交互判题 ----------

func (e *JudgeEngine) judgeInteractive(task *JudgeTask, testcases []testcase.JudgeTestcase, inCache, outCache map[string][]byte, strictCompare bool) {
	backend := sandbox.DefaultPool.Get()
	if backend == nil {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "无可用判题节点")
		return
	}

	_, userBinary, ok := compileCodeWithCheck(backend, task.SubmissionID, task.Code, task.Language)
	if !ok {
		return
	}

	interCompileResult, interactorBinary := compileAndGetBinary(backend, task.InteractiveCode, task.InteractiveLang)
	if interCompileResult.Status == judgetypes.StatusCompileError {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "交互器编译失败: "+interCompileResult.Stderr)
		return
	}
	if interCompileResult.Status == judgetypes.StatusSE {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "交互器编译失败: "+interCompileResult.Error)
		return
	}

	var details []testcase.TestCaseResult
	for _, tc := range testcases {
		userResult, err := runTestCase(backend, task, userBinary, getTestCaseInput(tc, inCache))
		if err != nil {
			details = append(details, testcase.TestCaseResult{
				TestCaseID: tc.ID,
				GroupID:    tc.GroupID,
				Order:      tc.Order,
				Status:     judgetypes.StatusSE,
				Error:      err.Error(),
			})
			if shouldBreakOnFirstFail(task) {
				break
			}
			continue
		}

		tcStatus := userResult.Status

		if tcStatus == judgetypes.StatusAccepted {
			interInput := strings.TrimRight(getTestCaseInput(tc, inCache), "\n") + "\n---USER_OUT---\n" +
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
				details = append(details, testcase.TestCaseResult{
					TestCaseID: tc.ID,
					GroupID:    tc.GroupID,
					Order:      tc.Order,
					Status:     judgetypes.StatusSE,
					Error:      err.Error(),
				})
				if shouldBreakOnFirstFail(task) {
					break
				}
				continue
			}

			if interResult.Status != judgetypes.StatusAccepted {
				tcStatus = interResult.Status
			} else if interResult.ExitCode == 0 {
				tcStatus = judgetypes.StatusAccepted
			} else {
				tcStatus = judgetypes.StatusWrongAnswer
			}
		}

		details = append(details, testcase.TestCaseResult{
			TestCaseID: tc.ID,
			GroupID:    tc.GroupID,
			Order:      tc.Order,
			Status:     tcStatus,
			TimeUsed:   userResult.TimeUsed,
			MemoryUsed: userResult.MemoryUsed,
		})

		if shouldBreakOnFirstFail(task) && tcStatus != judgetypes.StatusAccepted {
			break
		}
	}

	e.finalize(task, details, testcases)
}

// ---------- 结果聚合与持久化 ----------

// finalize 聚合所有测试用例结果、写入详情、更新提交记录
func (e *JudgeEngine) finalize(task *JudgeTask, details []testcase.TestCaseResult, testcases []testcase.JudgeTestcase) {
	if len(details) == 0 {
		e.updateSimple(task.SubmissionID, judgetypes.StatusSE, 0, 0, 0, "没有判题结果")
		return
	}

	// 按子任务分组计算得分
	groups := testcase.GroupBySubtaskForResult(details)

	// 计算总分和最终状态
	totalScore := 0
	overallStatus := judgetypes.StatusAccepted
	maxTime := int64(0)
	maxMemory := int64(0)

	for _, g := range groups {
		groupAllAC := true
		for _, d := range g.Testcases {
			if judgetypes.IsWorseThan(d.Status, overallStatus) {
				overallStatus = d.Status
			}
			if d.TimeUsed > maxTime {
				maxTime = d.TimeUsed
			}
			if d.MemoryUsed > maxMemory {
				maxMemory = d.MemoryUsed
			}
			if d.Status != judgetypes.StatusAccepted {
				groupAllAC = false
			}
		}
		// 子任务捆绑: 同组全部 AC 才给该组总分
		if groupAllAC {
			totalScore += g.Score
		}
	}

	// 写 JudgeSubmissionDetail
	e.writeDetails(task.SubmissionID, task.ProblemID, details)

	// 更新提交记录
	e.updateSimple(task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, "")
	log.Printf("[judge] submission %s result: status=%s, score=%d, time=%dns, memory=%dB, details=%d",
		task.SubmissionID, overallStatus, totalScore, maxTime, maxMemory, len(details))

	// 更新题目统计（仅非测试提交）
	if task.SubmissionType != "test" {
		db.DB.Table("judge_problem").Where("id = ?", task.ProblemID).
			UpdateColumn("submit_count", gorm.Expr("submit_count + 1"))
		if overallStatus == judgetypes.StatusAccepted {
			db.DB.Table("judge_problem").Where("id = ?", task.ProblemID).
				UpdateColumn("accept_count", gorm.Expr("accept_count + 1"))
		}
	}
}

// writeDetails 写入每个测试用例的判题详情（通过回调函数，避免循环导入）
func (e *JudgeEngine) writeDetails(submissionID, problemID string, details []testcase.TestCaseResult) {
	if detailWriter == nil {
		return
	}
	detailWriter(submissionID, problemID, details)
}

// ---------- 状态优先级（聚合用） ----------

var statusPriority = map[string]int{
	judgetypes.StatusSE:          0,
	judgetypes.StatusRF:          1,
	judgetypes.StatusOLE:         2,
	judgetypes.StatusMLE:         3,
	judgetypes.StatusTLE:         4,
	judgetypes.StatusRE:          5,
	judgetypes.StatusPE:          6,
	judgetypes.StatusWrongAnswer: 7,
	judgetypes.StatusAccepted:    8,
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

// ─── 测试用例数据读取辅助 ──────────────────────────────────────────────

// getTestCaseInput 从缓存读取测试用例输入
func getTestCaseInput(tc testcase.JudgeTestcase, cache map[string][]byte) string {
	if data, ok := cache[tc.ID]; ok {
		return string(data)
	}
	return ""
}

// getTestCaseOutput 从缓存读取测试用例输出
func getTestCaseOutput(tc testcase.JudgeTestcase, cache map[string][]byte) string {
	if data, ok := cache[tc.ID]; ok {
		return string(data)
	}
	return ""
}
