package runner

import (
	"log"

	"hei-gin/config"
	judgeclient "hei-gin/judge/client"
	judgetypes "hei-gin/judge/types"
	problemModel "hei-gin/modules/judge/problem"
)

func runSubtask(jc *JudgeContext, client *judgeclient.SandboxClient) *RunResult {
	result := &RunResult{
		Status:        judgetypes.StatusAccepted,
		TestcaseTotal: len(jc.TestCases),
		Results:       make([]TestCaseResult, 0, len(jc.TestCases)),
	}

	subtaskPassed := make(map[string]bool)
	totalScore := 0

	for _, st := range jc.Subtasks {
		deps := jc.DepMap[st.ID]
		depsPassed := true
		for _, dep := range deps {
			if !subtaskPassed[dep.DependsOnSubtaskID] {
				depsPassed = false
				break
			}
		}

		if !depsPassed {
			for _, tc := range filterBySubtaskID(jc.TestCases, st.ID) {
				tcr := TestCaseResult{
					Index:          findIndex(jc.TestCases, tc.ID),
					Status:         judgetypes.StatusSkipped,
					Input:          tc.Input,
					ExpectedOutput: tc.Output,
					SubtaskID:      findSubtaskSortOrder(jc.Subtasks, st.ID),
				}
				result.Results = append(result.Results, tcr)
			}
			continue
		}

		subtaskAllPassed := true
		subtaskTCs := filterBySubtaskID(jc.TestCases, st.ID)

		for _, tc := range subtaskTCs {
			tcLimits := jc.Limits
			if tc.TimeLimitMs != nil {
				tcLimits.TimeMs = *tc.TimeLimitMs
			}
			if tc.MemLimitKb != nil {
				tcLimits.MemKb = *tc.MemLimitKb
			}

			tcr := TestCaseResult{
				Index:          findIndex(jc.TestCases, tc.ID),
				Input:          tc.Input,
				ExpectedOutput: tc.Output,
				SubtaskID:      findSubtaskSortOrder(jc.Subtasks, st.ID),
			}

			runResult, err := client.Run(&judgeclient.RunArgs{
				Args:          getRunArgs(jc.JudgeLanguage),
				Stdin:         tc.Input,
				TimeLimitMs:   tcLimits.TimeMs,
				MemoryLimitKb: tcLimits.MemKb,
				ExecFileID:    jc.ExecFileID,
				SourceCode:    jc.SourceCode,
				SourceName:    jc.SourceName,
			})
			if err != nil {
				log.Printf("[Judge Subtask] Run error for testcase: %v", err)
				tcr.Status = judgetypes.StatusSystemError
				result.Status = judgetypes.StatusSystemError
				subtaskAllPassed = false
				result.Results = append(result.Results, tcr)
				break
			}

			tcr.TimeUsed = runResult.TimeUsed
			tcr.MemoryUsed = runResult.MemoryUsed
			tcr.Output = runResult.Stdout

			if runResult.Status == "Accepted" {
				strategy := StrategyIgnoreTrailSpace
				cr := Compare(runResult.Stdout, tc.Output, strategy)
				tcr.Status = cr.Status
			} else {
				tcr.Status = mapGoJudgeStatus(runResult.Status)
			}

			if tcr.Status != "AC" {
				subtaskAllPassed = false
			}

			result.TimeUsed += tcr.TimeUsed
			if tcr.MemoryUsed > result.MemoryUsed {
				result.MemoryUsed = tcr.MemoryUsed
			}
			result.Results = append(result.Results, tcr)
		}

		if subtaskAllPassed {
			subtaskPassed[st.ID] = true
			subtaskScore := st.Score
			if subtaskScore > 0 {
				totalScore += subtaskScore
			} else {
				totalScore += len(subtaskTCs)
			}
		}
	}

	result.Score = totalScore
	if totalScore >= 100 {
		result.Status = judgetypes.StatusAccepted
	} else if totalScore > 0 {
		result.Status = judgetypes.StatusPartial
	} else if result.Status == judgetypes.StatusAccepted {
		result.Status = judgetypes.StatusWrongAnswer
	}

	for _, tcr := range result.Results {
		if tcr.Status == "AC" {
			result.TestcasePass++
		}
	}

	return result
}

func filterBySubtaskID(tcs []problemModel.JudgeProblemTestCase, subtaskID string) []problemModel.JudgeProblemTestCase {
	var out []problemModel.JudgeProblemTestCase
	for _, tc := range tcs {
		if tc.SubtaskID != nil && *tc.SubtaskID == subtaskID {
			out = append(out, tc)
		}
	}
	return out
}

func findIndex(tcs []problemModel.JudgeProblemTestCase, id string) int {
	for i, tc := range tcs {
		if tc.ID == id {
			return i + 1
		}
	}
	return 0
}

func findSubtaskSortOrder(subtasks []problemModel.JudgeProblemSubtask, id string) int {
	for _, st := range subtasks {
		if st.ID == id {
			return st.SortOrder
		}
	}
	return 0
}

func init() {
	_ = config.C
}
