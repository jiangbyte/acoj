package judge

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"judge-service/internal/dto"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type Compiler struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
}

// 创建编译器，传入上下文，沙箱
func NewCompiler(ctx context.Context, sandbox Sandbox) *Compiler {
	return &Compiler{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

// 实际执行编译
func (e *Compiler) Execute() (*dto.JudgeResultDto, error) {
	// 转换提交
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)

	// ==================================== 获得编译命令 ====================================
	config := e.Sandbox.Workspace.langConfig             // 获取语言配置
	compileCmd := make([]string, len(config.CompileCmd)) // 创建编译命令
	for i, part := range config.CompileCmd {
		compileCmd[i] = strings.ReplaceAll(part, "{source}", e.Sandbox.Workspace.SourceFile)
		compileCmd[i] = strings.ReplaceAll(compileCmd[i], "{exec}", e.Sandbox.Workspace.BuildFile)
	}
	logx.Infof("得到编译命令: %s", compileCmd)

	// ==================================== 创建命令 ====================================
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel() // 30秒超时

	var cmd *exec.Cmd
	if len(compileCmd) == 1 {
		cmd = exec.CommandContext(ctx, compileCmd[0])
	} else {
		cmd = exec.CommandContext(ctx, compileCmd[0], compileCmd[1:]...)
	}
	cmd.Dir = e.Sandbox.Workspace.BuildPath

	// ==================================== 设置进程隔离命名空间 ====================================
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Cloneflags: syscall.CLONE_NEWNS | syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID | syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	// ==================================== 收集输出 ====================================
	var output bytes.Buffer // 存储输出
	cmd.Stdout = &output    // 将输出重定向到缓冲区
	cmd.Stderr = &output    // 将错误重定向到缓冲区

	// ==================================== 创建临时cgroup ====================================
	cgroupName := "judge_" + strconv.Itoa(os.Getpid()) + "_" + strconv.Itoa(time.Now().Nanosecond())
	cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)
	if err := os.Mkdir(cgroupPath, 0755); err != nil {
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("CGroup 创建失败: %v", err)
		return &result, err
	}
	defer os.RemoveAll(cgroupPath)                          // 结束时删除cgroup
	startMem, startPeak := getCgroupMemoryUsage(cgroupPath) // 获取进程开始 内存使用量 峰值

	// ==================================== 启动进程 ====================================
	if err := cmd.Start(); err != nil {
		result.Status = dto.StatusCompilationError
		result.Message = fmt.Sprintf("启动编译进程失败: %v", err)
		return &result, err
	}

	// ==================================== 添加进程到cgroup ====================================
	if err := addProcessToCgroup(cgroupPath, cmd.Process.Pid); err != nil {
		cmd.Process.Kill()
		// 编译失败
		result.Status = dto.StatusCompilationError
		result.Message = fmt.Sprintf("%v\n%s", err, output.String())
		return &result, err
	}
	logx.Infof("进程已加入cgroup - PID: %d, cgroup路径: %s", cmd.Process.Pid, cgroupPath)

	// ==================================== 等待完成 ====================================
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
		return &result, errors.New("编译超时")
	case err := <-done:
		if err != nil {
			// 编译失败
			result.Status = dto.StatusCompilationError
			result.Message = fmt.Sprintf("%v\n%s", err, output.String())
			return &result, fmt.Errorf("%v\n%s", err, output.String())
		}
	}

	// ==================================== 结果返回 ====================================
	endMem, endPeak := getCgroupMemoryUsage(cgroupPath)                  // 获取进程结束 内存使用量 峰值
	memUsed := max(endMem-startMem, 0)                                   // 计算实际使用内存
	timeUsed := time.Since(e.Sandbox.Workspace.startTime).Milliseconds() // 计算用时
	logx.Infof("编译成功, 用时: %d ms 起始: %d KB, 结束: %d KB, 差值: %d KB 起始峰值: %d KB, 结束峰值: %d KB",
		timeUsed,
		startMem/1024,
		endMem/1024,
		memUsed/1024,
		startPeak/1024,
		endPeak/1024)

	// 返回成功结果
	result.MaxTime = int(timeUsed)         // 返回用时(ms)
	result.MaxMemory = int(memUsed / 1024) // 返回内存(kb)
	result.Status = dto.StatusCompiledOK
	result.Message = "编译成功"
	return &result, nil
}

// addProcessToCgroup 将进程添加到 cgroup
func addProcessToCgroup(cgroupPath string, pid int) error {
	// 将进程添加到 cgroup
	err := os.WriteFile(filepath.Join(cgroupPath, "cgroup.procs"),
		[]byte(strconv.Itoa(pid)), 0644)
	if err == nil {
		logx.Infof("进程 %d 已成功加入cgroup %s", pid, cgroupPath)
	}
	return err
}

// getCgroupMemoryUsage 获取cgroup内存使用量
func getCgroupMemoryUsage(cgroupPath string) (uint64, uint64) {
	// 读取内存使用量
	data, err := os.ReadFile(filepath.Join(cgroupPath, "memory.current"))
	if err != nil {
		return 0, 0
	}
	usage, _ := strconv.ParseUint(strings.TrimSpace(string(data)), 10, 64)

	// 读取内存峰值
	data, err = os.ReadFile(filepath.Join(cgroupPath, "memory.peak"))
	if err != nil {
		return usage, 0
	}
	peak, _ := strconv.ParseUint(strings.TrimSpace(string(data)), 10, 64)

	maxData, _ := os.ReadFile(filepath.Join(cgroupPath, "memory.stat"))
	// 打印测试
	logx.Debugf("内存使用量: %s", string(maxData))

	// 返回内存使用量和峰值
	return usage, peak
}
