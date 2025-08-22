package judge

import (
	"bytes"
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
	// 转换提交
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)

	// ==================================== 获得执行命令 ====================================
	config := e.Sandbox.Workspace.langConfig     // 获取语言配置
	runCmd := make([]string, len(config.RunCmd)) // 创建执行命令
	for i, part := range config.RunCmd {
		runCmd[i] = strings.ReplaceAll(part, "{exec}", e.Sandbox.Workspace.BuildFile)
	}
	logx.Infof("得到运行命令: %s", runCmd)

	for i := range result.TestCase {
		// 获取当前测试用例的指针
		testCase := &result.TestCase[i]

		logx.Infof("开始测试第 %d 个测试用例, 输入 %s 输出 %s", i+1, testCase.Input, testCase.Except)

		// ==================================== 设置运行超时上下文 ====================================
		timeout := time.Duration(e.Sandbox.Workspace.judgeSubmit.MaxTime)*time.Millisecond + 500*time.Millisecond // 额外500ms缓冲
		if timeout > 10*time.Second {
			timeout = 10 * time.Second // 最大10秒超时
		} else if timeout < 10*time.Millisecond {
			timeout = 10 * time.Millisecond
		}
		ctx, cancel := context.WithTimeout(context.Background(), timeout)
		defer cancel()

		var cmd *exec.Cmd
		if len(runCmd) == 1 {
			cmd = exec.CommandContext(ctx, runCmd[0])
		} else {
			cmd = exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
		}
		// cmd.Dir = filepath.Join(e.Sandbox.Workspace.RunsPath, fmt.Sprint(i+1))

		// ==================================== 设置进程隔离命名空间 ====================================
		cmd.SysProcAttr = &syscall.SysProcAttr{
			Cloneflags: syscall.CLONE_NEWNS |
				syscall.CLONE_NEWUTS |
				syscall.CLONE_NEWPID |
				syscall.CLONE_NEWNET |
				syscall.CLONE_NEWIPC,
			Unshareflags: syscall.CLONE_NEWNS,
		}

		// ==================================== 创建临时cgroup ====================================
		cgroupName := "judge_" + strconv.Itoa(os.Getpid()) + "_" + strconv.Itoa(time.Now().Nanosecond())
		cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)
		if err := os.Mkdir(cgroupPath, 0755); err != nil {
			testCase.Status = dto.StatusSystemError
			testCase.Message = fmt.Sprintf("CGroup 创建失败: %v", err)
			return &result, err
		}
		defer os.RemoveAll(cgroupPath)

		// 初始化cgroup并设置资源限制
		if err := e.initCgroupWithLimits(cgroupPath, e.Sandbox.Workspace.judgeSubmit.MaxTime, e.Sandbox.Workspace.judgeSubmit.MaxMemory); err != nil {
			return &result, err
		}

		// 记录开始时间和资源状态
		startTime := time.Now()
		startMem, startPeak := getCgroupMemoryUsage(cgroupPath)
		logx.Infof("运行开始 - 时间: %v, 初始内存: %d KB, 初始峰值内存: %d KB",
			startTime.Format("2006-01-02 15:04:05.000"),
			startMem/1024,
			startPeak/1024)

		// ==================================== 输入，输出，错误重定向 ====================================
		// 重定向标准输入
		cmd.Stdin = strings.NewReader(testCase.Input)
		// 创建输出缓冲区
		var stdoutBuf, stderrBuf bytes.Buffer
		cmd.Stdout = &stdoutBuf
		cmd.Stderr = &stderrBuf

		// ==================================== 启动进程 ====================================
		if err := cmd.Start(); err != nil {
			testCase.Status = dto.StatusRuntimeError
			testCase.Message = fmt.Sprintf("启动执行进程失败: %v", err)
			logx.Errorf("启动执行进程失败: %v", err)
			return &result, err
		}

		// ==================================== 添加进程到cgroup ====================================
		if err := addProcessToCgroup(cgroupPath, cmd.Process.Pid); err != nil {
			cmd.Process.Kill()
			// 执行失败
			testCase.Status = dto.StatusRuntimeError
			testCase.Message = fmt.Sprintf("%v\n%s", err, stderrBuf.String())
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
			cmd.Process.Kill()
		case err := <-done:
			if err != nil {
				if exitErr, ok := err.(*exec.ExitError); ok {
					// 处理进程退出错误
					testCase.Status = dto.StatusRuntimeError
					testCase.Message = fmt.Sprintf("进程异常退出: %v", exitErr)
					return &result, nil
				}
			}
		}

		// 获取资源使用情况
		endMem, endPeak := getCgroupMemoryUsage(cgroupPath)
		memUsed := endPeak // 使用峰值内存作为内存使用量
		// 计算用时
		timeUsed := int(time.Since(startTime).Milliseconds())
		// 检查资源限制（时间检查）
		if timeUsed > e.Sandbox.Workspace.judgeSubmit.MaxTime {
			testCase.Status = dto.StatusTimeLimitExceeded
			testCase.Message = fmt.Sprintf("用时超出限制: %d ms", timeUsed)
			return &result, nil
		}
		if memUsed > uint64(e.Sandbox.Workspace.judgeSubmit.MaxMemory)*1024 {
			testCase.Status = dto.StatusMemoryLimitExceeded
			testCase.Message = fmt.Sprintf("内存超出限制: %d KB", memUsed/1024)
			return &result, nil
		}

		// 读取用户输出
		testCase.Output = stdoutBuf.String()
		logx.Infof("用户输出结果: %s", stdoutBuf.String())

		testCase.MaxTime = timeUsed
		testCase.MaxMemory = int(endMem / 1024) // 转换为KB

		logx.Infof("执行成功, 用时: %d ms 起始: %d KB, 结束: %d KB, 差值: %d KB 起始峰值: %d KB, 结束峰值: %d KB",
			timeUsed,
			startMem/1024,
			endMem/1024,
			memUsed/1024,
			startPeak/1024,
			endPeak/1024)

		logx.Infof("开始测试第 %d/%d 个测试用例", i+1, len(result.TestCase))
		logx.Infof("输入内容: %s", strings.Replace(testCase.Input, "\n", "\\n", -1))
		logx.Infof("期望输出: %s", strings.Replace(testCase.Except, "\n", "\\n", -1))
		logx.Infof("用户输出: %s", strings.Replace(testCase.Output, "\n", "\\n", -1))
		logx.Infof("最大时间: %d", testCase.MaxTime)
		logx.Infof("最大内存: %d", testCase.MaxMemory)
		logx.Infof("消息: %s", testCase.Message)
		logx.Infof("当前状态: %s", testCase.Status)

		// 规范化输出和用户输出
		normalizedExcept := normalizeLineEndings(testCase.Except)
		normalizedUser := normalizeLineEndings(testCase.Output)

		// 去除末尾空行后再比较
		trimmedExpected := strings.TrimRight(normalizedExcept, "\n")
		trimmedUser := strings.TrimRight(normalizedUser, "\n")

		if trimmedExpected == trimmedUser {
			result.Status = dto.StatusAccepted
		} else {
			result.Status = dto.StatusWrongAnswer
		}
	}

	return &result, nil
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

// normalizeLineEndings 将不同风格的换行符统一为\n
func normalizeLineEndings(str string) string {
	// 将\r\n替换为\n
	str = strings.ReplaceAll(str, "\r\n", "\n")
	// 将单独的\r也替换为\n（处理Mac OS旧格式）
	str = strings.ReplaceAll(str, "\r", "\n")
	return str
}
