package gojudge

import (
	"fmt"
	"sync"

	"github.com/criyle/go-judge/pb"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/langconf"
)

const (
	defaultPipeMax    = 64 * 1024 * 1024 // 64MB pipe buffer (fallback)
	defaultCopyOutMax = 64 * 1024 * 1024 // 64MB max output copy (fallback)
)

// Backend go-judge 沙箱后端
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

// Exec 执行代码（完全由 config.yaml judge.languages 驱动）
func (b *Backend) Exec(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	ld := langconf.Get(req.Language)
	if ld == nil {
		return nil, fmt.Errorf("unsupported language: %s", req.Language)
	}

	if ld.Interpreted {
		return b.execInterpreted(req, ld)
	}

	// 编译型语言
	if len(req.Binary) > 0 {
		return b.execBinaryRun(req, ld, req.Binary)
	}
	return b.execCompileRun(req, ld)
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

// InteractiveExec 交互式判题（顺序执行方案）
func (b *Backend) InteractiveExec(userReq, interactorReq *judgetypes.ExecRequest, testInput string) (*judgetypes.ExecResult, error) {
	userResult, err := b.Exec(userReq)
	if err != nil {
		return userResult, err
	}
	if userResult.Status != judgetypes.StatusAccepted {
		return userResult, nil
	}
	interReq := *interactorReq
	interReq.Stdin = testInput + "\n---USER_OUT---\n" + userResult.Stdout
	return b.Exec(&interReq)
}

func (b *Backend) Health() *judgetypes.HealthStatus {
	alive, version, err := b.client.Health()
	if err != nil {
		return &judgetypes.HealthStatus{Alive: false, BackendName: b.name, Error: err.Error()}
	}
	return &judgetypes.HealthStatus{Alive: alive, Version: version, BackendName: b.name}
}

// ---------- 解释型语言 ----------

func (b *Backend) execInterpreted(req *judgetypes.ExecRequest, ld *langconf.LangDef) (*judgetypes.ExecResult, error) {
	cmd := b.buildCmd(req)
	cmd.SetArgs(ld.RunArgs)
	cmd.SetCopyIn(map[string]*pb.Request_File{
		ld.SourceFile: makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(cmd, req.Stdin, req.MaxOutput)
	return b.doExec(cmd)
}

// ---------- 编译型语言 ----------

func (b *Backend) execCompileRun(req *judgetypes.ExecRequest, ld *langconf.LangDef) (*judgetypes.ExecResult, error) {
	// Step 1: 编译
	compileCmd := b.buildCmd(req)
	compileCmd.SetArgs(append([]string{ld.Compiler}, ld.CompileArgs...))
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), 10000000000))
	compileCmd.SetClockTimeLimit(maxUint64(uint64(req.MaxRealTime), 30000000000))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetStackLimit(maxUint64(uint64(req.MaxStack), 536870912))
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		ld.SourceFile: makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(compileCmd, "", req.MaxOutput)

	// 编译产出 copyOut
	copyOutFiles := makeStdCopyOut()
	for _, out := range ld.CompileOut {
		copyOutFiles = append(copyOutFiles, makeCopyOutFile(out))
	}
	compileCmd.SetCopyOut(copyOutFiles)

	compileResult, err := b.doExecWithBinary(compileCmd, ld.CompileOut)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		compileResult.Status = judgetypes.StatusCompileError
		return compileResult, nil
	}

	// Step 2: 运行
	runResult, err := b.execBinaryRun(req, ld, compileResult.Binary)
	if err != nil {
		return runResult, err
	}
	if runResult != nil {
		runResult.Binary = compileResult.Binary
	}
	return runResult, nil
}

// ---------- 通用方法 ----------

func (b *Backend) buildCmd(req *judgetypes.ExecRequest) *pb.Request_CmdType {
	cmd := &pb.Request_CmdType{}
	env := req.Env
	if len(env) == 0 {
		env = []string{"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"}
	}
	cmd.SetEnv(env)
	cmd.SetCpuTimeLimit(uint64(req.MaxCPUTime))
	cmd.SetClockTimeLimit(uint64(req.MaxRealTime))
	cmd.SetMemoryLimit(uint64(req.MaxMemory))
	cmd.SetStackLimit(uint64(req.MaxStack))
	copyOutMax := uint64(defaultCopyOutMax)
	if req.MaxOutput > 0 {
		if u := uint64(req.MaxOutput); u < copyOutMax {
			copyOutMax = u
		}
	}
	cmd.SetCopyOutMax(copyOutMax)
	cmd.SetProcLimit(64)
	return cmd
}

// execBinaryRun 使用预编译二进制直接运行
// 使用 ld.CompileOut 第一个文件名作为二进制文件名（由 config.yaml 配置）
func (b *Backend) execBinaryRun(req *judgetypes.ExecRequest, ld *langconf.LangDef, binary []byte) (*judgetypes.ExecResult, error) {
	if ld.Interpreted {
		return b.execInterpreted(req, ld)
	}

	// 编译型语言: 使用配置的编译产出文件名，优先取第一个
	binaryName := "main"
	if len(ld.CompileOut) > 0 && ld.CompileOut[0] != "" {
		binaryName = ld.CompileOut[0]
	}

	copyIn := map[string]*pb.Request_File{
		binaryName: makeMemoryFile(binary),
	}

	cmd := b.buildCmd(req)
	cmd.SetArgs(ld.RunArgs)
	cmd.SetCopyIn(copyIn)
	setStdFiles(cmd, req.Stdin, req.MaxOutput)
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

func (b *Backend) doExecWithBinary(cmd *pb.Request_CmdType, compileOut []string) (*judgetypes.ExecResult, error) {
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
	return mapResultWithBinary(results[0], compileOut), nil
}

// CompileOnly 只编译不运行, 返回二进制
func (b *Backend) CompileOnly(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	ld := langconf.Get(req.Language)
	if ld == nil || ld.Interpreted {
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	compileReq := *req
	compileReq.Stdin = ""
	result, err := b.Exec(&compileReq)
	if err != nil {
		return result, err
	}
	if result.Status == judgetypes.StatusCompileError || result.Status == judgetypes.StatusSE {
		return result, nil
	}
	return &judgetypes.ExecResult{
		Status: judgetypes.StatusAccepted,
		Binary: result.Binary,
	}, nil
}

// RunWithBinary 使用预编译二进制运行 (对外接口)
func (b *Backend) RunWithBinary(req *judgetypes.ExecRequest, binary []byte) (*judgetypes.ExecResult, error) {
	ld := langconf.Get(req.Language)
	if ld == nil {
		return nil, fmt.Errorf("unsupported language: %s", req.Language)
	}
	return b.execBinaryRun(req, ld, binary)
}

// ---------- 辅助函数 ----------

func makeMemoryFile(content []byte) *pb.Request_File {
	mf := &pb.Request_MemoryFile{}
	mf.SetContent(content)
	f := &pb.Request_File{}
	f.SetMemory(mf)
	return f
}

func makePipeCollector(name string, max int64) *pb.Request_File {
	pc := &pb.Request_PipeCollector{}
	pc.SetName(name)
	pc.SetMax(max)
	pc.SetPipe(false)
	f := &pb.Request_File{}
	f.SetPipe(pc)
	return f
}

func makeCopyOutFile(name string) *pb.Request_CmdCopyOutFile {
	f := &pb.Request_CmdCopyOutFile{}
	f.SetName(name)
	return f
}

func makeStdCopyOut() []*pb.Request_CmdCopyOutFile {
	return []*pb.Request_CmdCopyOutFile{
		makeCopyOutFile("stdout"),
		makeCopyOutFile("stderr"),
	}
}

func setStdFiles(cmd *pb.Request_CmdType, stdin string, maxOutput int64) {
	pipeMax := uint64(defaultPipeMax)
	if maxOutput > 0 {
		if u := uint64(maxOutput); u*2 < pipeMax {
			pipeMax = u * 2
			if pipeMax < 4096 {
				pipeMax = 4096
			}
		}
	}
	cmd.SetFiles([]*pb.Request_File{
		makeMemoryFile([]byte(stdin)),
		makePipeCollector("stdout", int64(pipeMax)),
		makePipeCollector("stderr", int64(pipeMax)),
	})
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

func mapResultWithBinary(r *pb.Response_Result, compileOut []string) *judgetypes.ExecResult {
	result := mapResult(r)
	if files := r.GetFiles(); files != nil {
		for _, name := range compileOut {
			if data, ok := files[name]; ok && len(data) > 0 {
				result.Binary = data
				break
			}
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
		return judgetypes.StatusOLE
	case pb.Response_Result_DangerousSyscall:
		return judgetypes.StatusRF
	case pb.Response_Result_Signalled:
		return judgetypes.StatusRE
	case pb.Response_Result_InternalError:
		return judgetypes.StatusSE
	case pb.Response_Result_FileError:
		return judgetypes.StatusSE
	default:
		return judgetypes.StatusSE
	}
}
