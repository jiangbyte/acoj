package utils

import (
	"bytes"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"

	"service-judge/internal/config"
)

// 实测沙箱边界：断网 + 工作区外文件不可读 + 工作区内可读。
func TestSandboxBoundaryBwrap(t *testing.T) {
	if _, err := exec.LookPath("bwrap"); err != nil {
		t.Skip("bwrap not installed")
	}
	if _, err := exec.LookPath("python3"); err != nil {
		t.Skip("python3 not installed")
	}

	root := t.TempDir()
	runs := filepath.Join(root, "runs")
	source := filepath.Join(root, "source")
	if err := os.MkdirAll(runs, 0755); err != nil {
		t.Fatal(err)
	}
	if err := os.MkdirAll(source, 0755); err != nil {
		t.Fatal(err)
	}

	// 工作区外机密文件（沙箱内应读不到）
	secretPath := filepath.Join(t.TempDir(), "secret.txt")
	if err := os.WriteFile(secretPath, []byte("TOP_SECRET_OUTSIDE"), 0644); err != nil {
		t.Fatal(err)
	}
	// 工作区内可读标记
	insidePath := filepath.Join(runs, "inside.txt")
	if err := os.WriteFile(insidePath, []byte("INSIDE_OK"), 0644); err != nil {
		t.Fatal(err)
	}

	probe := filepath.Join(source, "probe.py")
	script := `
import socket, sys
print("START")

# 1) 工作区内文件
try:
    with open("/box/runs/inside.txt") as f:
        print("INSIDE:" + f.read().strip())
except Exception as e:
    print("INSIDE_ERR:" + type(e).__name__)

# 2) 工作区外绝对路径
try:
    with open(sys.argv[1]) as f:
        print("OUTSIDE:" + f.read().strip())
except Exception as e:
    print("OUTSIDE_ERR:" + type(e).__name__)

# 3) 常见系统文件（bwrap 默认不挂载 /etc/passwd 时也应失败）
try:
    with open("/etc/passwd") as f:
        print("PASSWD:" + f.readline().strip()[:20])
except Exception as e:
    print("PASSWD_ERR:" + type(e).__name__)

# 4) 外网/内网 TCP
try:
    s = socket.create_connection(("1.1.1.1", 53), timeout=1)
    s.close()
    print("NET:OK")
except Exception as e:
    print("NET_ERR:" + type(e).__name__)

# 5) 解析并连接远程 MQ（dev 环境可达主机）
try:
    s = socket.create_connection(("47.110.3.35", 5672), timeout=1)
    s.close()
    print("MQ:OK")
except Exception as e:
    print("MQ_ERR:" + type(e).__name__)
`
	if err := os.WriteFile(probe, []byte(script), 0644); err != nil {
		t.Fatal(err)
	}

	cfg := config.SandboxConfig{
		DisableNetwork: true,
		DropPrivileges: false, // 非 root 环境不测降权
		UseBwrap:       "on",
	}
	plan, err := BuildIsolatePlan([]string{"python3", probe, secretPath}, root, runs, cfg, 0)
	if err != nil {
		t.Fatalf("BuildIsolatePlan: %v", err)
	}
	if plan.Mode != "bwrap" {
		t.Fatalf("want bwrap mode, got %s", plan.Mode)
	}

	cmd := exec.Command(plan.Argv[0], plan.Argv[1:]...)
	cmd.SysProcAttr = plan.Attr
	if plan.Dir != "" {
		cmd.Dir = plan.Dir
	}
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	runErr := cmd.Run()
	out := stdout.String()
	t.Logf("stdout:\n%s", out)
	if stderr.Len() > 0 {
		t.Logf("stderr:\n%s", stderr.String())
	}
	if runErr != nil {
		t.Fatalf("probe run failed: %v", runErr)
	}

	assertContains := func(sub string) {
		t.Helper()
		if !strings.Contains(out, sub) {
			t.Fatalf("missing %q in output:\n%s", sub, out)
		}
	}
	assertNotContains := func(sub string) {
		t.Helper()
		if strings.Contains(out, sub) {
			t.Fatalf("unexpected %q in output:\n%s", sub, out)
		}
	}

	assertContains("INSIDE:INSIDE_OK")
	assertContains("OUTSIDE_ERR:")
	assertNotContains("OUTSIDE:TOP_SECRET")
	assertContains("PASSWD_ERR:")
	assertNotContains("PASSWD:")
	assertContains("NET_ERR:")
	assertNotContains("NET:OK")
	assertContains("MQ_ERR:")
	assertNotContains("MQ:OK")
}

func TestSandboxBoundaryCloneNetOnly(t *testing.T) {
	if _, err := exec.LookPath("python3"); err != nil {
		t.Skip("python3 not installed")
	}
	cfg := config.SandboxConfig{DisableNetwork: true, DropPrivileges: false, UseBwrap: "off"}
	dir := t.TempDir()
	plan, err := BuildIsolatePlan([]string{"python3", "-c",
		"import socket\n" +
			"try:\n" +
			" s=socket.create_connection(('1.1.1.1',53),1); s.close(); print('NET:OK')\n" +
			"except Exception as e:\n" +
			" print('NET_ERR:'+type(e).__name__)\n",
	}, dir, dir, cfg, 0)
	if err != nil {
		t.Fatal(err)
	}
	cmd := exec.Command(plan.Argv[0], plan.Argv[1:]...)
	cmd.SysProcAttr = plan.Attr
	if plan.Dir != "" {
		cmd.Dir = plan.Dir
	}
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	runErr := cmd.Run()
	out := stdout.String()
	t.Logf("stdout=%q stderr=%q err=%v", out, stderr.String(), runErr)
	if runErr != nil && strings.Contains(runErr.Error(), "operation not permitted") {
		t.Skip("CLONE_NEWNET not permitted for current user; prefer bwrap path in production/root")
	}
	if strings.Contains(out, "NET:OK") {
		t.Fatalf("network should be blocked under CLONE_NEWNET")
	}
	if !strings.Contains(out, "NET_ERR:") {
		t.Fatalf("expected NET_ERR, got %q", out)
	}
}
