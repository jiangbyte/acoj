package utils

import (
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"syscall"
	"testing"
	"time"
)

func TestCgroupChild(t *testing.T) {
	switch os.Getenv("ACOJ_CHILD") {
	case "empty":
	case "alloc":
		touchPages(64 << 20)
	case "cpu":
		burnCPU(200 * time.Millisecond)
	case "oom":
		touchPages(128 << 20)
	default:
		t.Skip()
	}
}

func TestCgroupV2Accuracy(t *testing.T) {
	if os.Getenv("ACOJ_CG1_INNER") == "1" {
		t.Skip()
	}
	if err := InitSandboxMode("cgroup"); err != nil {
		t.Fatal(err)
	}
	if CgroupVersion() != CgroupV2 {
		t.Fatalf("当前沙箱不是 cgroup v2，而是 v%d", CgroupVersion())
	}
	assertResourceAccuracy(t)
}

func TestCgroupV1Accuracy(t *testing.T) {
	if os.Getenv("ACOJ_CG1_INNER") == "1" {
		// 1. 在特权容器里挂上 v1 的 memory 和 cpuacct
		// 2. 用这套挂载跑和 v2 相同的精度断言
		if err := mountCgroupV1("/cg/mem", "/cg/cpu", "/cg/pids"); err != nil {
			t.Fatal(err)
		}
		OverrideSandbox(CgroupV1, "/cg/mem", "/cg/cpu", "/cg/pids")
		assertResourceAccuracy(t)
		return
	}
	if mem, cpu, pids, ok := discoverV1Mounts(); ok {
		if pids == "" {
			t.Skip("本机 cgroup v1 无可用 pids 控制器")
		}
		OverrideSandbox(CgroupV1, mem, cpu, pids)
		assertResourceAccuracy(t)
		return
	}
	// 本机内核若没编 CONFIG_MEMCG_V1，memory 控制器只存在于 v2，v1 挂载会被内核拒绝
	if !kernelHasMemcgV1() {
		t.Skip("内核未编译 CONFIG_MEMCG_V1，cgroup v1 的 memory 控制器不存在，无法在本机实测 v1")
	}
	runV1InDocker(t)
}

func kernelHasMemcgV1() bool {
	release, err := os.ReadFile("/proc/sys/kernel/osrelease")
	if err != nil {
		return true
	}
	content, err := os.ReadFile("/boot/config-" + strings.TrimSpace(string(release)))
	if err != nil {
		return true
	}
	text := string(content)
	if strings.Contains(text, "CONFIG_MEMCG_V1=y") {
		return true
	}
	if strings.Contains(text, "# CONFIG_MEMCG_V1 is not set") {
		return false
	}
	return true
}

type sample struct {
	mem uint64
	cpu time.Duration
	oom bool
	err error
}

func assertResourceAccuracy(t *testing.T) {
	t.Helper()
	empty := runSample(t, 0, "empty")
	alloc := runSample(t, 0, "alloc")
	delta := int64(alloc.mem) - int64(empty.mem)
	t.Logf("empty=%dB alloc=%dB delta=%dB", empty.mem, alloc.mem, delta)
	const want = int64(64 << 20)
	if delta < want-8<<20 || delta > want+24<<20 {
		t.Fatalf("64MB 分配后 cgroup 增量=%dB，偏离真实分配", delta)
	}

	burned := runSample(t, 0, "cpu")
	t.Logf("cpu burn=%s", burned.cpu)
	if burned.cpu < 160*time.Millisecond || burned.cpu > 320*time.Millisecond {
		t.Fatalf("烧核 200ms，cgroup CPU=%s", burned.cpu)
	}

	limitBytes := empty.mem + 8<<20
	if limitBytes < 16<<20 {
		limitBytes = 16 << 20
	}
	oomed := runSample(t, float64(limitBytes)/1024, "oom")
	t.Logf("oom peak=%dB limit=%dB event=%v err=%v", oomed.mem, limitBytes, oomed.oom, oomed.err)
	if oomed.err == nil {
		t.Fatal("超过内存上限的进程没有被杀掉")
	}
	if oomed.mem > limitBytes+4<<20 {
		t.Fatalf("OOM 后峰值 %dB 高出上限 %dB 太多", oomed.mem, limitBytes)
	}
	if !oomed.oom {
		t.Fatalf("进程被杀，但 cgroup 没有 OOM 记录，峰值=%dB", oomed.mem)
	}
}

func runSample(t *testing.T, limitKB float64, mode string) sample {
	t.Helper()
	var (
		group *Group
		err   error
	)
	if limitKB > 0 {
		group, err = CreateCgroup(limitKB, defaultMaxPids)
	} else {
		group, err = CreateCgroupNoMemory(defaultMaxPids)
	}
	if err != nil {
		t.Fatal(err)
	}
	defer group.Close()

	cmd := exec.Command(os.Args[0], "-test.run=^TestCgroupChild$", "-test.count=1")
	cmd.Env = append(os.Environ(), "ACOJ_CHILD="+mode)
	attr := &syscall.SysProcAttr{Setpgid: true}
	if group.FD() > 0 {
		attr.UseCgroupFD = true
		attr.CgroupFD = group.FD()
	}
	cmd.SysProcAttr = attr
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
	runErr := cmd.Wait()
	mem, memErr := group.MemoryPeak()
	if memErr != nil {
		t.Fatal(memErr)
	}
	cpu, cpuErr := group.CPUTime()
	if cpuErr != nil {
		t.Fatal(cpuErr)
	}
	return sample{mem: mem, cpu: cpu, oom: group.OOMKilled(), err: runErr}
}

func touchPages(n int) {
	buf := make([]byte, n)
	for i := 0; i < n; i += 4096 {
		buf[i] = 1
	}
	runtime.KeepAlive(buf)
}

func burnCPU(d time.Duration) {
	deadline := time.Now().Add(d)
	for time.Now().Before(deadline) {
	}
}

func mountCgroupV1(memoryDir, cpuDir, pidsDir string) error {
	if err := os.MkdirAll(memoryDir, 0755); err != nil {
		return err
	}
	if err := os.MkdirAll(cpuDir, 0755); err != nil {
		return err
	}
	if err := os.MkdirAll(pidsDir, 0755); err != nil {
		return err
	}
	if err := syscall.Mount("none", memoryDir, "cgroup", 0, "memory"); err != nil {
		return err
	}
	if err := syscall.Mount("none", cpuDir, "cgroup", 0, "cpuacct"); err != nil {
		return err
	}
	return syscall.Mount("none", pidsDir, "cgroup", 0, "pids")
}

func runV1InDocker(t *testing.T) {
	t.Helper()
	if _, err := exec.LookPath("docker"); err != nil {
		t.Fatal("本机没有 cgroup v1，也没有 docker，无法验证 v1")
	}
	_, file, _, ok := runtime.Caller(0)
	if !ok {
		t.Fatal("找不到测试源码目录")
	}
	bin := filepath.Join(t.TempDir(), "acc.test")
	build := exec.Command("go", "test", "-c", "-o", bin, ".")
	build.Dir = filepath.Dir(file)
	build.Env = append(os.Environ(), "CGO_ENABLED=0")
	if out, err := build.CombinedOutput(); err != nil {
		t.Fatalf("编译静态测试失败: %v\n%s", err, out)
	}
	cmd := exec.Command("docker", "run", "--rm", "--privileged",
		"-v", bin+":/acc.test:ro",
		"-e", "ACOJ_CG1_INNER=1",
		"--entrypoint", "/acc.test",
		"rabbitmq:4.3.6-management",
		"-test.run", "^(TestCgroupV1Accuracy|TestCgroupChild)$",
		"-test.v", "-test.count=1")
	out, err := cmd.CombinedOutput()
	t.Log(string(out))
	if err != nil {
		t.Fatalf("cgroup v1 容器测试失败: %v", err)
	}
}
