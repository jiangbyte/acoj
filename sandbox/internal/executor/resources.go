package executor

import (
	"fmt"
	"syscall"

	"golang.org/x/sys/unix"
	"sandbox/internal/config"
)

/* 设置资源限制 */
func SetResourceLimits(resource config.Resource) error {
	// 设置栈大小限制
	if resource.MaxStack > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_STACK, &syscall.Rlimit{
			Cur: uint64(resource.MaxStack * 1024),
			Max: uint64(resource.MaxStack * 1024),
		}); err != nil {
			return fmt.Errorf("设置栈限制失败: %v", err)
		}
	}

	// 设置进程数限制
	if resource.MaxProcesses > 0 {
		if err := syscall.Setrlimit(unix.RLIMIT_NPROC, &syscall.Rlimit{
			Cur: uint64(resource.MaxProcesses),
			Max: uint64(resource.MaxProcesses),
		}); err != nil {
			return fmt.Errorf("设置进程数限制失败: %v", err)
		}
	}

	// 设置打开文件数限制
	if resource.MaxOpenFiles > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_NOFILE, &syscall.Rlimit{
			Cur: uint64(resource.MaxOpenFiles),
			Max: uint64(resource.MaxOpenFiles),
		}); err != nil {
			return fmt.Errorf("设置打开文件数限制失败: %v", err)
		}
	}

	// 设置文件大小限制
	if resource.MaxFileSize > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_FSIZE, &syscall.Rlimit{
			Cur: uint64(resource.MaxFileSize),
			Max: uint64(resource.MaxFileSize),
		}); err != nil {
			return fmt.Errorf("设置文件大小限制失败: %v", err)
		}
	}

	return nil
}