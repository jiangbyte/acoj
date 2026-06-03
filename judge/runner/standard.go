package runner

import (
	"log"

	"hei-gin/config"
	judgeclient "hei-gin/judge/client"
	judgetypes "hei-gin/judge/types"
)

func runStandard(jc *JudgeContext, client *judgeclient.SandboxClient) *RunResult {
	result := &RunResult{
		Status:        judgetypes.StatusAccepted,
		TestcaseTotal: len(jc.TestCases),
		Results:       make([]TestCaseResult, 0, len(jc.TestCases)),
	}

	for i, tc := range jc.TestCases {
		tcLimits := jc.Limits
		if tc.TimeLimitMs != nil {
			tcLimits.TimeMs = *tc.TimeLimitMs
		}
		if tc.MemLimitKb != nil {
			tcLimits.MemKb = *tc.MemLimitKb
		}

		tcr := TestCaseResult{
			Index:          i + 1,
			Input:          tc.Input,
			ExpectedOutput: tc.Output,
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
			log.Printf("[Judge] Run error for testcase %d: %v", i, err)
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
			result.Results = append(result.Results, tcr)
			break
		}

		tcr.TimeUsed = runResult.TimeUsed
		tcr.MemoryUsed = runResult.MemoryUsed
		tcr.Output = runResult.Stdout

		switch runResult.Status {
		case "Accepted":
			strategy := StrategyIgnoreTrailSpace
			cr := Compare(runResult.Stdout, tc.Output, strategy)
			tcr.Status = cr.Status
			if cr.Status != "AC" {
				result.Status = judgetypes.StatusWrongAnswer
			} else {
				tcr.Score = tc.Score
				result.Score += tc.Score
			}
		case "TimeLimitExceeded":
			tcr.Status = judgetypes.StatusTimeLimitExceeded
			result.Status = judgetypes.StatusTimeLimitExceeded
		case "MemoryLimitExceeded":
			tcr.Status = judgetypes.StatusMemoryLimitExceeded
			result.Status = judgetypes.StatusMemoryLimitExceeded
		case "NonZeroExitStatus", "Signalled", "RuntimeError":
			tcr.Status = judgetypes.StatusRuntimeError
			result.Status = judgetypes.StatusRuntimeError
			tcr.Output = runResult.Stderr
		default:
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
		}

		if runResult.TimeUsed > result.TimeUsed {
			result.TimeUsed = runResult.TimeUsed
		}
		if runResult.MemoryUsed > result.MemoryUsed {
			result.MemoryUsed = runResult.MemoryUsed
		}

		result.Results = append(result.Results, tcr)
		if tcr.Status != "AC" {
			break
		} else {
			result.TestcasePass++
		}
	}
	return result
}

func getRunArgs(lang string) []string {
	for _, l := range config.C.Judge.Languages {
		if l.Name == lang {
			return l.RunArgs
		}
	}
	return []string{"./a.out"}
}
