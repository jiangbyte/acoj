package judge

import (
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

type Executor struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
}

func NewExecutor(ctx context.Context, sandbox Sandbox) *Executor {
	return &Executor{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

func (e *Executor) Execute() *dto.JudgeResultDto {
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

	// 初始化cgroup并设置资源限制
	if err := e.initCgroupWithLimits(cgroupPath, e.Sandbox.Workspace.judgeSubmit.MaxTime, e.Sandbox.Workspace.judgeSubmit.MaxMemory); err != nil {
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("CGroup 资源设置失败: %v", err)
		return &result
	}

	// 获得执行命令
	config := e.Sandbox.Workspace.langConfig
	runCmd := make([]string, len(config.RunCmd))
	for i, part := range config.CompileCmd {
		runCmd[i] = strings.ReplaceAll(part, "{source}", e.Sandbox.Workspace.SourceFile)
		runCmd[i] = strings.ReplaceAll(runCmd[i], "{exec}", e.Sandbox.Workspace.BuildFile)
	}

	// 设置运行超时上下文
	timeout := time.Duration(e.Sandbox.Workspace.judgeSubmit.MaxTime)*time.Millisecond + 500*time.Millisecond // 额外500ms缓冲
	if timeout > 10*time.Second {
		timeout = 10 * time.Second // 最大10秒超时
	}
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	// 创建命令
	var cmd *exec.Cmd
	if len(runCmd) == 1 {
		cmd = exec.CommandContext(ctx, runCmd[0])
	} else {
		cmd = exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
	}
	cmd.Dir = e.Sandbox.Workspace.RunsPath

	// 设置进程隔离命名空间
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Cloneflags: syscall.CLONE_NEWNS | syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID | syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	// 测试用例遍历运行

	return nil
}

// initCgroupWithLimits 初始化cgroup并设置资源限制
func (s *Executor) initCgroupWithLimits(cgroupPath string, maxTime, maxMemory int) error {
	// 设置内存限制(转换为字节)
	if err := os.WriteFile(
		filepath.Join(cgroupPath, "memory.max"),
		[]byte(strconv.Itoa(maxMemory*1024)), 0644); err != nil {
		return err
	}

	// 设置CPU时间限制(转换为微秒)
	if err := os.WriteFile(
		filepath.Join(cgroupPath, "cpu.max"),
		[]byte(fmt.Sprintf("%d %d", maxTime*1000, 1000000)), 0644); err != nil {
		return err
	}

	// 启用内存统计
	if err := os.WriteFile(filepath.Join(cgroupPath, "memory.stat"), []byte(""), 0644); err != nil {
		return err
	}

	logx.Infof("已设置资源限制 - 最大内存: %d KB, 最大时间: %d ms", maxMemory, maxTime)
	return nil
}
