package utils

import (
	"net"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"testing"
	"time"

	"service-judge/internal/config"
)

// 仅测 bwrap --unshare-net 断网，与工作区文件边界用例分离。
func TestSandboxNetBoundaryBwrap(t *testing.T) {
	requireBwrapPython(t)

	root := t.TempDir()
	runs := filepath.Join(root, "runs")
	source := filepath.Join(root, "source")
	mustMkdir(t, runs)
	mustMkdir(t, source)

	// 本机受控监听（合规：不访问任意外网作为成功路径；断网下连本机也应失败）
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatalf("listen: %v", err)
	}
	defer ln.Close()
	localAddr := ln.Addr().String()
	go acceptAndDiscard(ln)

	targets := []struct {
		name string
		host string
		port string
	}{
		{name: "loopback_controlled", host: "127.0.0.1", port: portOf(localAddr)},
		{name: "loopback_http", host: "127.0.0.1", port: "80"},
		{name: "loopback_dns", host: "127.0.0.1", port: "53"},
		{name: "ipv4_public_dns", host: "1.1.1.1", port: "53"},
		{name: "ipv4_public_dns_alt", host: "8.8.8.8", port: "53"},
		{name: "link_local", host: "169.254.169.254", port: "80"},
	}

	for _, tc := range targets {
		t.Run(tc.name, func(t *testing.T) {
			out, runErr := runNetProbeBwrap(t, root, runs, source, tc.host, tc.port)
			t.Logf("target=%s:%s stdout=%q err=%v", tc.host, tc.port, out, runErr)
			if runErr != nil {
				t.Fatalf("probe failed: %v", runErr)
			}
			if strings.Contains(out, "NET:OK") {
				t.Fatalf("network should be blocked, got %q", out)
			}
			if !strings.Contains(out, "NET_ERR:") {
				t.Fatalf("want NET_ERR, got %q", out)
			}
		})
	}
}

// CLONE_NEWNET 路径：多目标断网，与 bwrap 用例独立。
func TestSandboxNetBoundaryClone(t *testing.T) {
	if _, err := exec.LookPath("python3"); err != nil {
		t.Skip("python3 not installed")
	}

	targets := []struct {
		name string
		host string
		port int
	}{
		{name: "loopback_53", host: "127.0.0.1", port: 53},
		{name: "loopback_80", host: "127.0.0.1", port: 80},
		{name: "public_dns", host: "1.1.1.1", port: 53},
		{name: "public_dns_alt", host: "8.8.8.8", port: 53},
	}

	for _, tc := range targets {
		t.Run(tc.name, func(t *testing.T) {
			dir := t.TempDir()
			cfg := config.SandboxConfig{
				DisableNetwork: true,
				DropPrivileges: false,
				UseBwrap:       "off",
			}
			script := `
import socket, sys
host, port = sys.argv[1], int(sys.argv[2])
try:
    s = socket.create_connection((host, port), timeout=1)
    s.close()
    print("NET:OK")
except Exception as e:
    print("NET_ERR:" + type(e).__name__)
`
			plan, err := BuildIsolatePlan(
				[]string{"python3", "-c", script, tc.host, strconv.Itoa(tc.port)},
				dir, dir, cfg, 0,
			)
			if err != nil {
				t.Fatal(err)
			}
			out, runErr := runIsolatePlan(t, plan)
			t.Logf("stdout=%q err=%v", out, runErr)
			if runErr != nil && strings.Contains(runErr.Error(), "operation not permitted") {
				t.Skip("CLONE_NEWNET not permitted for current user")
			}
			if strings.Contains(out, "NET:OK") {
				t.Fatalf("network should be blocked under CLONE_NEWNET")
			}
			if !strings.Contains(out, "NET_ERR:") {
				t.Fatalf("want NET_ERR, got %q", out)
			}
		})
	}
}

// runNetProbeBwrap 在 bwrap + DisableNetwork 下对单一 host:port 发起连接。
func runNetProbeBwrap(t *testing.T, root, runs, source, host, port string) (string, error) {
	t.Helper()
	probe := filepath.Join(source, "net_probe.py")
	script := `
import socket, sys
host, port = sys.argv[1], int(sys.argv[2])
try:
    s = socket.create_connection((host, port), timeout=1)
    s.close()
    print("NET:OK")
except Exception as e:
    print("NET_ERR:" + type(e).__name__)
`
	mustWrite(t, probe, script)

	cfg := config.SandboxConfig{
		DisableNetwork: true,
		DropPrivileges: false,
		UseBwrap:       "on",
	}
	plan, err := BuildIsolatePlan([]string{"python3", probe, host, port}, root, runs, cfg, 0)
	if err != nil {
		t.Fatalf("BuildIsolatePlan: %v", err)
	}
	if plan.Mode != "bwrap" {
		t.Fatalf("want bwrap, got %s", plan.Mode)
	}
	return runIsolatePlan(t, plan)
}

func acceptAndDiscard(ln net.Listener) {
	_ = ln.(*net.TCPListener).SetDeadline(time.Now().Add(30 * time.Second))
	for {
		c, err := ln.Accept()
		if err != nil {
			return
		}
		_ = c.Close()
	}
}

func portOf(addr string) string {
	_, port, err := net.SplitHostPort(addr)
	if err != nil {
		return "0"
	}
	return port
}
