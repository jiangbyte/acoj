package core

import (
	"syscall"
)

// buildSysProcAttr soft 模式仅 Setpgid，避免无特权环境下 namespace 创建失败
func buildSysProcAttr(soft bool) *syscall.SysProcAttr {
	if soft {
		return &syscall.SysProcAttr{
			Setpgid: true,
		}
	}
	return &syscall.SysProcAttr{
		Setpgid: true,
		Cloneflags: syscall.CLONE_NEWNS |
			syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}
}
