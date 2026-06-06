package gojudge

import (
	"fmt"
	"sync"

	"github.com/criyle/go-judge/pb"

	"hei-gin/plugins/plugin-judge/judgetypes"
)

type Backend struct {
	name   string
	client *GoJudgeClient
}

func NewBackend(endpoint string, timeout int) (*Backend, error) {
	client, err := NewGoJudgeClient(endpoint, timeout)
	if err != nil {
		return nil, fmt.Errorf("create go-judge client: %w", err)
	}
	return &Backend{name: "go-judge", client: client}, nil
}

func (b *Backend) Name() string { return b.name }

func (b *Backend) Exec(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	switch req.Language {
	case "python", "python3":
		return b.execInterpreted(req, "python3")
	case "bash", "sh":
		return b.execInterpreted(req, "sh")
	case "javascript", "js", "node":
		return b.execInterpreted(req, "node")
	default:
		return b.execCompiled(req)
	}
}

func (b *Backend) BatchExec(reqs []*judgetypes.ExecRequest) ([]*judgetypes.ExecResult, error) {
	var wg sync.WaitGroup
	results := make([]*judgetypes.ExecResult, len(reqs))
	for i, r := range reqs {
		wg.Add(1)
		go func(idx int, r *judgetypes.ExecRequest) {
			defer wg.Done()
			res, err := b.Exec(r)
			if err != nil {
				results[idx] = &judgetypes.ExecResult{Status: judgetypes.StatusSE, Error: err.Error()}
				return
			}
			results[idx] = res
		}(i, r)
	}
	wg.Wait()
	return results, nil
}

func (b *Backend) Health() *judgetypes.HealthStatus {
	alive, version, err := b.client.Health()
	if err != nil {
		return &judgetypes.HealthStatus{Alive: false, BackendName: b.name, Error: err.Error()}
	}
	return &judgetypes.HealthStatus{Alive: alive, Version: version, BackendName: b.name}
}

func (b *Backend) execInterpreted(req *judgetypes.ExecRequest, interpreter string) (*judgetypes.ExecResult, error) {
	cmd := &pb.Request_CmdType{}
	cmd.SetArgs([]string{interpreter, "-c", req.Code})
	cmd.SetEnv(req.Env)
	cmd.SetCpuTimeLimit(uint64(req.MaxCPUTime))
	cmd.SetClockTimeLimit(uint64(req.MaxRealTime))
	cmd.SetMemoryLimit(uint64(req.MaxMemory))
	cmd.SetProcLimit(50)
	cmd.SetFiles(makeStdFiles(req.Stdin))
	cmd.SetCopyOut(makeStdCopyOut())
	return b.doExec(cmd)
}

func (b *Backend) execCompiled(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	switch req.Language {
	case "c":
		return b.execCStyle(req, "gcc", "main.c", "-o", "main")
	case "cpp", "c++":
		return b.execCStyle(req, "g++", "main.cpp", "-o", "main")
	case "go":
		return b.execGo(req)
	case "rust", "rs":
		return b.execRust(req)
	case "java":
		return b.execJava(req)
	default:
		return b.execCStyle(req, "gcc", "main.c", "-o", "main")
	}
}

func (b *Backend) execCStyle(req *judgetypes.ExecRequest, compiler, srcFile string, extraArgs ...string) (*judgetypes.ExecResult, error) {
	// Compile
	compileCmd := &pb.Request_CmdType{}
	compileCmd.SetArgs(append([]string{compiler, srcFile}, extraArgs...))
	compileCmd.SetEnv(req.Env)
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), 10000000000))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetProcLimit(50)
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		srcFile: makeMemoryFile([]byte(req.Code)),
	})
	compileCmd.SetFiles(makeStdFiles(""))
	compileCmd.SetCopyOut(makeStdCopyOut())

	compileResult, err := b.doExec(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		return compileResult, nil
	}

	// Run
	runCmd := &pb.Request_CmdType{}
	runCmd.SetArgs([]string{"./main"})
	runCmd.SetEnv(req.Env)
	runCmd.SetCpuTimeLimit(uint64(req.MaxCPUTime))
	runCmd.SetClockTimeLimit(uint64(req.MaxRealTime))
	runCmd.SetMemoryLimit(uint64(req.MaxMemory))
	runCmd.SetProcLimit(50)
	runCmd.SetFiles(makeStdFiles(req.Stdin))
	runCmd.SetCopyOut(makeStdCopyOut())
	return b.doExec(runCmd)
}

func (b *Backend) execGo(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	cmd := &pb.Request_CmdType{}
	cmd.SetArgs([]string{"go", "run", "main.go"})
	cmd.SetEnv(append(req.Env, "GOPATH=/tmp/gopath", "HOME=/tmp"))
	cmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), 30000000000))
	cmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 1073741824))
	cmd.SetProcLimit(100)
	cmd.SetCopyIn(map[string]*pb.Request_File{
		"main.go": makeMemoryFile([]byte(req.Code)),
	})
	cmd.SetFiles(makeStdFiles(req.Stdin))
	cmd.SetCopyOut(makeStdCopyOut())
	return b.doExec(cmd)
}

func (b *Backend) execRust(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	// Compile
	compileCmd := &pb.Request_CmdType{}
	compileCmd.SetArgs([]string{"rustc", "main.rs", "-o", "main"})
	compileCmd.SetEnv(req.Env)
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), 30000000000))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 1073741824))
	compileCmd.SetProcLimit(50)
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		"main.rs": makeMemoryFile([]byte(req.Code)),
	})
	compileCmd.SetFiles(makeStdFiles(""))
	compileCmd.SetCopyOut(makeStdCopyOut())

	compileResult, err := b.doExec(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		return compileResult, nil
	}

	// Run
	runCmd := &pb.Request_CmdType{}
	runCmd.SetArgs([]string{"./main"})
	runCmd.SetEnv(req.Env)
	runCmd.SetCpuTimeLimit(uint64(req.MaxCPUTime))
	runCmd.SetClockTimeLimit(uint64(req.MaxRealTime))
	runCmd.SetMemoryLimit(uint64(req.MaxMemory))
	runCmd.SetProcLimit(50)
	runCmd.SetFiles(makeStdFiles(req.Stdin))
	runCmd.SetCopyOut(makeStdCopyOut())
	return b.doExec(runCmd)
}

func (b *Backend) execJava(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	cmd := &pb.Request_CmdType{}
	cmd.SetArgs([]string{"sh", "-c", fmt.Sprintf("cat > Main.java && javac Main.java && java Main")})
	cmd.SetEnv(req.Env)
	cmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), 30000000000))
	cmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 1073741824))
	cmd.SetProcLimit(100)
	cmd.SetFiles(makeStdFiles(req.Stdin))
	cmd.SetCopyOut(makeStdCopyOut())
	return b.doExec(cmd)
}

func (b *Backend) doExec(cmd *pb.Request_CmdType) (*judgetypes.ExecResult, error) {
	pbReq := &pb.Request{}
	pbReq.SetCmd([]*pb.Request_CmdType{cmd})

	resp, err := b.client.Exec(pbReq)
	if err != nil {
		return &judgetypes.ExecResult{Status: judgetypes.StatusSE, Error: err.Error()}, nil
	}
	results := resp.GetResults()
	if len(results) == 0 {
		return &judgetypes.ExecResult{Status: judgetypes.StatusSE, Error: "no result"}, nil
	}
	return mapResult(results[0]), nil
}

// makeMemoryFile 创建内存文件
func makeMemoryFile(content []byte) *pb.Request_File {
	mf := &pb.Request_MemoryFile{}
	mf.SetContent(content)
	f := &pb.Request_File{}
	f.SetMemory(mf)
	return f
}

// makePipeFile 创建管道文件
func makePipeFile(name string) *pb.Request_File {
	pc := &pb.Request_PipeCollector{}
	pc.SetName(name)
	f := &pb.Request_File{}
	f.SetPipe(pc)
	return f
}

// makeCopyOutFile 创建输出文件定义
func makeCopyOutFile(name string) *pb.Request_CmdCopyOutFile {
	f := &pb.Request_CmdCopyOutFile{}
	f.SetName(name)
	return f
}

// makeStdFiles 创建 stdin/stdout/stderr 文件列表
func makeStdFiles(stdin string) []*pb.Request_File {
	return []*pb.Request_File{
		makeMemoryFile([]byte(stdin)),
		makePipeFile("stdout"),
		makePipeFile("stderr"),
	}
}

// makeStdCopyOut 创建 stdout/stderr 输出定义
func makeStdCopyOut() []*pb.Request_CmdCopyOutFile {
	return []*pb.Request_CmdCopyOutFile{
		makeCopyOutFile("stdout"),
		makeCopyOutFile("stderr"),
	}
}

func maxUint64(a, b uint64) uint64 {
	if a > b {
		return a
	}
	return b
}

func mapResult(r *pb.Response_Result) *judgetypes.ExecResult {
	result := &judgetypes.ExecResult{
		Status:     mapPBStatus(r.GetStatus()),
		ExitCode:   int(r.GetExitStatus()),
		TimeUsed:   int64(r.GetTime()),
		MemoryUsed: int64(r.GetMemory()),
		Error:      r.GetError(),
	}
	if files := r.GetFiles(); files != nil {
		if out, ok := files["stdout"]; ok {
			result.Stdout = string(out)
		}
		if err, ok := files["stderr"]; ok {
			result.Stderr = string(err)
		}
	}
	return result
}

func mapPBStatus(status pb.Response_Result_StatusType) string {
	switch status {
	case pb.Response_Result_Accepted:
		return judgetypes.StatusAccepted
	case pb.Response_Result_MemoryLimitExceeded:
		return judgetypes.StatusMLE
	case pb.Response_Result_TimeLimitExceeded:
		return judgetypes.StatusTLE
	case pb.Response_Result_NonZeroExitStatus:
		return judgetypes.StatusRE
	case pb.Response_Result_OutputLimitExceeded:
		return judgetypes.StatusRE
	case pb.Response_Result_DangerousSyscall:
		return judgetypes.StatusRE
	case pb.Response_Result_InternalError:
		return judgetypes.StatusSE
	case pb.Response_Result_FileError:
		return judgetypes.StatusSE
	case pb.Response_Result_Signalled:
		return judgetypes.StatusRE
	default:
		return judgetypes.StatusSE
	}
}
