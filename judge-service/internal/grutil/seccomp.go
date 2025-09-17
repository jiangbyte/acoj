package grutil

import (
	"fmt"
	"syscall"

	seccomp "github.com/seccomp/libseccomp-golang"
)

// setupSeccompBlacklist 创建黑名单模式的 seccomp 过滤器
func SetupSeccompBlacklist() error {
	// 默认允许所有系统调用，只对特定系统调用返回错误
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return fmt.Errorf("创建seccomp过滤器失败: %v", err)
	}
	defer filter.Release()

	// 设置黑名单：不允许的系统调用及其动作
	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// 不允许的危险系统调用列表
	deniedSyscalls := []string{
		// 进程控制
		"fork", "vfork", "clone", "kill", "tkill", "tgkill",
		"execve", "execveat", "setns", "unshare",

		// 网络相关（如果需要完全隔离）
		"socket", "bind", "connect", "listen", "accept",
		"sendto", "recvfrom", "sendmsg", "recvmsg",
		"shutdown", "setsockopt", "getsockopt",

		// 文件系统操作（危险）
		"mount", "umount", "umount2", "pivot_root",
		"chroot", "swapon", "swapoff",

		// 系统管理
		"reboot", "sethostname", "setdomainname",
		"init_module", "finit_module", "delete_module",
		"iopl", "ioperm",

		// 权限相关
		"setuid", "setgid", "setreuid", "setregid",
		"setresuid", "setresgid", "setfsuid", "setfsgid",
		"capset",

		// 进程间通信（危险）
		"ptrace", "process_vm_readv", "process_vm_writev",

		// 内存管理（危险）
		"mremap", "remap_file_pages",

		// 其他危险调用
		"acct", "keyctl", "add_key", "request_key",
		"syslog", "quotactl", "nfsservctl",
	}

	for _, syscallName := range deniedSyscalls {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err != nil {
			// 如果系统调用不存在（可能是架构不支持），跳过
			continue
		}
		if err := filter.AddRule(sc, denyAction); err != nil {
			return fmt.Errorf("添加系统调用规则失败 %s: %v", syscallName, err)
		}
	}

	// 加载过滤器
	if err := filter.Load(); err != nil {
		return fmt.Errorf("加载seccomp过滤器失败: %v", err)
	}

	return nil
}

func SetupLanguageSeccomp(lang string) error {
	switch lang {
	case "c", "cpp":
		return SetupCppSeccomp()
	default:
		return SetupDefaultSeccomp()
	}
}

func SetupCppSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// C/C++ 黑名单：禁止危险系统调用
	cppDenied := []string{
		// 进程控制
		"fork", "vfork", "clone", "kill", "tkill", "tgkill",
		"execve", "execveat", "setns", "unshare",

		// // 网络相关
		// "socket", "bind", "connect", "listen", "accept",
		// "sendto", "recvfrom", "sendmsg", "recvmsg",
		// "shutdown", "setsockopt", "getsockopt",

		// // 文件系统危险操作
		// "mount", "umount", "umount2", "pivot_root",
		// "chroot", "swapon", "swapoff",

		// // 系统管理
		// "reboot", "sethostname", "setdomainname",
		// "init_module", "finit_module", "delete_module",
		// "iopl", "ioperm",

		// // 权限相关
		// "setuid", "setgid", "setreuid", "setregid",
		// "setresuid", "setresgid", "setfsuid", "setfsgid",
		// "capset",

		// // 调试和进程间通信
		// "ptrace", "process_vm_readv", "process_vm_writev",
		// "perf_event_open",

		// // 其他危险调用
		// "acct", "keyctl", "add_key", "request_key",
		// "syslog", "quotactl", "nfsservctl",

		// // 时间设置
		// "settimeofday", "adjtimex", "clock_settime",
	}

	for _, syscallName := range cppDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}

func SetupPythonSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// Python 黑名单：禁止执行外部程序和网络访问
	pythonDenied := []string{
		// 进程执行
		"execve", "execveat", "fork", "vfork", "clone",

		// 网络
		"socket", "bind", "connect", "listen", "accept",
		"sendto", "recvfrom", "sendmsg", "recvmsg",

		// 系统管理
		"reboot", "mount", "umount", "umount2",
		"init_module", "finit_module", "delete_module",

		// 权限
		"setuid", "setgid", "setreuid", "setregid",
		"setresuid", "setresgid",

		// 进程控制
		"kill", "tkill", "tgkill",
	}

	for _, syscallName := range pythonDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}

func SetupJavaSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// Java 黑名单：限制JVM可能滥用的系统调用
	javaDenied := []string{
		// 进程创建
		"fork", "vfork", "clone",

		// 网络（如果需要完全隔离）
		"socket", "bind", "connect", "listen", "accept",

		// 系统管理
		"reboot", "mount", "umount",
		"init_module", "finit_module", "delete_module",

		// 调试
		"ptrace", "process_vm_readv", "process_vm_writev",

		// 权限
		"setuid", "setgid", "setreuid", "setregid",
	}

	for _, syscallName := range javaDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}

func SetupGolangSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// Go 语言黑名单
	golangDenied := []string{
		// 进程控制
		"fork", "vfork", "clone",
		"execve", "execveat",

		// 网络（如果需要隔离）
		"socket", "bind", "connect", "listen", "accept",

		// 系统管理
		"reboot", "mount", "umount",
		"init_module", "finit_module", "delete_module",

		// 权限
		"setuid", "setgid", "setreuid", "setregid",
	}

	for _, syscallName := range golangDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}

func SetupDefaultSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// 默认黑名单：最危险的系统调用
	defaultDenied := []string{
		// 进程执行和控制
		"execve", "execveat", "fork", "vfork", "clone",
		"kill", "tkill", "tgkill",

		// 系统管理
		"reboot", "mount", "umount", "umount2",
		"init_module", "finit_module", "delete_module",
		"iopl", "ioperm",

		// 权限提升
		"setuid", "setgid", "setreuid", "setregid",
		"setresuid", "setresgid", "setfsuid", "setfsgid",
		"capset",

		// 调试和内存访问
		"ptrace", "process_vm_readv", "process_vm_writev",
		"perf_event_open",

		// 其他危险操作
		"acct", "keyctl", "add_key", "request_key",
		"syslog", "chroot", "pivot_root",
	}

	for _, syscallName := range defaultDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}

// SetupStrictSeccomp 严格模式：禁止所有非必要系统调用
func SetupStrictSeccomp() error {
	filter, err := seccomp.NewFilter(seccomp.ActAllow)
	if err != nil {
		return err
	}
	defer filter.Release()

	denyAction := seccomp.ActErrno.SetReturnCode(int16(syscall.EPERM))

	// 严格黑名单：禁止几乎所有可能危险的系统调用
	strictDenied := []string{
		// 进程相关
		"fork", "vfork", "clone", "execve", "execveat",
		"kill", "tkill", "tgkill", "setns", "unshare",

		// 网络
		"socket", "bind", "connect", "listen", "accept",
		"sendto", "recvfrom", "sendmsg", "recvmsg",
		"shutdown", "setsockopt", "getsockopt", "socketpair",

		// 文件系统
		"mount", "umount", "umount2", "pivot_root", "chroot",
		"swapon", "swapoff", "ioctl",

		// 系统管理
		"reboot", "sethostname", "setdomainname",
		"init_module", "finit_module", "delete_module",
		"iopl", "ioperm", "sysctl",

		// 权限
		"setuid", "setgid", "setreuid", "setregid",
		"setresuid", "setresgid", "setfsuid", "setfsgid",
		"capset", "setgroups",

		// 调试
		"ptrace", "process_vm_readv", "process_vm_writev",
		"perf_event_open",

		// 内存管理
		"mremap", "remap_file_pages", "mbind", "set_mempolicy",

		// 其他
		"acct", "keyctl", "add_key", "request_key",
		"syslog", "quotactl", "nfsservctl",
		"settimeofday", "adjtimex", "clock_settime",
	}

	for _, syscallName := range strictDenied {
		sc, err := seccomp.GetSyscallFromName(syscallName)
		if err == nil {
			filter.AddRule(sc, denyAction)
		}
	}

	return filter.Load()
}
