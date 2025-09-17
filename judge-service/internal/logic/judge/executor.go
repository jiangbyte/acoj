package judge

import (
	"context"
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

type Executor struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
}

// 创建执行器，传入上下文，沙箱
func NewExecutor(ctx context.Context, sandbox Sandbox) *Executor {
	return &Executor{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

// 实际执行
func (e *Executor) Execute() (*dto.JudgeResultDto, error) {
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)
	runCmd := e.getRunCommand()
	cgroupPath, err := e.createCgroup()
	if err != nil {
		logx.Errorf("创建cgroup失败: %v", err)
		return nil, fmt.Errorf("创建cgroup失败: %v", err)
	}
	defer e.cleanupCgroup(cgroupPath)

	for i := range result.TestCase {
		testCase := &result.TestCase[i]

		timeout := time.Duration(e.Sandbox.Workspace.judgeSubmit.MaxTime)*time.Millisecond + 500*time.Millisecond // 额外500ms缓冲
		ctx, cancel := context.WithTimeout(context.Background(), timeout)
		defer cancel()

		var cmd *exec.Cmd
		if len(runCmd) == 1 {
			cmd = exec.CommandContext(ctx, runCmd[0])
		} else {
			cmd = exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
		}

		// 使用Start()创建进程但不等待完成
		err := cmd.Start()
		if err != nil {
			testCase.Status = "Runtime Error"
			testCase.Message = fmt.Sprintf("启动进程失败: %v", err)
			logx.Errorf("测试用例 %d 运行失败: %v", i+1, testCase.Message)
			continue
		}

		// 立即暂停进程
		syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

		// 将进程加入cgroup
		if err := e.setCgroupForProcess(cgroupPath, cmd.Process.Pid); err != nil {
			cmd.Process.Kill()
			testCase.Status = "System Error"
			testCase.Message = fmt.Sprintf("设置cgroup失败: %v", err)
			logx.Errorf("测试用例 %d 运行失败: %v", i+1, testCase.Message)
			continue
		}

		// 恢复进程
		syscall.Kill(cmd.Process.Pid, syscall.SIGCONT)

		// 等待进程结束
		done := make(chan error, 1)
		go func() {
			done <- cmd.Wait()
		}()

		select {
		case <-ctx.Done():
			cmd.Process.Kill()
		case err := <-done:
			logx.Infof("测试用例 %d 进程结束", i+1)

			time.Sleep(10 * time.Millisecond)

			memoryUsed, _ := e.getMemoryUsage(cgroupPath)
			logx.Infof("测试用例 %d 完成, 内存使用: %d bytes (峰值), 程序退出状态: %v",
				i+1, memoryUsed, err)
			// KB显示
			logx.Infof("测试用例 %d 内存使用: %s (峰值)", i+1, formatBytesKB(memoryUsed))

			// 检查OOM事件
			oomOccurred := e.checkOOMEvent(cgroupPath)
			logx.Infof("测试用例 %d OOM事件发生: %v", i+1, oomOccurred)

		}
	}

	return &result, nil
}

// getRunCommand 获取执行命令
func (e *Executor) getRunCommand() []string {
	config := e.Sandbox.Workspace.langConfig
	runCmd := make([]string, len(config.RunCmd))
	for i, part := range config.RunCmd {
		runCmd[i] = strings.ReplaceAll(part, "{exec}", e.Sandbox.Workspace.BuildFile)
	}

	logx.Infof("得到运行命令: %s", runCmd)
	return runCmd
}

// createCgroup 创建cgroup，可在结束时主动销毁
func (e *Executor) createCgroup() (string, error) {
	// 生成唯一的cgroup路径
	cgroupName := fmt.Sprintf("judge-%d", time.Now().UnixNano())
	cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)

	// 创建cgroup目录
	if err := os.MkdirAll(cgroupPath, 0755); err != nil {
		return "", fmt.Errorf("创建cgroup目录失败: %v", err)
	}

	// 设置内存限制（单位：字节）
	memoryLimit := e.Sandbox.Workspace.judgeSubmit.MaxMemory * 1024 // KB 转字节
	memoryMaxPath := filepath.Join(cgroupPath, "memory.max")
	if err := os.WriteFile(memoryMaxPath, []byte(strconv.Itoa(memoryLimit)), 0644); err != nil {
		return "", fmt.Errorf("设置内存限制失败: %v", err)
	}

   // 禁用swap
    memorySwapMaxPath := filepath.Join(cgroupPath, "memory.swap.max")
    if err := os.WriteFile(memorySwapMaxPath, []byte("0"), 0644); err != nil {
        logx.Errorf("禁用swap失败: %v", err)
    }

	// 验证内存限制是否设置成功
	setLimit, err := os.ReadFile(memoryMaxPath)
	if err != nil {
		return "", fmt.Errorf("验证内存限制失败: %v", err)
	}
	logx.Infof("设置的内存限制: %s bytes", strings.TrimSpace(string(setLimit)))

	logx.Infof("创建cgroup: %s", cgroupPath)
	return cgroupPath, nil
}

// setCgroupForProcess 将进程添加到cgroup
func (e *Executor) setCgroupForProcess(cgroupPath string, pid int) error {
	procsFile := filepath.Join(cgroupPath, "cgroup.procs")

	// 等待进程真正创建，确保PID有效
	time.Sleep(10 * time.Millisecond)

	// 检查进程是否存在
	if _, err := os.FindProcess(pid); err != nil {
		return fmt.Errorf("进程不存在: %d", pid)
	}

	// 将进程PID写入cgroup.procs文件
	return os.WriteFile(procsFile, []byte(strconv.Itoa(pid)), 0644)
}

// cleanupCgroup 清理cgroup
func (e *Executor) cleanupCgroup(cgroupPath string) {
	if err := os.RemoveAll(cgroupPath); err != nil {
		logx.Errorf("清理cgroup失败: %v", err)
	} else {
		logx.Infof("清理cgroup: %s", cgroupPath)
	}
}

// getMemoryUsage 从cgroup读取峰值内存使用
func (e *Executor) getMemoryUsage(cgroupPath string) (uint64, error) {
	// 首先检查cgroup目录是否存在
	if _, err := os.Stat(cgroupPath); os.IsNotExist(err) {
		logx.Errorf("cgroup目录不存在: %s", cgroupPath)
		return 0, fmt.Errorf("cgroup目录不存在")
	}

	// 读取 memory.peak 文件
	peakPath := filepath.Join(cgroupPath, "memory.peak")
	content, err := os.ReadFile(peakPath)
	if err != nil {
		logx.Errorf("读取memory.peak失败: %s, 错误: %v", peakPath, err)
		return 0, fmt.Errorf("读取memory.peak失败: %v", err)
	}

	peakContent := strings.TrimSpace(string(content))
	logx.Infof("memory.peak 内容: '%s'", peakContent)

	// 读取 memory.current 文件
	currentPath := filepath.Join(cgroupPath, "memory.current")
	currentContent, err := os.ReadFile(currentPath)
	if err != nil {
		logx.Errorf("读取memory.current失败: %v", err)
	} else {
		logx.Infof("memory.current 内容: '%s'", strings.TrimSpace(string(currentContent)))
	}

	// 解析内存使用量
	memoryUsed, err := strconv.ParseUint(peakContent, 10, 64)
	if err != nil {
		logx.Errorf("解析内存使用量失败: '%s', 错误: %v", peakContent, err)
		return 0, fmt.Errorf("解析内存使用量失败: %v", err)
	}

	logx.Infof("解析后的内存使用量: %d bytes", memoryUsed)
	return memoryUsed, nil
}

func formatBytesKB(bytes uint64) string {
	return fmt.Sprintf("%.2f KB", float64(bytes)/1024)
}

// checkOOMEvent 检查cgroup是否发生了OOM事件
func (e *Executor) checkOOMEvent(cgroupPath string) bool {
	eventsPath := filepath.Join(cgroupPath, "memory.events")
	content, err := os.ReadFile(eventsPath)
	if err != nil {
		logx.Errorf("读取memory.events失败: %v", err)
		return false
	}

	// 解析事件文件
	lines := strings.Split(string(content), "\n")
	for _, line := range lines {
		if strings.HasPrefix(line, "oom ") {
			parts := strings.Split(line, " ")
			if len(parts) >= 2 {
				count, err := strconv.Atoi(strings.TrimSpace(parts[1]))
				if err == nil && count > 0 {
					logx.Infof("检测到OOM事件: %s", line)
					return true
				}
			}
		}
	}

	return false
}
