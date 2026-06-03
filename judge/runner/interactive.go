package runner

import (
	"context"
	"fmt"
	"log"
	judgeclient "hei-gin/judge/client"
	judgetypes "hei-gin/judge/types"
	pb "hei-gin/judge/grpc"
)

func runInteractive(jc *JudgeContext, client *judgeclient.SandboxClient) *RunResult {
	result := &RunResult{
		Status:        judgetypes.StatusAccepted,
		TestcaseTotal: len(jc.TestCases),
		Results:       make([]TestCaseResult, 0, len(jc.TestCases)),
	}

	spjFileID, err := compileSPJ(context.Background(), client, jc.Problem.InteractorSrc, jc.Problem.SpjLanguage)
	if err != nil {
		result.Status = judgetypes.StatusSystemError
		result.ErrorInfo = fmt.Sprintf("Interactor compile failed: %v", err)
		return result
	}

	for i, tc := range jc.TestCases {
		tcr := TestCaseResult{
			Index:          i + 1,
			Input:          tc.Input,
			ExpectedOutput: tc.Output,
		}

		userArgs := getRunArgs(jc.JudgeLanguage)
		limitMs := jc.Limits.TimeMs
		if limitMs <= 0 { limitMs = 2000 }

		// Build empty file for pipe-connected fds
		// In go-judge, an empty Request_File (no oneof field set) means "no file for this fd",
		// allowing the pipe mapping to connect to this fd.
		emptyFile := pb.Request_File_builder{}.Build()



		// CopyIn for user program
		userCopyIn := make(map[string]*pb.Request_File)
		if jc.ExecFileID != "" {
			userCopyIn["a.out"] = (&pb.Request_File_builder{
				Cached: (&pb.Request_CachedFile_builder{FileID: jc.ExecFileID}).Build(),
			}).Build()
		}
		if len(jc.SourceCode) > 0 {
			srcName := jc.SourceName
			if srcName == "" { srcName = "main.py" }
			userCopyIn[srcName] = (&pb.Request_File_builder{
				Memory: (&pb.Request_MemoryFile_builder{Content: jc.SourceCode}).Build(),
			}).Build()
		}

		// CopyIn for interactor
		interactorCopyIn := map[string]*pb.Request_File{
			"interactor": (&pb.Request_File_builder{
				Cached: (&pb.Request_CachedFile_builder{FileID: spjFileID}).Build(),
			}).Build(),
			"input.txt": (&pb.Request_File_builder{
				Memory: (&pb.Request_MemoryFile_builder{Content: []byte(tc.Input)}).Build(),
			}).Build(),
			"answer.txt": (&pb.Request_File_builder{
				Memory: (&pb.Request_MemoryFile_builder{Content: []byte(tc.Output)}).Build(),
			}).Build(),
		}

		// Checker limits (more generous for interactor)
		checkerTimeMs := jc.Limits.TimeMs * 3
		if checkerTimeMs > 30000 { checkerTimeMs = 30000 }
		if checkerTimeMs <= 0 { checkerTimeMs = 5000 }
		checkerMemKb := jc.Limits.MemKb * 2

		// Build the request with 2 commands and pipe mappings
		req := (&pb.Request_builder{
			Cmd: []*pb.Request_CmdType{
				// Cmd[0]: User program
				(&pb.Request_CmdType_builder{
					Args: userArgs,
					Env:  []string{"PATH=/usr/bin:/bin:/usr/local/go/bin"},
					Files: []*pb.Request_File{
						emptyFile,        // stdin (fd 0): from pipe (interactor stdout)
						emptyFile,        // stdout (fd 1): to pipe (interactor stdin)
					},
					CpuTimeLimit:   uint64(limitMs) * 1000000,
					ClockTimeLimit: uint64(limitMs) * 2000000,
					MemoryLimit:    uint64(jc.Limits.MemKb) * 1024,
					ProcLimit:      50,
					CopyIn:         userCopyIn,

				}).Build(),
				// Cmd[1]: Interactor
				(&pb.Request_CmdType_builder{
					Args: []string{"./interactor", "input.txt", "answer.txt", "user_output.txt", "interactor_output.txt"},
					Env:  []string{},
					Files: []*pb.Request_File{
						emptyFile,        // stdin (fd 0): from pipe (user stdout)
						emptyFile,        // stdout (fd 1): to pipe (user stdin)
					},
					CpuTimeLimit:   uint64(checkerTimeMs) * 1000000,
					ClockTimeLimit: uint64(checkerTimeMs) * 2000000,
					MemoryLimit:    uint64(checkerMemKb) * 1024,
					ProcLimit:      10,
					CopyIn:         interactorCopyIn,

				}).Build(),
			},
			PipeMapping: []*pb.Request_PipeMap{
				// Pipe[0]: User stdout (Cmd[0] fd 1) → Interactor stdin (Cmd[1] fd 0)
				(&pb.Request_PipeMap_builder{
					In:    (&pb.Request_PipeMap_PipeIndex_builder{Index: 0, Fd: 1}).Build(),
					Out:   (&pb.Request_PipeMap_PipeIndex_builder{Index: 1, Fd: 0}).Build(),
					Proxy: true,
					Name:  "user_stdout",
					Max:   65536,
				}).Build(),
				// Pipe[1]: Interactor stdout (Cmd[1] fd 1) → User stdin (Cmd[0] fd 0)
				(&pb.Request_PipeMap_builder{
					In:    (&pb.Request_PipeMap_PipeIndex_builder{Index: 1, Fd: 1}).Build(),
					Out:   (&pb.Request_PipeMap_PipeIndex_builder{Index: 0, Fd: 0}).Build(),
					Proxy: true,
					Name:  "interactor_stdout",
					Max:   65536,
				}).Build(),
			},
		}).Build()

		resp, err := client.ExecClient().Exec(req)
		if err != nil {
			log.Printf("[Judge Interactive] Exec error for testcase %d: %v", i, err)
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
			result.Results = append(result.Results, tcr)
			break
		}

		// Parse results
		results := resp.GetResults()
		if len(results) < 2 {
			log.Printf("[Judge Interactive] Expected 2 results, got %d", len(results))
			tcr.Status = judgetypes.StatusSystemError
			result.Status = judgetypes.StatusSystemError
			result.Results = append(result.Results, tcr)
			break
		}

		userResult := results[0]
		interactorResult := results[1]

		tcr.TimeUsed = int64(userResult.GetTime()) / 1000       // ns -> us
		tcr.MemoryUsed = int64(userResult.GetMemory()) / 1024     // bytes -> KB
		tcr.Output = string(userResult.GetFiles()["user_stdout"])

		// Check user program status
		userStatus := userResult.GetStatus()
		if userResult.GetStatus() != pb.Response_Result_Accepted {
			tcr.Status = mapGoJudgeStatus(userStatus.String())
			result.Status = tcr.Status
			userErrInfo := string(userResult.GetFiles()["stderr"])
			log.Printf("[Judge Interactive] User program status %s for testcase %d: %s", userStatus, i, userErrInfo)
		}

		// Check interactor result
		interactorExitCode := int(interactorResult.GetExitStatus())
		interactorStdout := string(interactorResult.GetFiles()["interactor_stdout"])
		interactorStderrStr := string(interactorResult.GetFiles()["stderr"])

		// Pipe-collected data:
		// userResult.GetFiles()["user_stdout"] = user's stdout
		// interactorResult.GetFiles()["interactor_stdout"] = interactor's stdout

		verdict := parseSPJVerdict(int32(interactorExitCode), interactorStdout, interactorStderrStr)
		tcr.Status = verdict.Status
		tcr.Score = verdict.Score

		// If user program already failed (TLE/MLE/RE), keep that status
		if userResult.GetStatus() != pb.Response_Result_Accepted && (tcr.Status == judgetypes.StatusAccepted || tcr.Status == "") {
			tcr.Status = mapGoJudgeStatus(userStatus.String())
		}

		if tcr.Status == judgetypes.StatusAccepted || tcr.Status == "PARTIAL" {
			result.TestcasePass++
			if tcr.Score > 0 {
				result.Score += tcr.Score
			}
		} else if tcr.Status != "" {
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


