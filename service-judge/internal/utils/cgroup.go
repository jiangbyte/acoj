package utils

import (
	"crypto/rand"
	"encoding/base64"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

// cgroup v2 的 CLONE_INTO_CGROUP 要求目录 fd 用 O_PATH 打开。amd64 的 syscall 包没有这个常量。
const oPath = 0x200000

// Group 一次评测使用的 cgroup。v2 只有一个目录；v1 的内存和 cpuacct 可能是两个目录。
type Group struct {
	Version int
	Dir     string
	CPUDir  string
	fd      int
}

// CreateCgroup 按题目内存上限（KB）建组。v2 写 memory.max，v1 写 memory.limit_in_bytes。
func CreateCgroup(maxMemoryKB float64) (*Group, error) {
	limitBytes := uint64(maxMemoryKB * 1024)
	if limitBytes == 0 {
		return nil, fmt.Errorf("内存上限无效: %.2f KB", maxMemoryKB)
	}
	group, err := createGroup()
	if err != nil {
		return nil, err
	}
	if err := group.applyMemoryLimit(limitBytes); err != nil {
		group.Close()
		return nil, err
	}
	return group, nil
}

// CreateCgroupNoMemory 只记账、不设上限，编译阶段用来读峰值和 CPU 时间。
func CreateCgroupNoMemory() (*Group, error) {
	return createGroup()
}

func createGroup() (*Group, error) {
	where, err := currentPlace()
	if err != nil {
		return nil, err
	}
	name := GenerateCgroupName()
	group := &Group{
		Version: where.version,
		Dir:     filepath.Join(where.memoryParent, name),
		CPUDir:  filepath.Join(where.cpuParent, name),
	}
	if group.CPUDir == "" {
		group.CPUDir = group.Dir
	}

	// 1. 先建内存目录，v1 的 cpuacct 是另一棵树时再平行建一个同名目录
	if err := os.Mkdir(group.Dir, 0755); err != nil {
		return nil, fmt.Errorf("创建 cgroup 失败: %w", err)
	}
	if group.CPUDir != group.Dir {
		if err := os.Mkdir(group.CPUDir, 0755); err != nil {
			_ = os.Remove(group.Dir)
			return nil, fmt.Errorf("创建 cpu cgroup 失败: %w", err)
		}
	}

	// 2. v2 打开目录 fd，进程在 clone 时就进组，启动阶段的内存也会记进 memory.peak
	if group.Version == CgroupV2 {
		fd, openErr := syscall.Open(group.Dir, oPath|syscall.O_CLOEXEC, 0)
		if openErr != nil {
			group.removeDirs()
			return nil, fmt.Errorf("打开 cgroup 目录失败: %w", openErr)
		}
		group.fd = fd
	}
	return group, nil
}

func (g *Group) applyMemoryLimit(limitBytes uint64) error {
	text := strconv.FormatUint(limitBytes, 10)
	if g.Version == CgroupV2 {
		// 关掉 swap，峰值只反映物理内存；oom.group 让超限时整组一起退出
		if err := os.WriteFile(filepath.Join(g.Dir, "memory.swap.max"), []byte("0"), 0644); err != nil {
			logx.Errorf("禁用 swap 失败: %v", err)
		}
		if err := os.WriteFile(filepath.Join(g.Dir, "memory.max"), []byte(text), 0644); err != nil {
			return fmt.Errorf("设置 memory.max 失败: %w", err)
		}
		if err := os.WriteFile(filepath.Join(g.Dir, "memory.oom.group"), []byte("1"), 0644); err != nil {
			logx.Errorf("设置 oom.group 失败: %v", err)
		}
		return nil
	}
	if err := os.WriteFile(filepath.Join(g.Dir, "memory.limit_in_bytes"), []byte(text), 0644); err != nil {
		return fmt.Errorf("设置 memory.limit_in_bytes 失败: %w", err)
	}
	_ = os.WriteFile(filepath.Join(g.Dir, "memory.swappiness"), []byte("0"), 0644)
	// memsw 不存在时忽略。存在时必须不小于 memory.limit，否则写内存上限会失败
	_ = os.WriteFile(filepath.Join(g.Dir, "memory.memsw.limit_in_bytes"), []byte(text), 0644)
	return nil
}

// FD 返回 v2 clone 用的目录 fd。v1 为 0，调用方改走 AddProc。
func (g *Group) FD() int {
	if g == nil {
		return 0
	}
	return g.fd
}

// AddProc 把进程写进 cgroup。v2 已经用 fd 进入时不必再调用。
func (g *Group) AddProc(pid int) error {
	if err := writePID(g.Dir, pid); err != nil {
		return err
	}
	if g.CPUDir != g.Dir {
		if err := writePID(g.CPUDir, pid); err != nil {
			return err
		}
	}
	return nil
}

func writePID(dir string, pid int) error {
	text := strconv.Itoa(pid)
	procs := filepath.Join(dir, "cgroup.procs")
	if err := os.WriteFile(procs, []byte(text), 0644); err != nil {
		tasks := filepath.Join(dir, "tasks")
		if err2 := os.WriteFile(tasks, []byte(text), 0644); err2 != nil {
			return fmt.Errorf("写入 cgroup 进程失败: %v", err)
		}
	}
	return nil
}

// MemoryPeak 读取内核记下的内存高水位，单位字节。
func (g *Group) MemoryPeak() (uint64, error) {
	if g == nil || g.Dir == "" {
		return 0, fmt.Errorf("cgroup 为空")
	}
	if g.Version == CgroupV2 {
		peak, peakErr := readCgroupUint(filepath.Join(g.Dir, "memory.peak"))
		current, curErr := readCgroupUint(filepath.Join(g.Dir, "memory.current"))
		if peakErr != nil && curErr != nil {
			return 0, peakErr
		}
		if current > peak {
			return current, nil
		}
		return peak, nil
	}
	return readCgroupUint(filepath.Join(g.Dir, "memory.max_usage_in_bytes"))
}

// CPUTime 读取 cgroup 累计 CPU 时间。v2 用 cpu.stat 的 usage_usec，v1 用 cpuacct.usage。
func (g *Group) CPUTime() (time.Duration, error) {
	dir := g.CPUDir
	if dir == "" {
		dir = g.Dir
	}
	if g.Version == CgroupV2 {
		content, err := os.ReadFile(filepath.Join(dir, "cpu.stat"))
		if err != nil {
			return 0, err
		}
		for _, line := range strings.Split(string(content), "\n") {
			fields := strings.Fields(line)
			if len(fields) == 2 && fields[0] == "usage_usec" {
				usec, convErr := strconv.ParseInt(fields[1], 10, 64)
				if convErr != nil {
					return 0, convErr
				}
				return time.Duration(usec) * time.Microsecond, nil
			}
		}
		return 0, fmt.Errorf("cpu.stat 没有 usage_usec")
	}
	nsec, err := readCgroupUint(filepath.Join(dir, "cpuacct.usage"))
	if err != nil {
		return 0, err
	}
	return time.Duration(nsec), nil
}

// OOMKilled 这组里是否发生过 OOM。
func (g *Group) OOMKilled() bool {
	if g == nil {
		return false
	}
	if g.Version == CgroupV2 {
		content, err := os.ReadFile(filepath.Join(g.Dir, "memory.events"))
		if err != nil {
			return false
		}
		for _, line := range strings.Split(string(content), "\n") {
			fields := strings.Fields(line)
			if len(fields) < 2 {
				continue
			}
			switch fields[0] {
			case "oom", "oom_kill", "oom_group_kill":
				count, convErr := strconv.Atoi(fields[1])
				if convErr == nil && count > 0 {
					return true
				}
			}
		}
		return false
	}
	content, err := os.ReadFile(filepath.Join(g.Dir, "memory.oom_control"))
	if err != nil {
		return false
	}
	for _, line := range strings.Split(string(content), "\n") {
		fields := strings.Fields(line)
		if len(fields) == 2 && fields[0] == "oom_kill" {
			count, convErr := strconv.Atoi(fields[1])
			return convErr == nil && count > 0
		}
	}
	return false
}

// WatchCPU 按 cgroup 的 CPU 计数杀掉超时进程。返回的函数停止轮询。
func (g *Group) WatchCPU(limit time.Duration, pgid int) func() {
	if g == nil || limit <= 0 || pgid <= 0 {
		return func() {}
	}
	stop := make(chan struct{})
	var once sync.Once
	go func() {
		ticker := time.NewTicker(5 * time.Millisecond)
		defer ticker.Stop()
		for {
			select {
			case <-stop:
				return
			case <-ticker.C:
				cpu, err := g.CPUTime()
				if err == nil && cpu > limit {
					_ = syscall.Kill(-pgid, syscall.SIGKILL)
					_ = syscall.Kill(pgid, syscall.SIGKILL)
					return
				}
			}
		}
	}()
	return func() {
		once.Do(func() { close(stop) })
	}
}

// Close 关掉目录 fd，清掉残留进程，再删目录。
func (g *Group) Close() {
	if g == nil {
		return
	}
	if g.fd > 0 {
		_ = syscall.Close(g.fd)
		g.fd = 0
	}
	if g.Version == CgroupV2 && g.Dir != "" {
		_ = os.WriteFile(filepath.Join(g.Dir, "cgroup.kill"), []byte("1"), 0644)
	} else {
		g.killMembers(g.Dir)
		if g.CPUDir != g.Dir {
			g.killMembers(g.CPUDir)
		}
	}
	g.removeDirs()
}

func (g *Group) killMembers(dir string) {
	if dir == "" {
		return
	}
	content, err := os.ReadFile(filepath.Join(dir, "cgroup.procs"))
	if err != nil {
		content, _ = os.ReadFile(filepath.Join(dir, "tasks"))
	}
	for _, field := range strings.Fields(string(content)) {
		pid, convErr := strconv.Atoi(field)
		if convErr == nil && pid > 0 {
			_ = syscall.Kill(pid, syscall.SIGKILL)
		}
	}
}

func (g *Group) removeDirs() {
	removeCgroupDir(g.Dir)
	if g.CPUDir != g.Dir {
		removeCgroupDir(g.CPUDir)
	}
}

func removeCgroupDir(dir string) {
	if dir == "" {
		return
	}
	var err error
	for i := 0; i < 20; i++ {
		err = syscall.Rmdir(dir)
		if err == nil || os.IsNotExist(err) {
			return
		}
		time.Sleep(10 * time.Millisecond)
	}
	logx.Errorf("清理 cgroup 失败: %s: %v", dir, err)
}

func readCgroupUint(path string) (uint64, error) {
	content, err := os.ReadFile(path)
	if err != nil {
		return 0, err
	}
	text := strings.TrimSpace(string(content))
	if text == "" || text == "max" {
		return 0, fmt.Errorf("%s 没有数值", path)
	}
	return strconv.ParseUint(text, 10, 64)
}

func GenerateCgroupName() string {
	randomBytes := make([]byte, 3)
	_, _ = rand.Read(randomBytes)
	randomStr := base64.RawURLEncoding.EncodeToString(randomBytes)
	return fmt.Sprintf("judge-p%d-%s-%s", os.Getpid(), time.Now().Format("150405"), randomStr)
}

// MemoryLimitBytesFromKB 将题目 MaxMemory(KB) 转为字节。
func MemoryLimitBytesFromKB(maxMemoryKB float64) uint64 {
	if maxMemoryKB <= 0 {
		return 0
	}
	return uint64(maxMemoryKB * 1024)
}
