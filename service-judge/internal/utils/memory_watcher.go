package utils

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

const defaultMemoryWatchInterval = 20 * time.Millisecond

// MemoryWatcher 通过 /proc 轮询进程内存，超限时杀死进程组
type MemoryWatcher struct {
	peak     atomic.Uint64
	exceeded atomic.Bool
	stopOnce sync.Once
	stopCh   chan struct{}
	doneCh   chan struct{}
	exceedCh chan struct{}
}

// StartMemoryWatcher 启动内存监视；limitBytes 为 0 时仅统计峰值不杀进程
func StartMemoryWatcher(pid int, limitBytes uint64, interval time.Duration) *MemoryWatcher {
	if interval <= 0 {
		interval = defaultMemoryWatchInterval
	}

	w := &MemoryWatcher{
		stopCh:   make(chan struct{}),
		doneCh:   make(chan struct{}),
		exceedCh: make(chan struct{}, 1),
	}

	go w.loop(pid, limitBytes, interval)
	return w
}

func (w *MemoryWatcher) loop(pid int, limitBytes uint64, interval time.Duration) {
	defer close(w.doneCh)

	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-w.stopCh:
			return
		case <-ticker.C:
			usage, err := ReadProcessMemoryBytes(pid)
			if err != nil {
				// 进程可能已退出
				continue
			}
			for {
				old := w.peak.Load()
				if usage <= old || w.peak.CompareAndSwap(old, usage) {
					break
				}
			}

			if limitBytes > 0 && usage > limitBytes && w.exceeded.CompareAndSwap(false, true) {
				logx.Infof("MLE by soft watcher: peak=%d limit=%d pid=%d", usage, limitBytes, pid)
				_ = syscall.Kill(-pid, syscall.SIGKILL)
				_ = syscall.Kill(pid, syscall.SIGKILL)
				select {
				case w.exceedCh <- struct{}{}:
				default:
				}
				return
			}
		}
	}
}

// Peak 返回观测到的峰值内存（字节）
func (w *MemoryWatcher) Peak() uint64 {
	return w.peak.Load()
}

// ExceededChan 超限通知（最多一次）
func (w *MemoryWatcher) ExceededChan() <-chan struct{} {
	return w.exceedCh
}

// Exceeded 是否已判定超限
func (w *MemoryWatcher) Exceeded() bool {
	return w.exceeded.Load()
}

func (w *MemoryWatcher) Stop() {
	if w == nil {
		return
	}
	w.stopOnce.Do(func() {
		close(w.stopCh)
		<-w.doneCh
	})
}

// ReadProcessMemoryBytes 读取进程 VmHWM（优先）或 VmRSS，单位字节
func ReadProcessMemoryBytes(pid int) (uint64, error) {
	f, err := os.Open(fmt.Sprintf("/proc/%d/status", pid))
	if err != nil {
		return 0, err
	}
	defer f.Close()

	var hwmKB, rssKB uint64
	hasHWM, hasRSS := false, false

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		line := scanner.Text()
		if strings.HasPrefix(line, "VmHWM:") {
			hwmKB, err = parseStatusKB(line)
			if err == nil {
				hasHWM = true
			}
		} else if strings.HasPrefix(line, "VmRSS:") {
			rssKB, err = parseStatusKB(line)
			if err == nil {
				hasRSS = true
			}
		}
		if hasHWM && hasRSS {
			break
		}
	}
	if err := scanner.Err(); err != nil {
		return 0, err
	}

	if hasHWM {
		return hwmKB * 1024, nil
	}
	if hasRSS {
		return rssKB * 1024, nil
	}
	return 0, fmt.Errorf("VmHWM/VmRSS not found for pid %d", pid)
}

func parseStatusKB(line string) (uint64, error) {
	fields := strings.Fields(line)
	if len(fields) < 2 {
		return 0, fmt.Errorf("invalid status line: %s", line)
	}
	return strconv.ParseUint(fields[1], 10, 64)
}

// MemoryLimitBytesFromKB 将题目 MaxMemory(KB) 转为字节，与 CreateCgroup 一致
func MemoryLimitBytesFromKB(maxMemoryKB float64) uint64 {
	if maxMemoryKB <= 0 {
		return 0
	}
	return uint64(maxMemoryKB * 1024)
}
