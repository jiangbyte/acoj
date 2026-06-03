package runner

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	judgeclient "hei-gin/judge/client"
	judgetypes "hei-gin/judge/types"
	pb "hei-gin/judge/grpc"
)

type spjVerdict struct {
	Score   int    `json:"score"`
	Message string `json:"message"`
}

func runSPJ(jc *JudgeContext, client *judgeclient.SandboxClient) *RunResult {
	result := &RunResult{
		Status:        judgetypes.StatusAccepted,
		TestcaseTotal: len(jc.TestCases),
		Results:       make([]TestCaseResult, 0, len(jc.TestCases)),
	}

	spjFileID, err := compileSPJ(context.Background(), client, jc.Problem.SpjSource, jc.Problem.SpjLanguage)
	if err != nil {
		result.Status = judgetypes.StatusSystemError
		result.ErrorInfo = fmt.Sprintf("SPJ compile failed: %v", err)
		return result
	}

	for i, tc := range jc.TestCases {
		tcr := TestCaseResult{
			Index:          i + 1,
			Input:          tc.Input,
			ExpectedOutput: tc.Output,
		}

		runResult, err := client.Run(&judgeclient.RunArgs{
			Args:          getRunArgs(jc.JudgeLanguage),
			Stdin:         tc.Input,
			TimeLimitMs:   jc.Limits.TimeMs,
			MemoryLimitKb: jc.Limits.MemKb,
			ExecFileID:    jc.ExecFileID,
			SourceCode:    jc.SourceCode,
			SourceName:    jc.SourceName,
		})
		if err != nil {
			log.Printf("[Judge SPJ] Run error for testcase %d: %v", i, err)
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
			result.Results = append(result.Results, tcr)
			break
		}

		tcr.TimeUsed = runResult.TimeUsed
		tcr.MemoryUsed = runResult.MemoryUsed
		tcr.Output = runResult.Stdout

		if runResult.Status != "Accepted" {
			tcr.Status = mapGoJudgeStatus(runResult.Status)
			result.Status = tcr.Status
		}

		checkerLimits := struct {
			TimeMs int
			MemKb  int64
		}{
			TimeMs: jc.Limits.TimeMs * 3,
			MemKb:  jc.Limits.MemKb * 2,
		}
		if checkerLimits.TimeMs > 30000 {
			checkerLimits.TimeMs = 30000
		}

		checkerReq := (&pb.Request_builder{
			Cmd: []*pb.Request_CmdType{
				(&pb.Request_CmdType_builder{
					Args:         []string{"/bin/sh", "-c", "exec ./checker input.txt user.out answer.txt > /w/stdout 2> /w/stderr"},
					CpuTimeLimit: uint64(checkerLimits.TimeMs) * 1000000,
					MemoryLimit:  uint64(checkerLimits.MemKb) * 1024,
					ProcLimit:    10,
					CopyOut: []*pb.Request_CmdCopyOutFile{
						(&pb.Request_CmdCopyOutFile_builder{Name: "stdout"}).Build(),
						(&pb.Request_CmdCopyOutFile_builder{Name: "stderr"}).Build(),
					},
					CopyIn: map[string]*pb.Request_File{
						"input.txt": (&pb.Request_File_builder{
							Memory: (&pb.Request_MemoryFile_builder{Content: []byte(tc.Input)}).Build(),
						}).Build(),
						"user.out": (&pb.Request_File_builder{
							Memory: (&pb.Request_MemoryFile_builder{Content: []byte(runResult.Stdout)}).Build(),
						}).Build(),
						"answer.txt": (&pb.Request_File_builder{
							Memory: (&pb.Request_MemoryFile_builder{Content: []byte(tc.Output)}).Build(),
						}).Build(),
						"checker": (&pb.Request_File_builder{
							Cached: (&pb.Request_CachedFile_builder{FileID: spjFileID}).Build(),
						}).Build(),
					},
				}).Build(),
			},
		}).Build()

		checkerResp, err := client.ExecClient().Exec(checkerReq)
		if err != nil {
			log.Printf("[Judge SPJ] Checker exec error for testcase %d: %v", i, err)
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
			result.Results = append(result.Results, tcr)
			break
		}

		cr := checkerResp.GetResults()[0]
		verdict := parseSPJVerdict(cr.GetExitStatus(), string(cr.GetFiles()["stdout"]), string(cr.GetFiles()["stderr"]))
		tcr.Status = verdict.Status
		tcr.Score = verdict.Score

		if tcr.Status == "AC" || tcr.Status == "PARTIAL" {
			result.TestcasePass++
			if tcr.Score > 0 {
				result.Score += tcr.Score
			}
		} else {
			result.Status = tcr.Status
		}

		result.TimeUsed += tcr.TimeUsed
		if tcr.MemoryUsed > result.MemoryUsed {
			result.MemoryUsed = tcr.MemoryUsed
		}
		result.Results = append(result.Results, tcr)
	}

	return result
}

func parseSPJVerdict(exitCode int32, stdout, stderr string) TestCaseResult {
	tcr := TestCaseResult{}
	switch exitCode {
	case 0:
		tcr.Status = "AC"
		tcr.Score = 100
	case 1:
		tcr.Status = "WA"
		tcr.Score = 0
	case 2:
		tcr.Status = "PE"
		tcr.Score = 0
	default:
		tcr.Status = "WA"
		tcr.Score = 0
	}

	if stderr != "" {
		var v spjVerdict
		if json.Unmarshal([]byte(stderr), &v) == nil && v.Score > 0 {
			tcr.Score = v.Score
			if tcr.Score >= 100 {
				tcr.Status = "AC"
			} else if tcr.Score > 0 {
				tcr.Status = "PARTIAL"
			}
		}
	}
	return tcr
}

func mapGoJudgeStatus(s string) string {
	switch s {
	case "Accepted":
		return "AC"
	case "TimeLimitExceeded":
		return "TLE"
	case "MemoryLimitExceeded":
		return "MLE"
	case "NonZeroExitStatus", "Signalled", "RuntimeError":
		return "RE"
	default:
		return "SE"
	}
}

func init() {
	_ = json.Marshal
	_ = fmt.Sprintf
}
