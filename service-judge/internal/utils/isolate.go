package utils

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"syscall"

	"service-judge/internal/config"

	"github.com/zeromicro/go-zero/core/logx"
)

const boxRoot = "/box"

var lookPath = exec.LookPath

// IsolatePlan 隔离后的启动参数与 SysProcAttr。
type IsolatePlan struct {
	Argv []string
	Attr *syscall.SysProcAttr
	// Dir 非空时作为 cmd.Dir（非 bwrap 回退路径）。
	Dir  string
	Mode string // bwrap | clone
}

// BuildIsolatePlan 为编译/运行构造隔离启动计划。
// rootPath 为提交工作区根（含 source/build/runs）；workDir 为进程 cwd（通常 runs）。
func BuildIsolatePlan(argv []string, rootPath, workDir string, cfg config.SandboxConfig, cgroupFD int) (*IsolatePlan, error) {
	if len(argv) == 0 {
		return nil, fmt.Errorf("隔离命令为空")
	}
	rootAbs, err := filepath.Abs(rootPath)
	if err != nil {
		return nil, fmt.Errorf("解析工作区失败: %w", err)
	}
	workAbs, err := filepath.Abs(workDir)
	if err != nil {
		return nil, fmt.Errorf("解析工作目录失败: %w", err)
	}

	// 仅显式 off 走 clone；on/auto/空均要求 bwrap，缺失则 fail-closed（禁止无目录 jail 回退）
	if !shouldUseBwrap(cfg.UseBwrap) {
		return buildClonePlan(argv, workAbs, cfg, cgroupFD)
	}
	plan, err := buildBwrapPlan(argv, rootAbs, workAbs, cfg, cgroupFD)
	if err != nil {
		return nil, fmt.Errorf("bwrap 隔离必需但不可用: %w", err)
	}
	return plan, nil
}

func shouldUseBwrap(mode string) bool {
	mode = strings.ToLower(strings.TrimSpace(mode))
	switch mode {
	case "off", "false", "0", "no":
		return false
	default: // on / auto / 空：默认强制 bwrap
		return true
	}
}

func buildBwrapPlan(argv []string, rootAbs, workAbs string, cfg config.SandboxConfig, cgroupFD int) (*IsolatePlan, error) {
	bwrapPath, err := lookPath("bwrap")
	if err != nil {
		return nil, err
	}
	chdirRel, err := filepath.Rel(rootAbs, workAbs)
	if err != nil || strings.HasPrefix(chdirRel, "..") {
		return nil, fmt.Errorf("工作目录不在工作区内: work=%s root=%s", workAbs, rootAbs)
	}

	rewritten := rewriteArgvPaths(argv, rootAbs, boxRoot)
	bwrapArgs := []string{
		bwrapPath,
		"--die-with-parent",
		"--new-session",
		"--ro-bind", "/usr", "/usr",
		"--ro-bind-try", "/usr/local", "/usr/local",
		"--ro-bind", "/lib", "/lib",
		"--ro-bind-try", "/lib64", "/lib64",
		"--ro-bind", "/bin", "/bin",
		"--ro-bind-try", "/sbin", "/sbin",
		"--ro-bind-try", "/etc/ld.so.cache", "/etc/ld.so.cache",
		"--ro-bind-try", "/etc/alternatives", "/etc/alternatives",
		"--ro-bind-try", "/etc/java-17-openjdk", "/etc/java-17-openjdk",
		"--ro-bind-try", "/etc/ssl", "/etc/ssl",
		"--ro-bind-try", "/etc/ca-certificates", "/etc/ca-certificates",
		"--dev", "/dev",
		"--proc", "/proc",
		"--tmpfs", "/tmp",
		"--bind", rootAbs, boxRoot,
		"--chdir", filepath.Join(boxRoot, chdirRel),
		"--clearenv",
		"--setenv", "PATH", "/usr/local/go/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
		"--setenv", "HOME", boxRoot,
		"--setenv", "LANG", "C.UTF-8",
		"--setenv", "LC_ALL", "C.UTF-8",
		"--setenv", "GOCACHE", "/tmp/go-cache",
		"--setenv", "GOTMPDIR", "/tmp",
		"--setenv", "GOROOT", "/usr/local/go",
		// 1. 目录 jail 内再卸掉能力，避免保留宿主 CAP
		"--cap-drop", "ALL",
	}
	if cfg.DisableNetwork {
		bwrapArgs = append(bwrapArgs, "--unshare-net")
	}
	dropPriv := cfg.DropPrivileges && os.Geteuid() == 0
	if dropPriv {
		// 2. 先以 nobody 身份启动 bwrap（SysProcAttr.Credential），再进 user ns；
		//    禁止 root 下 --uid（会把 ns uid 映回宿主 root，假降权）。
		bwrapArgs = append(bwrapArgs, "--unshare-user")
	}
	bwrapArgs = append(bwrapArgs, "--")
	bwrapArgs = append(bwrapArgs, rewritten...)

	attr := &syscall.SysProcAttr{Setpgid: true}
	attachCgroup(attr, cgroupFD)
	if dropPriv {
		attr.Credential = &syscall.Credential{
			Uid:         cfg.Uid,
			Gid:         cfg.Gid,
			NoSetGroups: true,
		}
	}
	logx.Infof("sandbox isolate=bwrap netOff=%v dropPriv=%v uid=%d root=%s",
		cfg.DisableNetwork, dropPriv, cfg.Uid, rootAbs)
	return &IsolatePlan{Argv: bwrapArgs, Attr: attr, Mode: "bwrap"}, nil
}

func buildClonePlan(argv []string, workAbs string, cfg config.SandboxConfig, cgroupFD int) (*IsolatePlan, error) {
	attr := &syscall.SysProcAttr{Setpgid: true}
	attachCgroup(attr, cgroupFD)
	if cfg.DisableNetwork {
		attr.Cloneflags |= syscall.CLONE_NEWNET
	}
	if cfg.DropPrivileges && os.Geteuid() == 0 {
		attr.Credential = &syscall.Credential{
			Uid:         cfg.Uid,
			Gid:         cfg.Gid,
			NoSetGroups: true,
		}
	}
	logx.Infof("sandbox isolate=clone netOff=%v dropPriv=%v dir=%s", cfg.DisableNetwork, attr.Credential != nil, workAbs)
	return &IsolatePlan{Argv: argv, Attr: attr, Dir: workAbs, Mode: "clone"}, nil
}

func attachCgroup(attr *syscall.SysProcAttr, cgroupFD int) {
	if cgroupFD > 0 {
		attr.UseCgroupFD = true
		attr.CgroupFD = cgroupFD
	}
}

// rewriteArgvPaths 将工作区内的绝对路径改写到 /box 下，系统路径保持不变。
func rewriteArgvPaths(argv []string, rootAbs, box string) []string {
	out := make([]string, len(argv))
	rootPrefix := rootAbs + string(os.PathSeparator)
	for i, part := range argv {
		if part == rootAbs {
			out[i] = box
			continue
		}
		if strings.HasPrefix(part, rootPrefix) {
			rel := strings.TrimPrefix(part, rootPrefix)
			out[i] = filepath.Join(box, rel)
			continue
		}
		out[i] = part
	}
	return out
}
