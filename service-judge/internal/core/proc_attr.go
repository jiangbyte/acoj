package core

import "syscall"

// buildSysProcAttr 仅保留给测试/兼容：进程组 + 可选 cgroup fd。
// 正式评测请走 utils.BuildIsolatePlan（网络命名空间 / 降权 / bwrap）。
func buildSysProcAttr(cgroupFD int) *syscall.SysProcAttr {
	attr := &syscall.SysProcAttr{Setpgid: true}
	if cgroupFD > 0 {
		attr.UseCgroupFD = true
		attr.CgroupFD = cgroupFD
	}
	return attr
}
