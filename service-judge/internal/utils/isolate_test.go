package utils

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"syscall"
	"testing"

	"service-judge/internal/config"
)

func TestRewriteArgvPaths(t *testing.T) {
	root := "/tmp/judgespace/abc"
	argv := []string{
		"python3",
		filepath.Join(root, "source", "main.py"),
		filepath.Join(root, "runs", "main"),
		"/usr/bin/python3",
		root,
	}
	got := rewriteArgvPaths(argv, root, boxRoot)
	want := []string{
		"python3",
		"/box/source/main.py",
		"/box/runs/main",
		"/usr/bin/python3",
		"/box",
	}
	for i := range want {
		if got[i] != want[i] {
			t.Fatalf("argv[%d]=%q want %q", i, got[i], want[i])
		}
	}
}

func TestBuildClonePlanDisablesNetwork(t *testing.T) {
	plan, err := buildClonePlan([]string{"./main"}, "/tmp/work", config.SandboxConfig{
		DisableNetwork: true,
		DropPrivileges: false,
	}, 0)
	if err != nil {
		t.Fatal(err)
	}
	if plan.Mode != "clone" {
		t.Fatalf("mode=%s", plan.Mode)
	}
	if plan.Attr.Cloneflags&syscall.CLONE_NEWNET == 0 {
		t.Fatalf("expected CLONE_NEWNET in Cloneflags: %#x", plan.Attr.Cloneflags)
	}
}

// UseBwrap=on 且 bwrap 缺失时必须 fail-closed，禁止回退 clone。
func TestBuildIsolatePlanRequiresBwrapWhenOn(t *testing.T) {
	old := lookPath
	t.Cleanup(func() { lookPath = old })
	lookPath = func(file string) (string, error) {
		return "", fmt.Errorf("bwrap not found")
	}
	root := t.TempDir()
	_, err := BuildIsolatePlan([]string{"/bin/true"}, root, root, config.SandboxConfig{
		UseBwrap:       "on",
		DisableNetwork: true,
	}, 0)
	if err == nil {
		t.Fatal("expected error when bwrap missing and UseBwrap=on")
	}
	if !strings.Contains(err.Error(), "bwrap") {
		t.Fatalf("unexpected error: %v", err)
	}
}

// 显式 off 仍允许 clone 路径（单测/本地无 bwrap 场景）。
func TestBuildIsolatePlanAllowsCloneWhenOff(t *testing.T) {
	root := t.TempDir()
	plan, err := BuildIsolatePlan([]string{"./main"}, root, root, config.SandboxConfig{
		UseBwrap:       "off",
		DisableNetwork: true,
	}, 0)
	if err != nil {
		t.Fatal(err)
	}
	if plan.Mode != "clone" {
		t.Fatalf("mode=%s want clone", plan.Mode)
	}
}

// 降权必须走 Credential，禁止依赖 root 下 bwrap --uid 假映射。
func TestBuildBwrapPlanDropsViaCredential(t *testing.T) {
	old := lookPath
	t.Cleanup(func() { lookPath = old })
	lookPath = func(file string) (string, error) { return "/usr/bin/bwrap", nil }

	root := t.TempDir()
	plan, err := buildBwrapPlan([]string{"/bin/true"}, root, root, config.SandboxConfig{
		DisableNetwork: true,
		DropPrivileges: true,
		Uid:            65534,
		Gid:            65534,
	}, 0)
	if err != nil {
		t.Fatal(err)
	}
	joined := strings.Join(plan.Argv, " ")
	if strings.Contains(joined, "--uid") || strings.Contains(joined, "--gid ") || strings.Contains(joined, " --gid") {
		t.Fatalf("must not use bwrap --uid/--gid: %s", joined)
	}
	if !strings.Contains(joined, "--cap-drop") {
		t.Fatalf("expected --cap-drop ALL: %s", joined)
	}
	if os.Geteuid() != 0 {
		t.Skip("Credential 路径需 root（容器内评测进程）")
	}
	if plan.Attr == nil || plan.Attr.Credential == nil {
		t.Fatal("expected SysProcAttr.Credential for real host privilege drop")
	}
	if plan.Attr.Credential.Uid != 65534 || plan.Attr.Credential.Gid != 65534 {
		t.Fatalf("credential=%+v", plan.Attr.Credential)
	}
	if !strings.Contains(joined, "--unshare-user") {
		t.Fatalf("expected --unshare-user: %s", joined)
	}
}
