package runner

import (
	"context"

	judgeclient "hei-gin/judge/client"
	judgetypes "hei-gin/judge/types"
	problemModel "hei-gin/modules/judge/problem"
)

// JudgeContext holds all data needed for a single judging run.
type JudgeContext struct {
	Ctx           context.Context
	Problem       *problemModel.JudgeProblem
	TestCases     []problemModel.JudgeProblemTestCase
	Subtasks      []problemModel.JudgeProblemSubtask
	LangCfg       *problemModel.JudgeProblemLanguage
	DepMap        map[string][]problemModel.JudgeProblemSubtaskDep
	Limits        judgetypes.Limits
	JudgeLanguage string
	ExecFileID    string
	SourceCode    []byte
	SourceName    string
}

// RunResult holds the aggregated judging result for a submission.
type RunResult struct {
	Status        string
	Score         int
	TimeUsed      int64
	MemoryUsed    int64
	TestcasePass  int
	TestcaseTotal int
	ErrorInfo     string
	Results       []TestCaseResult
}

// TestCaseResult holds the result of a single test case.
type TestCaseResult struct {
	Index          int
	Status         string
	Score          int
	TimeUsed       int64
	MemoryUsed     int64
	Output         string
	ExpectedOutput string
	Input          string
	SubtaskID      int
}

// Run executes the judging pipeline based on the problem's judge method.
func Run(jc *JudgeContext, client *judgeclient.SandboxClient) *RunResult {
	switch jc.Problem.JudgeMethod {
	case problemModel.JudgeMethodStandard:
		return runStandard(jc, client)
	case problemModel.JudgeMethodSPJ:
		return runSPJ(jc, client)
	case problemModel.JudgeMethodSubtask:
		return runSubtask(jc, client)
	case problemModel.JudgeMethodInteractive:
		return runInteractive(jc, client)
	default:
		return &RunResult{Status: judgetypes.StatusSystemError, ErrorInfo: "unknown judge method: " + jc.Problem.JudgeMethod}
	}
}
