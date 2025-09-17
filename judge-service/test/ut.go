
// package main

// import (
// 	"context"
// 	"fmt"
// 	"os"
// 	"os/exec"
// 	"sync"
// 	"time"

// 	"github.com/shirou/gopsutil/v3/process"
// )

// func main() {
// 	startTime := time.Now() // 记录程序开始执行的时间
	
// 	// 用cmd启动一个进程
// 	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
// 	defer cancel()

// 	// 创建输出文件
// 	outputFile, err := os.Create("program_output.txt")
// 	if err != nil {
// 		fmt.Printf("创建输出文件失败: %v\n", err)
// 		return
// 	}
// 	defer outputFile.Close()

// 	cmd := exec.CommandContext(ctx, "/home/charlie/Desktop/test/program")
	
// 	// 重定向标准输入
// 	input, err := cmd.StdinPipe()
// 	if err != nil {
// 		fmt.Printf("创建标准输入管道失败: %v\n", err)
// 		return
// 	}
	
// 	// 重定向标准输出和标准错误到文件
// 	cmd.Stdout = outputFile
// 	cmd.Stderr = outputFile

// 	err = cmd.Start()
// 	if err != nil {
// 		fmt.Printf("启动进程失败: %v\n", err)
// 		return
// 	}

// 	processStartTime := time.Now() // 记录进程启动时间
// 	pid := int32(cmd.Process.Pid)
// 	fmt.Printf("启动的进程PID: %d\n", pid)
// 	fmt.Printf("进程启动时间: %s\n", processStartTime.Format("15:04:05.000"))

// 	var wg sync.WaitGroup
// 	wg.Add(1)
	
// 	// 创建停止通道，用于通知监控协程结束
// 	stopChan := make(chan bool, 1)
	
// 	// 启动监控协程，定期获取进程信息
// 	go func() {
// 		defer wg.Done()
// 		monitorProcess(pid, processStartTime, cmd, stopChan)
// 	}()

// 	// 向进程输入数字 1 和 2
// 	go func() {
// 		defer input.Close()
// 		// 等待进程完全启动
// 		time.Sleep(500 * time.Millisecond)
// 		fmt.Fprintln(input, "1")
// 		fmt.Fprintln(input, "2")
// 	}()

// 	// 等待进程完成
// 	err = cmd.Wait()
// 	processEndTime := time.Now() // 记录进程结束时间
	
// 	// TODO 获得监控协程序的监控数据

// 	// 通知监控协程结束
// 	stopChan <- true
	
// 	// 计算总执行时间
// 	totalDuration := time.Since(startTime)
// 	processDuration := processEndTime.Sub(processStartTime)

// 	if err != nil {
// 		fmt.Printf("进程执行错误: %v\n", err)
// 	}

// 	fmt.Printf("\n=== 最终统计 ===\n")
// 	fmt.Printf("进程启动时间: %s\n", processStartTime.Format("15:04:05.000"))
// 	fmt.Printf("进程结束时间: %s\n", processEndTime.Format("15:04:05.000"))
// 	fmt.Printf("进程执行时间: %v\n", processDuration.Round(time.Millisecond))
// 	fmt.Printf("总监控时间: %v\n", totalDuration.Round(time.Millisecond))
// 	fmt.Printf("进程退出码: %d\n", cmd.ProcessState.ExitCode())
// 	fmt.Println("输出已保存到 program_output.txt")
// 	fmt.Println("----------------------------------------------")
// }

// // 监控进程，定期获取信息
// func monitorProcess(pid int32, processStartTime time.Time, cmd *exec.Cmd, stopChan chan bool) {
// 	ticker := time.NewTicker(1 * time.Millisecond)
// 	defer ticker.Stop()
	
// 	var lastMemRSS uint64
// 	var processExited bool
// 	var maxMemUsage uint64
	
// 	// 定义限制条件
// 	const (
// 		memoryLimitMB = 800
// 		timeLimitSec  = 50
// 	)
// 	memoryLimitBytes := uint64(memoryLimitMB) * 1024 * 1024
// 	timeLimit := time.Duration(timeLimitSec) * time.Second
	
// 	fmt.Printf("监控设置: 内存限制=%dMB, 时间限制=%d秒\n", memoryLimitMB, timeLimitSec)
// 	fmt.Println("----------------------------------------------")
	
// 	for {
// 		select {
// 		case <-stopChan:
// 			// 收到停止信号，退出监控
// 			return
// 		case <-ticker.C:
// 			// 检查时间限制
// 			elapsedTime := time.Since(processStartTime)
// 			if elapsedTime > timeLimit {
// 				fmt.Printf("时间超过限制! 运行时间: %v, 限制: %v\n", 
// 					elapsedTime.Round(time.Millisecond), timeLimit)
// 				if cmd.Process != nil {
// 					cmd.Process.Kill()
// 				}
// 				return
// 			}
			
// 			peo, err := process.NewProcess(pid)
// 			if err != nil {
// 				// 进程可能已经退出
// 				if !processExited {
// 					fmt.Printf("进程 %d 已退出或无法访问\n", pid)
// 					processExited = true
// 				}
// 				continue
// 			}
			
// 			// 检查进程是否还在运行
// 			isRunning, err := peo.IsRunning()
// 			if err != nil || !isRunning {
// 				if !processExited {
// 					fmt.Printf("进程 %d 已停止运行\n", pid)
// 					processExited = true
// 				}
// 				continue
// 			}
			
// 			// 获取进程信息并检查内存限制
// 			shouldStop := displayProcessInfo(peo, processStartTime, &lastMemRSS, &maxMemUsage, memoryLimitBytes, timeLimit)
// 			if shouldStop {
// 				// 内存超限，停止进程
// 				fmt.Printf("内存使用超过%dMB限制，正在停止进程...\n", memoryLimitMB)
// 				if cmd.Process != nil {
// 					cmd.Process.Kill()
// 				}
// 				return
// 			}
// 		}
// 	}
// }

// // 显示进程信息并检查内存限制，返回是否应该停止进程
// func displayProcessInfo(peo *process.Process, processStartTime time.Time, lastMemRSS *uint64, maxMemUsage *uint64, memoryLimitBytes uint64, timeLimit time.Duration) bool {
// 	// 获取进程名称
// 	name, err := peo.Name()
// 	if err != nil {
// 		name = "未知"
// 	}

// 	// 获取进程内存信息
// 	memInfo, err := peo.MemoryInfo()
// 	if err != nil {
// 		return false
// 	}

// 	// 计算从进程启动到现在的时间
// 	elapsedTime := time.Since(processStartTime)

// 	// 获取CPU百分比
// 	cpuPercent, err := peo.CPUPercent()
// 	if err != nil {
// 		cpuPercent = 0
// 	}

// 	// 跟踪最大内存使用量
// 	if memInfo.RSS > *maxMemUsage {
// 		*maxMemUsage = memInfo.RSS
// 	}

// 	// 如果超过内存限制，则停止进程
// 	if memInfo.RSS > memoryLimitBytes {
// 		fmt.Printf("警告: 内存使用超过限制! 当前: %s, 限制: %s\n", 
// 			formatBytes(memInfo.RSS), formatBytes(memoryLimitBytes))
// 		return true // 返回true表示应该停止进程
// 	}

// 	// 检查内存是否有变化
// 	memChanged := memInfo.RSS != *lastMemRSS
// 	*lastMemRSS = memInfo.RSS

// 	// 计算剩余时间
// 	remainingTime := timeLimit - elapsedTime
// 	if remainingTime < 0 {
// 		remainingTime = 0
// 	}

// 	// 只在内存有变化或每隔几次时显示信息，避免输出过于频繁
// 	if memChanged || time.Now().Second()%2 == 0 {
// 		// 格式化输出进程信息
// 		fmt.Printf("时间: %s (运行: %v, 剩余: %v)\n", 
// 			time.Now().Format("15:04:05.000"), 
// 			elapsedTime.Round(time.Millisecond*100),
// 			remainingTime.Round(time.Millisecond*100))
// 		fmt.Printf("PID: %d, 名称: %s\n", peo.Pid, name)
// 		fmt.Printf("CPU使用率: %.2f%%, 内存: RSS=%s\n", 
// 			cpuPercent, formatBytes(memInfo.RSS))
// 		fmt.Printf("最大内存使用: %s, 内存限制: %s\n", 
// 			formatBytes(*maxMemUsage), formatBytes(memoryLimitBytes))
// 		fmt.Printf("时间限制: %v\n", timeLimit)
// 		fmt.Println("----------------------------------------------")
// 	}
	
// 	return false // 返回false表示不需要停止进程
// }

// // 辅助函数：将字节数格式化为易读的格式
// func formatBytes(bytes uint64) string {
// 	const (
// 		KB = 1024
// 		MB = KB * 1024
// 		GB = MB * 1024
// 	)

// 	switch {
// 	case bytes >= GB:
// 		return fmt.Sprintf("%.2f GB", float64(bytes)/float64(GB))
// 	case bytes >= MB:
// 		return fmt.Sprintf("%.2f MB", float64(bytes)/float64(MB))
// 	case bytes >= KB:
// 		return fmt.Sprintf("%.2f KB", float64(bytes)/float64(KB))
// 	default:
// 		return fmt.Sprintf("%d B", bytes)
// 	}
// }