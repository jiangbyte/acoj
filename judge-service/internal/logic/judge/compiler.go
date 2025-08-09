package judge

import (
	"bytes"
	"context"
	"fmt"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/dto"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"time"
)

type Compiler struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
}

func NewCompiler(ctx context.Context, sandbox Sandbox) *Compiler {
	return &Compiler{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

func (e *Compiler) Execute() *dto.JudgeResultDto {
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)

	// 创建临时cgroup
	cgroupName := "judge_" + strconv.Itoa(os.Getpid()) + "_" + strconv.Itoa(time.Now().Nanosecond())
	cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)
	if err := os.Mkdir(cgroupPath, 0755); err != nil {
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("CGroup 创建失败: %v", err)
		return &result
	}
	defer os.RemoveAll(cgroupPath)

	// 获得编译命令
	config := e.Sandbox.Workspace.langConfig
	compileCmd := make([]string, len(config.CompileCmd))
	for i, part := range config.CompileCmd {
		compileCmd[i] = strings.ReplaceAll(part, "{source}", e.Sandbox.Workspace.SourceFile)
		compileCmd[i] = strings.ReplaceAll(compileCmd[i], "{exec}", e.Sandbox.Workspace.BuildFile)
	}
	logx.Infof("得到编译命令: %s", compileCmd)
	// [g++, /tmp/judge_workspace/submissions/49f5114e477d4e56bbfbad7b01499e9f/source/cpp.cpp, -o, /tmp/judge_workspace/submissions/49f5114e477d4e56bbfbad7b01499e9f/build/Main]

	// 设置编译超时上下文
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	// 创建命令
	var cmd *exec.Cmd
	if len(compileCmd) == 1 {
		cmd = exec.CommandContext(ctx, compileCmd[0])
	} else {
		cmd = exec.CommandContext(ctx, compileCmd[0], compileCmd[1:]...)
	}
	cmd.Dir = e.Sandbox.Workspace.RunsPath

	// 设置进程隔离命名空间
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Cloneflags: syscall.CLONE_NEWNS | syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID | syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	// 收集输出
	var output bytes.Buffer
	cmd.Stdout = &output
	cmd.Stderr = &output

	startMem, startPeak, err := getCgroupMemoryUsage(cgroupPath)

	// 启动进程
	if err := cmd.Start(); err != nil {
		result.Status = dto.StatusCompilationError
		result.Message = fmt.Sprintf("启动编译进程失败: %v", err)
		return &result
	}

	// 加入cgroup
	if err := addProcessToCgroup(cgroupPath, cmd.Process.Pid); err != nil {
		cmd.Process.Kill()
		// 编译失败
		result.Status = dto.StatusCompilationError
		result.Message = fmt.Sprintf("%v\n%s", err, output.String())
		return &result
	}
	logx.Infof("进程已加入cgroup - PID: %d, cgroup路径: %s", cmd.Process.Pid, cgroupPath)

	// 等待完成
	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		// 超时处理
		cmd.Process.Kill()
		result.Status = dto.StatusCompilationError
		result.Message = "编译超时(30秒)"
		return &result
	case err := <-done:
		if err != nil {
			// 编译失败
			result.Status = dto.StatusCompilationError
			result.Message = fmt.Sprintf("%v\n%s", err, output.String())
			return &result
		}
	}

	// 获取资源使用情况
	endMem, endPeak, err := getCgroupMemoryUsage(cgroupPath)
	if err != nil {

	}
	memUsed := max(endMem-startMem, 0)

	// 计算用时
	timeUsed := time.Since(e.Sandbox.Workspace.startTime).Milliseconds()
	logx.Infof("编译成功, 用时: %d ms", timeUsed)
	// 记录详细的资源使用情况
	logx.Infof("内存使用详情 - 起始: %d KB, 结束: %d KB, 差值: %d KB",
		startMem/1024, endMem/1024, memUsed/1024)
	logx.Infof("峰值内存使用 - 起始峰值: %d KB, 结束峰值: %d KB",
		startPeak/1024, endPeak/1024)

	// 返回成功结果
	result.MaxTime = int(timeUsed)         // 返回用时(ms)
	result.MaxMemory = int(memUsed / 1024) // 返回内存(kb)
	result.Status = dto.StatusCompiledOK
	result.Message = "编译成功"
	return &result
}

// addProcessToCgroup 将进程添加到 cgroup
func addProcessToCgroup(cgroupPath string, pid int) error {
	err := os.WriteFile(filepath.Join(cgroupPath, "cgroup.procs"),
		[]byte(strconv.Itoa(pid)), 0644)
	if err == nil {
		logx.Infof("进程 %d 已成功加入cgroup %s", pid, cgroupPath)
	}
	return err
}

// getCgroupMemoryUsage 获取cgroup内存使用量
func getCgroupMemoryUsage(cgroupPath string) (uint64, uint64, error) {
	data, err := os.ReadFile(filepath.Join(cgroupPath, "memory.current"))
	if err != nil {
		return 0, 0, err
	}
	usage, _ := strconv.ParseUint(strings.TrimSpace(string(data)), 10, 64)

	data, err = os.ReadFile(filepath.Join(cgroupPath, "memory.peak"))
	if err != nil {
		return usage, 0, err
	}
	peak, _ := strconv.ParseUint(strings.TrimSpace(string(data)), 10, 64)

	return usage, peak, nil
}
