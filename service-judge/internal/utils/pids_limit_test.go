package utils

import (
	"bytes"
	"os/exec"
	"strings"
	"syscall"
	"testing"
	"time"
)

// 受控验证 pids.max：上限很小的 cgroup 内 fork 应失败（不跑进程耗尽炸弹）。
func TestPidsMaxLimitsFork(t *testing.T) {
	if err := InitSandboxMode("cgroup"); err != nil {
		t.Skipf("cgroup 不可用: %v", err)
	}
	group, err := CreateCgroupNoMemory(2)
	if err != nil {
		t.Fatalf("CreateCgroupNoMemory: %v", err)
	}
	defer group.Close()

	// 在 cgroup 内尝试多次 fork；第 2 个额外子进程应被 pids.max=2 拒绝
	script := `
import os, sys, time
ok = 0
err = None
for i in range(8):
    try:
        pid = os.fork()
    except OSError as e:
        err = e
        break
    if pid == 0:
        time.sleep(1.5)
        os._exit(0)
    ok += 1
print("FORK_OK:"+str(ok))
if err is not None:
    print("FORK_ERR:"+str(err.errno))
else:
    print("FORK_ERR:none")
sys.stdout.flush()
time.sleep(0.2)
`
	cmd := exec.Command("python3", "-c", script)
	attr := &syscall.SysProcAttr{Setpgid: true}
	if group.FD() > 0 {
		attr.UseCgroupFD = true
		attr.CgroupFD = group.FD()
	}
	cmd.SysProcAttr = attr
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Start(); err != nil {
		t.Fatal(err)
	}
	if group.FD() == 0 {
		if err := syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP); err != nil {
			t.Fatal(err)
		}
		if err := group.AddProc(cmd.Process.Pid); err != nil {
			_ = syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
			_ = cmd.Wait()
			t.Fatal(err)
		}
		if err := syscall.Kill(cmd.Process.Pid, syscall.SIGCONT); err != nil {
			t.Fatal(err)
		}
	}

	done := make(chan error, 1)
	go func() { done <- cmd.Wait() }()
	select {
	case <-time.After(5 * time.Second):
		_ = syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
		t.Fatal("pids probe timed out")
	case waitErr := <-done:
		out := stdout.String()
		t.Logf("stdout=%q stderr=%q wait=%v", out, stderr.String(), waitErr)
		if !strings.Contains(out, "FORK_ERR:") {
			t.Fatalf("missing FORK_ERR marker: %q", out)
		}
		if strings.Contains(out, "FORK_ERR:none") {
			t.Fatalf("pids.max=2 should block further forks, got: %q", out)
		}
		// EAGAIN == 11 on Linux
		if !strings.Contains(out, "FORK_ERR:11") && !strings.Contains(out, "FORK_OK:0") && !strings.Contains(out, "FORK_OK:1") {
			// 至少应有错误码或极少成功次数
			t.Fatalf("unexpected fork outcome: %q", out)
		}
	}
}
