package utils

import (
	"os"
	"syscall"
	"time"
)

// DurationMS 把时长换成毫秒，保留两位小数，和评测结果的 decimal(10,2) 对齐。
func DurationMS(d time.Duration) float64 {
	ms := float64(d.Microseconds()) / 1000
	return float64(int64(ms*100+0.5)) / 100
}

// RusageOf 读取 wait 之后内核记在进程上的 CPU 时间和最大 RSS。
// Linux 的 ru_maxrss 单位是 KB。测试用它对照 cgroup 计数，评测结果不以它为准。
func RusageOf(ps *os.ProcessState) (cpu time.Duration, rssBytes uint64, ok bool) {
	if ps == nil || ps.SysUsage() == nil {
		return 0, 0, false
	}
	ru, isRusage := ps.SysUsage().(*syscall.Rusage)
	if !isRusage || ru == nil {
		return 0, 0, false
	}
	cpu = time.Duration(ru.Utime.Nano()) + time.Duration(ru.Stime.Nano())
	if ru.Maxrss > 0 {
		rssBytes = uint64(ru.Maxrss) * 1024
	}
	return cpu, rssBytes, true
}
