package gojudge

import (
	"fmt"
	"sync"

	"github.com/criyle/go-judge/pb"

	"hei-gin/plugins/plugin-judge/judgetypes"
)

const (
	defaultPipeMax    = 64 * 1024 * 1024 // 64MB pipe buffer
	defaultCopyOutMax = 64 * 1024 * 1024 // 64MB max output copy
	compileCPULimit   = 10000000000      // 10s ns 编译CPU时间下限
	compileRealLimit  = 30000000000      // 30s ns 编译实际时间下限
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

// Exec 执行代码.
// 对于编译型语言:
//   - 如果 ExecRequest.Binary 非空, 跳过编译, 直接运行预编译二进制
//   - 否则先编译再运行, 并在结果中设置 Binary 供引擎缓存
// 对于解释型语言: 忽略 Binary, 始终使用文件方式执行代码
func (b *Backend) Exec(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	switch req.Language {
	case "python", "python3":
		return b.execInterpreted(req, "python3", "py")
	case "bash", "sh":
		return b.execInterpreted(req, "sh", "sh")
	case "javascript", "js", "node":
		return b.execInterpreted(req, "node", "js")
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

// ---------- 解释型语言 (文件执行) ----------

// execInterpreted 将代码写入文件后执行, 比 -c 参数更可靠
func (b *Backend) execInterpreted(req *judgetypes.ExecRequest, interpreter, ext string) (*judgetypes.ExecResult, error) {
	filename := "user_code." + ext
	cmd := b.buildCmd(req)
	cmd.SetArgs([]string{interpreter, filename})
	cmd.SetCopyIn(map[string]*pb.Request_File{
		filename: makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(cmd, req.Stdin)
	return b.doExec(cmd)
}

// execNode (保留兼容)
func (b *Backend) execNode(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	return b.execInterpreted(req, "node", "js")
}

// ---------- 编译型语言 ----------

func (b *Backend) execCompiled(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	// 预编译二进制: 跳过编译直接运行
	if len(req.Binary) > 0 {
		return b.execBinaryRun(req, req.Binary)
	}
	switch req.Language {
	case "c":
		return b.execCompileRun(req, "gcc", "main.c", "-o", "main")
	case "cpp", "c++":
		return b.execCompileRun(req, "g++", "main.cpp", "-o", "main")
	case "go":
		return b.execCompileRunGo(req)
	case "rust", "rs":
		return b.execCompileRunRust(req)
	case "java":
		return b.execCompileRunJava(req)
	default:
		return nil, fmt.Errorf("unsupported language: %s", req.Language)
	}
}

// execCompileRun 编译+运行 C/C++, 结果中返回 Binary 供缓存
func (b *Backend) execCompileRun(req *judgetypes.ExecRequest, compiler, srcFile string, extraArgs ...string) (*judgetypes.ExecResult, error) {
	// Step 1: 编译
	compileCmd := b.buildCmd(req)
	compileCmd.SetArgs(append([]string{compiler, srcFile}, extraArgs...))
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), compileCPULimit))
	compileCmd.SetClockTimeLimit(maxUint64(uint64(req.MaxRealTime), compileRealLimit))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetStackLimit(maxUint64(uint64(req.MaxStack), 536870912))
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		srcFile: makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(compileCmd, "")
	compileCmd.SetCopyOut(append(makeStdCopyOut(), makeCopyOutFile("main")))

	compileResult, err := b.doExecWithBinary(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		compileResult.Status = judgetypes.StatusCompileError
		return compileResult, nil
	}

	// Step 2: 运行, 结果中嵌入 Binary 供引擎缓存
	runResult, err := b.execBinaryRun(req, compileResult.Binary)
	if err != nil {
		return runResult, err
	}
	if runResult != nil {
		runResult.Binary = compileResult.Binary
	}
	return runResult, nil
}

// execCompileRunGo 编译+运行 Go
func (b *Backend) execCompileRunGo(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	if len(req.Binary) > 0 {
		return b.execBinaryRun(req, req.Binary)
	}
	compileCmd := b.buildCmd(req)
	compileCmd.SetArgs([]string{"go", "build", "-o", "main", "main.go"})
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), compileCPULimit))
	compileCmd.SetClockTimeLimit(maxUint64(uint64(req.MaxRealTime), compileRealLimit))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetProcLimit(50)
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		"main.go": makeMemoryFile([]byte(req.Code)),
		"go.mod":  makeMemoryFile([]byte("module user_code")),
	})
	setStdFiles(compileCmd, "")
	compileCmd.SetCopyOut(append(makeStdCopyOut(), makeCopyOutFile("main")))

	compileResult, err := b.doExecWithBinary(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		compileResult.Status = judgetypes.StatusCompileError
		return compileResult, nil
	}

	runResult, err := b.execBinaryRun(req, compileResult.Binary)
	if err != nil {
		return runResult, err
	}
	if runResult != nil {
		runResult.Binary = compileResult.Binary
	}
	return runResult, nil
}

// execCompileRunRust 编译+运行 Rust
func (b *Backend) execCompileRunRust(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	if len(req.Binary) > 0 {
		return b.execBinaryRun(req, req.Binary)
	}
	compileCmd := b.buildCmd(req)
	compileCmd.SetArgs([]string{"rustc", "main.rs", "-o", "main"})
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), compileCPULimit))
	compileCmd.SetClockTimeLimit(maxUint64(uint64(req.MaxRealTime), compileRealLimit))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetProcLimit(50)
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		"main.rs": makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(compileCmd, "")
	compileCmd.SetCopyOut(append(makeStdCopyOut(), makeCopyOutFile("main")))

	compileResult, err := b.doExecWithBinary(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		compileResult.Status = judgetypes.StatusCompileError
		return compileResult, nil
	}

	runResult, err := b.execBinaryRun(req, compileResult.Binary)
	if err != nil {
		return runResult, err
	}
	if runResult != nil {
		runResult.Binary = compileResult.Binary
	}
	return runResult, nil
}

// execCompileRunJava 编译+运行 Java
func (b *Backend) execCompileRunJava(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	if len(req.Binary) > 0 {
		return b.execBinaryRun(req, req.Binary)
	}
	compileCmd := b.buildCmd(req)
	compileCmd.SetArgs([]string{"javac", "Main.java"})
	compileCmd.SetCpuTimeLimit(maxUint64(uint64(req.MaxCPUTime), compileCPULimit))
	compileCmd.SetClockTimeLimit(maxUint64(uint64(req.MaxRealTime), compileRealLimit))
	compileCmd.SetMemoryLimit(maxUint64(uint64(req.MaxMemory), 536870912))
	compileCmd.SetProcLimit(50)
	compileCmd.SetCopyIn(map[string]*pb.Request_File{
		"Main.java": makeMemoryFile([]byte(req.Code)),
	})
	setStdFiles(compileCmd, "")
	compileCmd.SetCopyOut(append(makeStdCopyOut(), makeCopyOutFile("Main.class")))

	compileResult, err := b.doExecWithBinary(compileCmd)
	if err != nil {
		return compileResult, err
	}
	if compileResult.Status != judgetypes.StatusAccepted {
		compileResult.Status = judgetypes.StatusCompileError
		return compileResult, nil
	}

	runResult, err := b.execBinaryRun(req, compileResult.Binary)
	if err != nil {
		return runResult, err
	}
	if runResult != nil {
		runResult.Binary = compileResult.Binary
	}
	return runResult, nil
}

// CompileOnly 只编译不运行, 返回二进制
func (b *Backend) CompileOnly(req *judgetypes.ExecRequest) (*judgetypes.ExecResult, error) {
	switch req.Language {
	case "python", "python3", "bash", "sh", "javascript", "js", "node":
		return &judgetypes.ExecResult{Status: judgetypes.StatusAccepted}, nil
	}
	// 内部调用 execCompiled 将执行编译+运行
	// 但我们只需要二进制, 通过设置超大资源限制来确保编译完成
	compileReq := *req
	compileReq.Stdin = ""
	result, err := b.Exec(&compileReq)
	if err != nil {
		return result, err
	}
	// 如果是编译错误, 保持状态
	if result.Status == judgetypes.StatusCompileError || result.Status == judgetypes.StatusSE {
		return result, nil
	}
	// 返回仅包含二进制和编译状态的结果
	return &judgetypes.ExecResult{
		Status: judgetypes.StatusAccepted,
		Binary: result.Binary,
	}, nil
}

// RunWithBinary 使用预编译二进制运行 (对外接口)
func (b *Backend) RunWithBinary(req *judgetypes.ExecRequest, binary []byte) (*judgetypes.ExecResult, error) {
	return b.execBinaryRun(req, binary)
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
	cmd.SetCopyOutMax(defaultCopyOutMax)
	cmd.SetProcLimit(50)
	return cmd
}

// execBinaryRun 使用预编译二进制直接运行 (内部)
func (b *Backend) execBinaryRun(req *judgetypes.ExecRequest, binary []byte) (*judgetypes.ExecResult, error) {
	switch req.Language {
	case "python", "python3":
		return b.execInterpreted(req, "python3", "py")
	case "bash", "sh":
		return b.execInterpreted(req, "sh", "sh")
	case "javascript", "js", "node":
		return b.execInterpreted(req, "node", "js")
	case "java":
		cmd := b.buildCmd(req)
		cmd.SetArgs([]string{"java", "Main"})
		cmd.SetCopyIn(map[string]*pb.Request_File{
			"Main.class": makeMemoryFile(binary),
		})
		setStdFiles(cmd, req.Stdin)
		return b.doExec(cmd)
	default:
		// C/C++/Go/Rust: ELF 二进制
		cmd := b.buildCmd(req)
		cmd.SetArgs([]string{"./main"})
		cmd.SetCopyIn(map[string]*pb.Request_File{
			"main": makeMemoryFile(binary),
		})
		setStdFiles(cmd, req.Stdin)
		return b.doExec(cmd)
	}
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

func (b *Backend) doExecWithBinary(cmd *pb.Request_CmdType) (*judgetypes.ExecResult, error) {
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
	return mapResultWithBinary(results[0]), nil
}

// ---------- 辅助函数 ----------

func makeMemoryFile(content []byte) *pb.Request_File {
	mf := &pb.Request_MemoryFile{}
	mf.SetContent(content)
	f := &pb.Request_File{}
	f.SetMemory(mf)
	return f
}

// makePipeCollector 创建输出收集器.
// pipe=false: 收集命令的标准输出/错误, 在响应 Files 中返回.
// pipe=true 仅用于多命令间的 IPC 管道, 对单命令无意义.
func makePipeCollector(name string, max int64) *pb.Request_File {
	pc := &pb.Request_PipeCollector{}
	pc.SetName(name)
	pc.SetMax(max)
	pc.SetPipe(false) // 单命令模式下必须为 false 才能收集到输出
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

func setStdFiles(cmd *pb.Request_CmdType, stdin string) {
	cmd.SetFiles([]*pb.Request_File{
		makeMemoryFile([]byte(stdin)),
		makePipeCollector("stdout", defaultPipeMax),
		makePipeCollector("stderr", defaultPipeMax),
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

func mapResultWithBinary(r *pb.Response_Result) *judgetypes.ExecResult {
	result := mapResult(r)
	if files := r.GetFiles(); files != nil {
		for _, name := range []string{"main", "Main.class"} {
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
		return judgetypes.StatusRE
	case pb.Response_Result_DangerousSyscall:
		return judgetypes.StatusRE
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
