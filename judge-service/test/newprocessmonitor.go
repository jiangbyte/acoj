package main

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/shirou/gopsutil/v3/process"
)

// 定义监控结果结构体
type MonitorResult struct {
	MaxMemoryUsage uint64
	PeakCPULoad    float64
	TotalSamples   int
	IsKilled       bool
	Reason         string
}

func main() {
	startTime := time.Now() // 记录程序开始执行的时间

	// 用cmd启动一个进程
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	// 创建输出文件
	outputFile, err := os.Create("program_output.txt")
	if err != nil {
		fmt.Printf("创建输出文件失败: %v\n", err)
		return
	}
	defer outputFile.Close()

	cmd := exec.CommandContext(ctx, "/home/charlie/Desktop/test/test2/program")

	// 重定向标准输入
	input, err := cmd.StdinPipe()
	if err != nil {
		fmt.Printf("创建标准输入管道失败: %v\n", err)
		return
	}

	// 重定向标准输出和标准错误到文件
	cmd.Stdout = outputFile
	cmd.Stderr = outputFile

	err = cmd.Start()
	if err != nil {
		fmt.Printf("启动进程失败: %v\n", err)
		return
	}

	processStartTime := time.Now() // 记录进程启动时间
	pid := int32(cmd.Process.Pid)
	fmt.Printf("启动的进程PID: %d\n", pid)
	fmt.Printf("进程启动时间: %s\n", processStartTime.Format("15:04:05.000"))

	var wg sync.WaitGroup
	wg.Add(1)

	// 创建停止通道，用于通知监控协程结束
	stopChan := make(chan bool, 1)
	// 创建结果通道，用于接收监控结果
	resultChan := make(chan MonitorResult, 1)

	// 启动监控协程，定期获取进程信息
	go func() {
		defer wg.Done()
		monitorProcess(pid, processStartTime, cmd, stopChan, resultChan)
	}()

	// 向进程输入数字 1 和 2
	go func() {
		defer input.Close()
		// 等待进程完全启动
		time.Sleep(500 * time.Millisecond)
		fmt.Fprintln(input, "1")
		fmt.Fprintln(input, "2")
	}()

	// 等待进程完成
	err = cmd.Wait()
	processEndTime := time.Now() // 记录进程结束时间

	// 获得监控协程序的监控数据
	// 通知监控协程结束
	stopChan <- true

	// 等待监控协程完成并获取结果
	var monitorResult MonitorResult
	select {
	case result := <-resultChan:
		monitorResult = result
	case <-time.After(100 * time.Millisecond):
		// 超时保护，防止结果通道阻塞
		monitorResult = MonitorResult{Reason: "监控结果获取超时"}
	}

	wg.Wait() // 等待监控协程完全退出

	// 计算总执行时间
	totalDuration := time.Since(startTime)
	processDuration := processEndTime.Sub(processStartTime)

	if err != nil {
		fmt.Printf("进程执行错误: %v\n", err)
	}

	fmt.Printf("\n=== 最终统计 ===\n")
	fmt.Printf("进程启动时间: %s\n", processStartTime.Format("15:04:05.000"))
	fmt.Printf("进程结束时间: %s\n", processEndTime.Format("15:04:05.000"))
	fmt.Printf("进程执行时间: %d ms\n", processDuration.Milliseconds())
	fmt.Printf("总监控时间: %d ms\n", totalDuration.Milliseconds())
	fmt.Printf("进程退出码: %d\n", cmd.ProcessState.ExitCode())

	// 显示监控结果
	fmt.Printf("最大内存使用: %s\n", formatBytesKB(monitorResult.MaxMemoryUsage))
	fmt.Printf("峰值CPU负载: %.2f%%\n", monitorResult.PeakCPULoad)
	fmt.Printf("总监控采样次数: %d\n", monitorResult.TotalSamples)
	if monitorResult.IsKilled {
		fmt.Printf("进程终止原因: %s\n", monitorResult.Reason)
	}

	fmt.Println("输出已保存到 program_output.txt")
	fmt.Println("----------------------------------------------")
}

// 监控进程，定期获取信息
func monitorProcess(pid int32, processStartTime time.Time, cmd *exec.Cmd, stopChan chan bool, resultChan chan MonitorResult) {
	ticker := time.NewTicker(1 * time.Millisecond)
	defer ticker.Stop()

	var lastMemRSS uint64
	var processExited bool
	var maxMemUsage uint64
	var peakCPULoad float64
	var totalSamples int

	// 定义限制条件 - 使用KB和ms单位
	const (
		memoryLimitKB = 8 * 1024 // KB
		timeLimitMs   = 5 * 1000 // 毫秒
	)
	memoryLimitBytes := uint64(memoryLimitKB) * 1024
	timeLimit := time.Duration(timeLimitMs) * time.Millisecond

	fmt.Printf("监控设置: 内存限制=%d KB, 时间限制=%d ms\n", memoryLimitKB, timeLimitMs)
	fmt.Println("----------------------------------------------")

	var killReason string
	var isKilled bool

	for {
		select {
		case <-stopChan:
			// 收到停止信号，退出监控并发送结果
			resultChan <- MonitorResult{
				MaxMemoryUsage: maxMemUsage,
				PeakCPULoad:    peakCPULoad,
				TotalSamples:   totalSamples,
				IsKilled:       isKilled,
				Reason:         killReason,
			}
			return
		case <-ticker.C:
			totalSamples++

			// 检查时间限制
			elapsedTime := time.Since(processStartTime)
			if elapsedTime > timeLimit {
				killReason = fmt.Sprintf("时间超过限制! 运行时间: %d ms, 限制: %d ms",
					elapsedTime.Milliseconds(), timeLimitMs)
				isKilled = true
				fmt.Println(killReason)
				if cmd.Process != nil {
					cmd.Process.Kill()
				}
				resultChan <- MonitorResult{
					MaxMemoryUsage: maxMemUsage,
					PeakCPULoad:    peakCPULoad,
					TotalSamples:   totalSamples,
					IsKilled:       isKilled,
					Reason:         killReason,
				}
				return
			}

			peo, err := process.NewProcess(pid)
			if err != nil {
				// 进程可能已经退出
				if !processExited {
					fmt.Printf("进程 %d 已退出或无法访问\n", pid)
					processExited = true
				}
				continue
			}

			// 检查进程是否还在运行
			isRunning, err := peo.IsRunning()
			if err != nil || !isRunning {
				if !processExited {
					fmt.Printf("进程 %d 已停止运行\n", pid)
					processExited = true
				}
				continue
			}

			// 获取CPU百分比并更新峰值
			cpuPercent, err := peo.CPUPercent()
			if err == nil && cpuPercent > peakCPULoad {
				peakCPULoad = cpuPercent
			}

			// 获取进程信息并检查内存限制
			shouldStop, currentMem := displayProcessInfo(peo, processStartTime, &lastMemRSS, &maxMemUsage, memoryLimitBytes, timeLimitMs)
			if shouldStop {
				// 内存超限，停止进程
				killReason = fmt.Sprintf("内存使用超过%d KB限制", memoryLimitKB)
				isKilled = true
				fmt.Printf("内存使用超过%d KB限制，正在停止进程...\n", memoryLimitKB)
				if cmd.Process != nil {
					cmd.Process.Kill()
				}
				resultChan <- MonitorResult{
					MaxMemoryUsage: maxMemUsage,
					PeakCPULoad:    peakCPULoad,
					TotalSamples:   totalSamples,
					IsKilled:       isKilled,
					Reason:         killReason,
				}
				return
			}

			// 更新最大内存使用量
			if currentMem > maxMemUsage {
				maxMemUsage = currentMem
			}
		}
	}
}

// 显示进程信息并检查内存限制，返回是否应该停止进程和当前内存使用量
func displayProcessInfo(peo *process.Process, processStartTime time.Time, lastMemRSS *uint64, maxMemUsage *uint64, memoryLimitBytes uint64, timeLimitMs int) (bool, uint64) {
	// 获取进程名称
	// name, err := peo.Name()
	// if err != nil {
	// 	name = "未知"
	// }

	// 获取进程内存信息
	memInfo, err := peo.MemoryInfo()
	if err != nil {
		return false, 0
	}

	// 计算从进程启动到现在的时间（毫秒）
	elapsedTimeMs := time.Since(processStartTime).Milliseconds()

	// 获取CPU百分比
	// cpuPercent, err := peo.CPUPercent()
	// if err != nil {
	// 	cpuPercent = 0
	// }

	// 如果超过内存限制，则停止进程
	if memInfo.RSS > memoryLimitBytes {
		fmt.Printf("警告: 内存使用超过限制! 当前: %s, 限制: %s\n",
			formatBytesKB(memInfo.RSS), formatBytesKB(memoryLimitBytes))
		return true, memInfo.RSS // 返回true表示应该停止进程
	}

	// 检查内存是否有变化
	memChanged := memInfo.RSS != *lastMemRSS
	*lastMemRSS = memInfo.RSS

	// 计算剩余时间（毫秒）
	remainingTimeMs := int64(timeLimitMs) - elapsedTimeMs
	if remainingTimeMs < 0 {
		remainingTimeMs = 0
	}

	// 只在内存有变化或每隔几次时显示信息，避免输出过于频繁
	if memChanged || time.Now().Second()%2 == 0 {
		// 格式化输出进程信息
		// fmt.Printf("时间: %s (运行: %d ms, 剩余: %d ms)\n",
		// 	time.Now().Format("15:04:05.000"),
		// 	elapsedTimeMs,
		// 	remainingTimeMs)
		// fmt.Printf("PID: %d, 名称: %s\n", peo.Pid, name)
		// fmt.Printf("CPU使用率: %.2f%%, 内存: RSS=%s\n",
		// 	cpuPercent, formatBytesKB(memInfo.RSS))
		// fmt.Printf("最大内存使用: %s, 内存限制: %s\n",
		// 	formatBytesKB(*maxMemUsage), formatBytesKB(memoryLimitBytes))
		// fmt.Printf("时间限制: %d ms\n", timeLimitMs)
		// fmt.Println("----------------------------------------------")
	}

	return false, memInfo.RSS // 返回false表示不需要停止进程
}

// 辅助函数：将字节数格式化为KB单位
func formatBytesKB(bytes uint64) string {
	const KB = 1024
	return fmt.Sprintf("%d KB", bytes/KB)
}

// 获取进程内存使用量（RSS）
func getProcessMemory(pid int32) {
	// 读取 /proc/[pid]/statm 文件
	statmPath := fmt.Sprintf("/proc/%d/statm", pid)
	content, err := os.ReadFile(statmPath)
	if err != nil {
	}

	// 解析文件内容
	fields := strings.Fields(string(content))
	if len(fields) < 2 {
	}

	// 第二个字段是RSS（以页面为单位）
	rssPages, err := strconv.ParseUint(fields[1], 10, 64)
	if err != nil {
	}

	// 页面大小通常是4KB（4096字节）
	pageSize := uint64(os.Getpagesize())

	// 转换为字节
	rssBytes := rssPages * pageSize

	fmt.Printf("进程 %d 的内存使用量: %d KB\n", pid, rssBytes/1024)
}
