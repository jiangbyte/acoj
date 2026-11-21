package core

import "syscall"

// buildSysProcAttr 只建进程组。cgroupFD > 0 时进程在 clone 当下就进入该组，
// 启动阶段的内存也会记进 cgroup 峰值。资源限制只走 cgroup，不再用 /proc 轮询。
func buildSysProcAttr(cgroupFD int) *syscall.SysProcAttr {
	attr := &syscall.SysProcAttr{Setpgid: true}
	if cgroupFD > 0 {
		attr.UseCgroupFD = true
		attr.CgroupFD = cgroupFD
	}
	return attr
}
