package utils

import (
	"path/filepath"
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
