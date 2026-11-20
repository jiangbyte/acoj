package utils

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"

	"github.com/zeromicro/go-zero/core/logx"
)

const (
	SandboxModeAuto   = "auto"
	SandboxModeCgroup = "cgroup"
	SandboxModeSoft   = "soft"
)

var (
	sandboxModeMu sync.RWMutex
	sandboxMode   = SandboxModeSoft
)

// ProbeCgroupWritable 探测 /sys/fs/cgroup 是否可创建子目录
func ProbeCgroupWritable() bool {
	probeDir := filepath.Join("/sys/fs/cgroup", fmt.Sprintf("judge-probe-%d", os.Getpid()))
	if err := os.Mkdir(probeDir, 0755); err != nil {
		return false
	}
	_ = os.Remove(probeDir)
	return true
}

// ResolveSandboxMode 根据配置解析实际沙箱模式
func ResolveSandboxMode(configured string) string {
	mode := strings.ToLower(strings.TrimSpace(configured))
	if mode == "" {
		mode = SandboxModeAuto
	}

	switch mode {
	case SandboxModeCgroup:
		if !ProbeCgroupWritable() {
			logx.Errorf("Sandbox.Mode=cgroup 但 cgroup 不可写，强制降级为 soft")
			return SandboxModeSoft
		}
		return SandboxModeCgroup
	case SandboxModeSoft:
		return SandboxModeSoft
	case SandboxModeAuto:
		if ProbeCgroupWritable() {
			return SandboxModeCgroup
		}
		return SandboxModeSoft
	default:
		logx.Errorf("未知 Sandbox.Mode=%s，使用 auto", configured)
		if ProbeCgroupWritable() {
			return SandboxModeCgroup
		}
		return SandboxModeSoft
	}
}

// InitSandboxMode 在服务启动时解析并缓存沙箱模式
func InitSandboxMode(configured string) string {
	resolved := ResolveSandboxMode(configured)
	sandboxModeMu.Lock()
	sandboxMode = resolved
	sandboxModeMu.Unlock()

	switch resolved {
	case SandboxModeCgroup:
		logx.Infof("sandbox mode=cgroup, using cgroup v2 memory limits")
	case SandboxModeSoft:
		logx.Infof("sandbox mode=soft, cgroup unavailable, using /proc memory watcher")
	}
	return resolved
}

// GetSandboxMode 返回当前沙箱模式
func GetSandboxMode() string {
	sandboxModeMu.RLock()
	defer sandboxModeMu.RUnlock()
	return sandboxMode
}

// IsSoftSandbox 是否为 soft 降级模式
func IsSoftSandbox() bool {
	return GetSandboxMode() == SandboxModeSoft
}

// IsCgroupSandbox 是否为 cgroup 模式
func IsCgroupSandbox() bool {
	return GetSandboxMode() == SandboxModeCgroup
}
