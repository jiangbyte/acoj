package utils

import (
	"crypto/rand"
	"encoding/base64"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

// createCgroup 创建cgroup
func CreateCgroup(maxMemory float64) (string, error) {
	cgroupName := GenerateCgroupName()
	cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)

	// 创建cgroup目录
	if err := os.MkdirAll(cgroupPath, 0755); err != nil {
		return "", fmt.Errorf("创建cgroup目录失败: %v", err)
	}

	// 设置内存限制（单位：字节）
	memoryLimit := maxMemory * 1024 // KB 转字节
	memoryMaxPath := filepath.Join(cgroupPath, "memory.max")
	if err := os.WriteFile(memoryMaxPath, []byte(strconv.FormatFloat(memoryLimit, 'f', -1, 64)), 0644); err != nil {
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
	return cgroupPath, nil
}

// createCgroup 创建cgroup
func CreateCgroupNoMemory() (string, error) {
	cgroupName := GenerateCgroupName()
	cgroupPath := filepath.Join("/sys/fs/cgroup", cgroupName)

	// 创建cgroup目录
	if err := os.MkdirAll(cgroupPath, 0755); err != nil {
		return "", fmt.Errorf("创建cgroup目录失败: %v", err)
	}

	// 禁用swap
	memorySwapMaxPath := filepath.Join(cgroupPath, "memory.swap.max")
	if err := os.WriteFile(memorySwapMaxPath, []byte("0"), 0644); err != nil {
		logx.Errorf("禁用swap失败: %v", err)
	}

	return cgroupPath, nil
}

// getMemoryUsage 从cgroup读取峰值内存使用
func GetMemoryUsage(cgroupPath string) (uint64, error) {
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

func GenerateCgroupName() string {
	pid := os.Getpid()
	timestamp := time.Now().Format("150405") // 时分秒

	// 生成3字节随机数
	randomBytes := make([]byte, 3)
	rand.Read(randomBytes)
	randomStr := base64.RawURLEncoding.EncodeToString(randomBytes)

	return fmt.Sprintf("judge-p%d-%s-%s", pid, timestamp, randomStr)
}

// setCgroupForProcess 将进程添加到cgroup
func SetCgroupForProcess(cgroupPath string, pid int) error {
	procsFile := filepath.Join(cgroupPath, "cgroup.procs")
	// 检查进程是否存在
	if _, err := os.FindProcess(pid); err != nil {
		return fmt.Errorf("进程不存在: %d", pid)
	}

	// 将进程PID写入cgroup.procs文件
	return os.WriteFile(procsFile, []byte(strconv.Itoa(pid)), 0644)
}

// cleanupCgroup 清理cgroup
func CleanupCgroup(cgroupPath string) {
	if err := os.RemoveAll(cgroupPath); err != nil {
		logx.Errorf("清理cgroup失败: %v", err)
	} else {
		logx.Infof("清理cgroup: %s", cgroupPath)
	}
}

// checkOOMEvent 检查cgroup是否发生了OOM事件
func CheckOOMEvent(cgroupPath string) bool {
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
