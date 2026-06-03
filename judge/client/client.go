package client

import (
	"fmt"
	"sync"

	pb "hei-gin/judge/grpc"
)

// ExecutorClient defines the interface for communicating with go-judge.
type ExecutorClient interface {
	Exec(req *pb.Request) (*pb.Response, error)
	FileAdd(content *pb.FileContent) (*pb.FileID, error)
}

// SandboxClient wraps gRPC calls to a go-judge instance.
// All communication uses native gRPC on the go-judge gRPC port.
type SandboxClient struct {
	addr string
	exec *grpcExecutor
	mu   sync.Mutex
}

// NewSandboxClient creates a new SandboxClient for the given go-judge address.
// The addr should point to the go-judge gRPC endpoint (e.g. "127.0.0.1:5051").
func NewSandboxClient(addr string) (*SandboxClient, error) {
	exec, err := newGrpcExecutor(addr)
	if err != nil {
		return nil, fmt.Errorf("connect to sandbox %s failed: %w", addr, err)
	}
	return &SandboxClient{
		addr: addr,
		exec: exec,
	}, nil
}

// Close cleans up the gRPC connection.
func (c *SandboxClient) Close() {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.exec != nil {
		c.exec.Close()
		c.exec = nil
	}
}

// ExecClient returns the underlying executor for gRPC calls.
func (c *SandboxClient) ExecClient() ExecutorClient {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.exec
}

// ===== Helper methods (use ExecClient internally) =====

// Compile sends a compile request to go-judge.
func (c *SandboxClient) Compile(code []byte, lang string) (*CompileResult, error) {
	ec := c.ExecClient()

	langCfg, ok := getLangConfig(lang)
	if !ok {
		return nil, fmt.Errorf("unsupported language: %s", lang)
	}

	ext := getExtension(lang)
	fileName := "main" + ext

	fileResp, err := ec.FileAdd((&pb.FileContent_builder{
		Name:    fileName,
		Content: code,
	}).Build())
	if err != nil {
		return nil, fmt.Errorf("FileAdd failed: %w", err)
	}

	cmdStr := joinArgs(langCfg.CompileArgs)
	shellCmd := fmt.Sprintf("exec %s > /w/stdout 2> /w/stderr", cmdStr)

	req := (&pb.Request_builder{
		Cmd: []*pb.Request_CmdType{
			(&pb.Request_CmdType_builder{
				Args: []string{"/bin/sh", "-c", shellCmd},
				Env:  []string{"PATH=/usr/bin:/bin:/usr/local/go/bin", "GOCACHE=/tmp/go-cache", "HOME=/tmp"},
				Files: []*pb.Request_File{
					(&pb.Request_File_builder{
						Memory: (&pb.Request_MemoryFile_builder{Content: []byte{}}).Build(),
					}).Build(),
				},
				CopyIn: map[string]*pb.Request_File{
					fileName: (&pb.Request_File_builder{
						Cached: (&pb.Request_CachedFile_builder{FileID: fileResp.GetFileID()}).Build(),
					}).Build(),
				},
				CpuTimeLimit:    120 * 1000000000,
				ClockTimeLimit:  240 * 1000000000,
				MemoryLimit:     512 * 1024 * 1024,
				ProcLimit:       200,
				CopyOutCached: []*pb.Request_CmdCopyOutFile{
					(&pb.Request_CmdCopyOutFile_builder{Name: "a.out"}).Build(),
				},
				CopyOut: []*pb.Request_CmdCopyOutFile{
					(&pb.Request_CmdCopyOutFile_builder{Name: "stdout"}).Build(),
					(&pb.Request_CmdCopyOutFile_builder{Name: "stderr"}).Build(),
				},
			}).Build(),
		},
	}).Build()

	resp, err := ec.Exec(req)
	if err != nil {
		return nil, fmt.Errorf("compile Exec failed: %w", err)
	}

	r := resp.GetResults()[0]
	return &CompileResult{
		Status:     r.GetStatus().String(),
		Stdout:     string(r.GetFiles()["stdout"]),
		Stderr:     string(r.GetFiles()["stderr"]),
		FileID:     r.GetFileIDs()["a.out"],
		TimeUsed:   int64(r.GetTime()) / 1000,       // ns -> us
		MemoryUsed: int64(r.GetMemory()) / 1024,     // bytes -> KB
	}, nil
}

// Run sends a run request to go-judge.
func (c *SandboxClient) Run(args *RunArgs) (*ExecRunResult, error) {
	ec := c.ExecClient()

	cmdCopyIn := map[string]*pb.Request_File{}

	if args.ExecFileID != "" {
		cmdCopyIn["a.out"] = (&pb.Request_File_builder{
			Cached: (&pb.Request_CachedFile_builder{FileID: args.ExecFileID}).Build(),
		}).Build()
	}

	if args.ExecFileID == "" && len(args.SourceCode) > 0 {
		sourceName := args.SourceName
		if sourceName == "" {
			sourceName = "main.py"
		}
		fileResp, err := ec.FileAdd((&pb.FileContent_builder{
			Name:    sourceName,
			Content: args.SourceCode,
		}).Build())
		if err != nil {
			return nil, fmt.Errorf("FileAdd source failed: %w", err)
		}
		cmdCopyIn[sourceName] = (&pb.Request_File_builder{
			Cached: (&pb.Request_CachedFile_builder{FileID: fileResp.GetFileID()}).Build(),
		}).Build()
	}

	stdinContent := []byte(args.Stdin)

	cmdFiles := []*pb.Request_File{
		(&pb.Request_File_builder{
			Memory: (&pb.Request_MemoryFile_builder{Content: stdinContent}).Build(),
		}).Build(),
	}

	cmdStr := joinArgs(args.Args)
	shellCmd := fmt.Sprintf("exec %s > /w/stdout 2> /w/stderr", cmdStr)

	cmd := (&pb.Request_CmdType_builder{
		Args:  []string{"/bin/sh", "-c", shellCmd},
		Files: cmdFiles,
		CopyIn: cmdCopyIn,
		CpuTimeLimit:   uint64(args.TimeLimitMs) * 1000000,
		ClockTimeLimit: uint64(args.TimeLimitMs) * 2000000,
		MemoryLimit:    uint64(args.MemoryLimitKb) * 1024,
		ProcLimit:      50,
		CopyOut: []*pb.Request_CmdCopyOutFile{
			(&pb.Request_CmdCopyOutFile_builder{Name: "stdout"}).Build(),
			(&pb.Request_CmdCopyOutFile_builder{Name: "stderr"}).Build(),
		},
	}).Build()

	req := (&pb.Request_builder{Cmd: []*pb.Request_CmdType{cmd}}).Build()
	resp, err := ec.Exec(req)
	if err != nil {
		return nil, fmt.Errorf("run Exec failed: %w", err)
	}

	r := resp.GetResults()[0]
	return &ExecRunResult{
		Status:     r.GetStatus().String(),
		Stdout:     string(r.GetFiles()["stdout"]),
		Stderr:     string(r.GetFiles()["stderr"]),
		TimeUsed:   int64(r.GetTime()) / 1000,       // ns -> us
		MemoryUsed: int64(r.GetMemory()) / 1024,     // bytes -> KB
		ExitCode:   int(r.GetExitStatus()),
	}, nil
}

// ===== Types =====

type CompileResult struct {
	Status     string
	Stdout     string
	Stderr     string
	FileID     string
	TimeUsed   int64
	MemoryUsed int64
}

type RunArgs struct {
	Args          []string
	Stdin         string
	TimeLimitMs   int
	MemoryLimitKb int64
	ExecFileID    string
	SourceCode    []byte
	SourceName    string
}

type ExecRunResult struct {
	Status     string
	Stdout     string
	Stderr     string
	TimeUsed   int64
	MemoryUsed int64
	ExitCode   int
}

// ===== Helpers =====

func joinArgs(args []string) string {
	result := ""
	for i, a := range args {
		if i > 0 {
			result += " "
		}
		result += a
	}
	return result
}

func getLangConfig(lang string) (*langConfig, bool) {
	cfg, ok := langConfigs[lang]
	if ok {
		return &cfg, true
	}
	return nil, false
}

func getExtension(lang string) string {
	if cfg, ok := langConfigs[lang]; ok {
		return cfg.Extension
	}
	return ".txt"
}

type langConfig struct {
	Name        string
	CompileArgs []string
	RunArgs     []string
	Extension   string
}

var langConfigs = map[string]langConfig{
	"cpp":     {Name: "cpp", CompileArgs: []string{"g++", "-O2", "-o", "a.out", "main.cpp"}, RunArgs: []string{"./a.out"}, Extension: ".cpp"},
	"c":       {Name: "c", CompileArgs: []string{"gcc", "-O2", "-o", "a.out", "main.c"}, RunArgs: []string{"./a.out"}, Extension: ".c"},
	"python3": {Name: "python3", CompileArgs: []string{}, RunArgs: []string{"python3", "main.py"}, Extension: ".py"},
	"go":      {Name: "go", CompileArgs: []string{"/usr/bin/go", "build", "-p", "1", "-o", "a.out", "main.go"}, RunArgs: []string{"./a.out"}, Extension: ".go"},
}
