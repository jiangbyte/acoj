package utils

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"syscall"

	"github.com/zeromicro/go-zero/core/logx"
)

const (
	CgroupV1 = 1
	CgroupV2 = 2
)

type sandboxPlace struct {
	version      int
	memoryParent string
	cpuParent    string
	pidsParent   string // v2 与 memory 同目录；v1 为独立 pids 挂载
}

var (
	sandboxMu sync.RWMutex
	place     sandboxPlace
)

// InitSandboxMode 只接受 cgroup。找不到可记账的 cgroup v1 或 v2 时直接失败，不再降级。
func InitSandboxMode(configured string) error {
	mode := strings.ToLower(strings.TrimSpace(configured))
	if mode == "" {
		mode = "auto"
	}
	if mode == "soft" {
		logx.Errorf("Sandbox.Mode=soft 已删除，改为 cgroup")
		mode = "cgroup"
	}
	if mode != "auto" && mode != "cgroup" {
		return fmt.Errorf("未知 Sandbox.Mode=%s", configured)
	}

	// 1. 优先用当前进程所在的 cgroup v2，父目录要能写 memory.max，并且能读到 cpu.stat
	// 2. 没有 v2 时再找已经挂载的 cgroup v1 memory / cpuacct
	found, err := discoverSandbox()
	if err != nil {
		return err
	}
	sandboxMu.Lock()
	place = found
	sandboxMu.Unlock()
	logx.Infof("sandbox cgroup=v%d memory=%s cpu=%s pids=%s", found.version, found.memoryParent, found.cpuParent, found.pidsParent)
	return nil
}

// CgroupVersion 返回 1 或 2。
func CgroupVersion() int {
	sandboxMu.RLock()
	defer sandboxMu.RUnlock()
	return place.version
}

// OverrideSandbox 测试时指定层级，不走自动发现。pidsParent 为空时与 memoryParent 相同。
func OverrideSandbox(version int, memoryParent, cpuParent, pidsParent string) {
	if cpuParent == "" {
		cpuParent = memoryParent
	}
	if pidsParent == "" {
		pidsParent = memoryParent
	}
	sandboxMu.Lock()
	place = sandboxPlace{version: version, memoryParent: memoryParent, cpuParent: cpuParent, pidsParent: pidsParent}
	sandboxMu.Unlock()
}

func currentPlace() (sandboxPlace, error) {
	sandboxMu.RLock()
	defer sandboxMu.RUnlock()
	if place.memoryParent == "" || (place.version != CgroupV1 && place.version != CgroupV2) {
		return sandboxPlace{}, fmt.Errorf("cgroup 尚未初始化")
	}
	return place, nil
}

func discoverSandbox() (sandboxPlace, error) {
	if mem, ok := discoverV2Parent(); ok {
		return sandboxPlace{version: CgroupV2, memoryParent: mem, cpuParent: mem, pidsParent: mem}, nil
	}
	mem, cpu, pids, ok := discoverV1Mounts()
	if !ok {
		return sandboxPlace{}, fmt.Errorf("没有可写的 cgroup v1 或 v2 内存控制器")
	}
	if pids == "" {
		return sandboxPlace{}, fmt.Errorf("cgroup v1 未找到可写 pids 控制器，拒绝无 PID 上限启动")
	}
	return sandboxPlace{version: CgroupV1, memoryParent: mem, cpuParent: cpu, pidsParent: pids}, nil
}

// discoverV2Parent 从当前 cgroup 向上找能限制内存、且带 cpu.stat 的父目录。
func discoverV2Parent() (string, bool) {
	for _, parent := range v2ParentCandidates() {
		if probeV2Parent(parent) {
			return parent, true
		}
	}
	return "", false
}

func v2ParentCandidates() []string {
	data, err := os.ReadFile("/proc/self/cgroup")
	if err != nil {
		return nil
	}
	var parents []string
	for _, line := range strings.Split(string(data), "\n") {
		line = strings.TrimSpace(line)
		idx := strings.Index(line, "::")
		if idx < 0 {
			continue
		}
		rel := strings.Trim(line[idx+2:], "/")
		dir := "/sys/fs/cgroup"
		if rel != "" {
			dir = filepath.Join("/sys/fs/cgroup", rel)
		}
		for {
			parent := filepath.Dir(dir)
			if parent == dir {
				break
			}
			parents = append(parents, parent)
			if parent == "/sys/fs/cgroup" {
				break
			}
			dir = parent
		}
		break
	}
	return parents
}

func probeV2Parent(parent string) bool {
	info, err := os.Stat(parent)
	if err != nil || !info.IsDir() {
		return false
	}
	dir := filepath.Join(parent, fmt.Sprintf("judge-probe-%d", os.Getpid()))
	if err := os.Mkdir(dir, 0755); err != nil {
		return false
	}
	defer syscall.Rmdir(dir)
	if err := os.WriteFile(filepath.Join(dir, "memory.max"), []byte("1048576"), 0644); err != nil {
		return false
	}
	// cpu.stat 能读，CPU 时间才有内核计数，而不是再去采样 /proc
	if _, err := os.Stat(filepath.Join(dir, "cpu.stat")); err != nil {
		return false
	}
	// pids 控制器必须可写，否则无法 fail-closed 施加进程数上限
	if err := os.WriteFile(filepath.Join(dir, "pids.max"), []byte("64"), 0644); err != nil {
		return false
	}
	return true
}

// discoverV1Mounts 从 mountinfo 找出 cgroup v1 的 memory、cpuacct、pids 挂载点。
func discoverV1Mounts() (memoryMount, cpuMount, pidsMount string, ok bool) {
	data, err := os.ReadFile("/proc/self/mountinfo")
	if err != nil {
		return "", "", "", false
	}
	for _, line := range strings.Split(string(data), "\n") {
		parts := strings.Split(line, " - ")
		if len(parts) != 2 || !strings.Contains(parts[1], "cgroup ") {
			continue
		}
		fields := strings.Fields(parts[0])
		if len(fields) < 5 {
			continue
		}
		mountpoint := fields[4]
		super := parts[1]
		if strings.Contains(super, "memory") && memoryMount == "" && probeV1Memory(mountpoint) {
			memoryMount = mountpoint
		}
		if strings.Contains(super, "cpuacct") && cpuMount == "" && dirWritable(mountpoint) {
			cpuMount = mountpoint
		}
		if strings.Contains(super, "pids") && pidsMount == "" && probeV1Pids(mountpoint) {
			pidsMount = mountpoint
		}
	}
	if memoryMount == "" {
		return "", "", "", false
	}
	if cpuMount == "" {
		cpuMount = memoryMount
	}
	return memoryMount, cpuMount, pidsMount, true
}

func probeV1Pids(parent string) bool {
	if !dirWritable(parent) {
		return false
	}
	dir := filepath.Join(parent, fmt.Sprintf("judge-probe-%d", os.Getpid()))
	if err := os.Mkdir(dir, 0755); err != nil {
		return false
	}
	defer syscall.Rmdir(dir)
	return os.WriteFile(filepath.Join(dir, "pids.max"), []byte("64"), 0644) == nil
}

func probeV1Memory(parent string) bool {
	if !dirWritable(parent) {
		return false
	}
	dir := filepath.Join(parent, fmt.Sprintf("judge-probe-%d", os.Getpid()))
	if err := os.Mkdir(dir, 0755); err != nil {
		return false
	}
	defer syscall.Rmdir(dir)
	return os.WriteFile(filepath.Join(dir, "memory.limit_in_bytes"), []byte("1048576"), 0644) == nil
}

func dirWritable(parent string) bool {
	info, err := os.Stat(parent)
	if err != nil || !info.IsDir() {
		return false
	}
	dir := filepath.Join(parent, fmt.Sprintf("judge-wprobe-%d", os.Getpid()))
	if err := os.Mkdir(dir, 0755); err != nil {
		return false
	}
	return syscall.Rmdir(dir) == nil
}
