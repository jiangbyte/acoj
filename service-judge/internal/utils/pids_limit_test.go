package utils

import (
	"bytes"
	"fmt"
	"os/exec"
	"strconv"
	"strings"
	"syscall"
	"testing"
	"time"
)

// 受控验证 pids.max：多档上限下 fork 应被拒绝（不跑进程耗尽炸弹）。
func TestPidsMaxLimitsFork(t *testing.T) {
	if err := InitSandboxMode("cgroup"); err != nil {
		t.Skipf("cgroup 不可用: %v", err)
	}

	// 多档上限，避免只验证 pids.max=2 的特例
	cases := []struct {
		name    string
		maxPids int
	}{
		{name: "max1", maxPids: 1},
		{name: "max2", maxPids: 2},
		{name: "max3", maxPids: 3},
		{name: "max4", maxPids: 4},
		{name: "max8", maxPids: 8},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			runPidsMaxForkProbe(t, tc.maxPids)
		})
	}
}

// runPidsMaxForkProbe 在 pids.max=maxPids 的 cgroup 内受控多次 fork，断言超限被拒。
func runPidsMaxForkProbe(t *testing.T, maxPids int) {
	t.Helper()

	// 1. 建仅含 PID 上限的 cgroup；失败则整组用例不可信
	group, err := CreateCgroupNoMemory(maxPids)
	if err != nil {
		t.Fatalf("CreateCgroupNoMemory(%d): %v", maxPids, err)
	}
	defer group.Close()

	// 2. 尝试次数明显超过上限，子进程短睡后退出，避免真实耗尽
	attempts := maxPids + 4
	sleepSec := 1.5
	script := fmt.Sprintf(`
import os, sys, time
ok = 0
err = None
for i in range(%d):
    try:
        pid = os.fork()
    except OSError as e:
        err = e
        break
    if pid == 0:
        time.sleep(%.1f)
        os._exit(0)
    ok += 1
print("FORK_OK:"+str(ok))
if err is not None:
    print("FORK_ERR:"+str(err.errno))
else:
    print("FORK_ERR:none")
sys.stdout.flush()
time.sleep(0.2)
`, attempts, sleepSec)

	// 3. 启动探测进程并尽量在 clone 时即入组（v2 UseCgroupFD）
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

	// 4. v1 / 无 cgroup FD：停进程 → AddProc → 继续，保证 fork 发生在限内
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

	// 5. 短超时等待；超时杀进程组，防止挂死
	timeout := 5*time.Second + time.Duration(sleepSec*float64(time.Second))
	done := make(chan error, 1)
	go func() { done <- cmd.Wait() }()
	select {
	case <-time.After(timeout):
		_ = syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
		t.Fatalf("pids.max=%d probe timed out", maxPids)
	case waitErr := <-done:
		out := stdout.String()
		t.Logf("maxPids=%d stdout=%q stderr=%q wait=%v", maxPids, out, stderr.String(), waitErr)
		assertPidsForkBlocked(t, maxPids, out)
	}
}

// assertPidsForkBlocked 校验成功 fork 数不超过「上限-1」（父进程占 1 槽），且出现 EAGAIN。
func assertPidsForkBlocked(t *testing.T, maxPids int, out string) {
	t.Helper()

	if !strings.Contains(out, "FORK_ERR:") {
		t.Fatalf("pids.max=%d missing FORK_ERR marker: %q", maxPids, out)
	}
	if strings.Contains(out, "FORK_ERR:none") {
		t.Fatalf("pids.max=%d should block further forks, got: %q", maxPids, out)
	}

	okCount, ok := parseForkMarker(out, "FORK_OK:")
	if !ok {
		t.Fatalf("pids.max=%d cannot parse FORK_OK: %q", maxPids, out)
	}
	// 父进程占 1 槽，成功子进程数应严格小于 maxPids（理想约为 maxPids-1）
	if okCount >= maxPids {
		t.Fatalf("pids.max=%d FORK_OK=%d should be < %d, out=%q", maxPids, okCount, maxPids, out)
	}
	// maxPids>=2 时至少应成功过 1 次，说明入组生效且限制在「非零上限」处卡住
	if maxPids >= 2 && okCount < 1 {
		t.Fatalf("pids.max=%d FORK_OK=%d，疑似未正确入组，out=%q", maxPids, okCount, out)
	}

	errNo, hasErr := parseForkMarker(out, "FORK_ERR:")
	if !hasErr {
		t.Fatalf("pids.max=%d cannot parse FORK_ERR: %q", maxPids, out)
	}
	// Linux 上 pids 控制器拒绝 fork 时 errno 为 EAGAIN(11)
	if errNo != 11 {
		t.Fatalf("pids.max=%d expected EAGAIN=11, got errno=%d FORK_OK=%d out=%q", maxPids, errNo, okCount, out)
	}
}

// parseForkMarker 从脚本输出解析 FORK_OK: / FORK_ERR: 后的整数。
func parseForkMarker(out, prefix string) (int, bool) {
	idx := strings.Index(out, prefix)
	if idx < 0 {
		return 0, false
	}
	rest := out[idx+len(prefix):]
	end := 0
	for end < len(rest) && rest[end] >= '0' && rest[end] <= '9' {
		end++
	}
	if end == 0 {
		return 0, false
	}
	n, err := strconv.Atoi(rest[:end])
	if err != nil {
		return 0, false
	}
	return n, true
}
