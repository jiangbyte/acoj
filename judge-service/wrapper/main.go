// package main

// import (
// 	"fmt"
// 	"os"
// 	"os/exec"
// 	"syscall"

// 	seccomp "github.com/seccomp/libseccomp-golang"
// )

// /**
// # 编译为当前系统架构
// go build -o wrapper main.go

// # 或者编译为静态链接的可执行文件（推荐，避免依赖问题）
// go build -ldflags="-s -w" -o wrapper main.go
// */

// func main() {
// 	if len(os.Args) < 2 {
// 		fmt.Println("Usage: wrapper <command> [args...]")
// 		os.Exit(1)
// 	}

// 	// 设置seccomp过滤器
// 	lang := os.Getenv("JUDGE_LANG")
// 	if lang != "" {
// 		if err := setupSeccomp(lang); err != nil {
// 			fmt.Printf("Failed to setup seccomp: %v\n", err)
// 			os.Exit(1)
// 		}
// 	}

// 	// 执行实际命令
// 	cmd := exec.Command(os.Args[1], os.Args[2:]...)
// 	cmd.Stdin = os.Stdin
// 	cmd.Stdout = os.Stdout
// 	cmd.Stderr = os.Stderr

// 	if err := cmd.Run(); err != nil {
// 		if exitError, ok := err.(*exec.ExitError); ok {
// 			os.Exit(exitError.ExitCode())
// 		}
// 		os.Exit(1)
// 	}
// }

// func setupSeccomp(lang string) error {
// 	switch lang {
// 	case "c", "cpp":
// 		return setupCppSeccomp()
// 	default:
// 		return setupDefaultSeccomp()
// 	}
// }

// func setupCppSeccomp() error {
// 	filter, err := seccomp.NewFilter(seccomp.ActAllow)
// 	if err != nil {
// 		return err
// 	}
// 	defer filter.Release()

// 	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

// 	cppDenied := []string{
// 		"ptrace", "process_vm_readv", "process_vm_writev",
// 		"perf_event_open", "iopl", "ioperm",
// 		"execve", "execveat", "fork", "vfork", "clone",
// 	}

// 	for _, syscallName := range cppDenied {
// 		sc, err := seccomp.GetSyscallFromName(syscallName)
// 		if err == nil {
// 			filter.AddRule(sc, denyAction)
// 		}
// 	}

// 	return filter.Load()
// }

// func setupDefaultSeccomp() error {
// 	// 默认实现
// 	return nil
// }

package main

import (
	"fmt"
	"os"
	"os/exec"
	"strings"
	"syscall"

	seccomp "github.com/seccomp/libseccomp-golang"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: wrapper <command> [args...]")
		os.Exit(1)
	}

	// 设置seccomp过滤器
	lang := os.Getenv("JUDGE_LANG")
	if lang != "" {
		if err := setupSeccomp(lang); err != nil {
			fmt.Printf("SECCOMP SETUP FAILED: %v\n", err)
			os.Exit(126) // 126 = command cannot execute
		}
		fmt.Printf("SECCOMP enabled for language: %s\n", lang)
	} else {
		fmt.Println("SECCOMP not enabled (no JUDGE_LANG set)")
	}

	// 执行实际命令
	cmd := exec.Command(os.Args[1], os.Args[2:]...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	err := cmd.Run()
	if err != nil {
		if exitError, ok := err.(*exec.ExitError); ok {
			// 检查是否是由于seccomp导致的失败
			if exitError.ExitCode() == -1 {
				fmt.Printf("COMMAND FAILED DUE TO SECCOMP RESTRICTION\n")
				os.Exit(1)
			}
			os.Exit(exitError.ExitCode())
		}
		fmt.Printf("COMMAND EXECUTION FAILED: %v\n", err)
		os.Exit(127)
	}

	os.Exit(0)
}

func setupSeccomp(lang string) error {
	lang = strings.ToLower(lang)

	switch lang {
	case "c", "cpp":
		return setupCppSeccomp()
	default:
		return setupDefaultSeccomp()
	}
}

func setupCppSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return fmt.Errorf("create filter failed: %v", err)
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	cppDenied := []string{
		"fork", "vfork", "clone", // 进程创建
		"execve", "execveat", // 程序执行
		"ptrace",                                // 调试
		"process_vm_readv", "process_vm_writev", // 进程内存访问
		"perf_event_open", // 性能监控
		"iopl", "ioperm",  // I/O 端口访问
		"kcmp",             // 进程比较
		"setns", "unshare", // 命名空间操作
		"mount", "umount", "umount2", // 文件系统挂载
		"chroot", "pivot_root", // 根目录更改
		"swapon", "swapoff", // 交换空间
		"syslog",                        // 系统日志
		"reboot",                        // 重启系统
		"kexec_load", "kexec_file_load", // kexec 加载
		"init_module", "finit_module", "delete_module", // 内核模块
	}

	fmt.Printf("Setting up C++ seccomp with %d denied syscalls\n", len(cppDenied))

	for _, syscallName := range cppDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err != nil {
			// 系统调用可能不存在于当前架构，跳过
			continue
		}
		if err := filter.AddRule(sc, denyAction); err != nil {
			return fmt.Errorf("add rule for %s failed: %v", syscallName, err)
		}
	}

	if err := filter.Load(); err != nil {
		return fmt.Errorf("load filter failed: %v", err)
	}

	fmt.Println("C++ seccomp filter loaded successfully")
	return nil
}

func setupDefaultSeccomp() error {
	fmt.Println("Using default seccomp (no restrictions)")
	return nil
}
