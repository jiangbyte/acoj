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

// 仅测工作区文件系统边界（bwrap 目录 jail），与断网用例分离。
// 断言风格：区内 → OK:内容；区外 → ERR:异常类型名，且不得泄露 canary。
func TestSandboxFsBoundaryBwrap(t *testing.T) {
	requireBwrapPython(t)

	root := t.TempDir()
	runs := filepath.Join(root, "runs")
	source := filepath.Join(root, "source")
	build := filepath.Join(root, "build")
	mustMkdir(t, runs)
	mustMkdir(t, source)
	mustMkdir(t, build)

	// 工作区内多路径可读标记（runs / source / build / 嵌套）
	insideFiles := map[string]string{
		filepath.Join(runs, "inside_a.txt"):       "INSIDE_A",
		filepath.Join(runs, "inside_b.txt"):       "INSIDE_B",
		filepath.Join(runs, "nested", "c.txt"):    "INSIDE_C",
		filepath.Join(runs, "nested", "d.dat"):    "INSIDE_D",
		filepath.Join(source, "main.py"):          "INSIDE_SRC",
		filepath.Join(build, "out.bin"):           "INSIDE_BUILD",
	}
	for path, content := range insideFiles {
		mustWrite(t, path, content)
	}

	// 工作区外 canary（无敏感信息）
	outsideRoot := t.TempDir()
	sibling := filepath.Join(filepath.Dir(root), "sibling_canary.txt")
	parentLeak := filepath.Join(filepath.Dir(root), "parent_leak.txt")
	deepOutside := filepath.Join(outsideRoot, "sub", "deep", "canary_z.txt")
	canaries := []struct {
		path string
		body string
	}{
		{filepath.Join(outsideRoot, "canary_a.txt"), "CANARY_A"},
		{filepath.Join(outsideRoot, "canary_b.txt"), "CANARY_B"},
		{filepath.Join(outsideRoot, "canary_c.txt"), "CANARY_C"},
		{deepOutside, "CANARY_DEEP"},
		{sibling, "CANARY_SIBLING"},
		{parentLeak, "CANARY_PARENT"},
	}
	for _, c := range canaries {
		mustWrite(t, c.path, c.body)
	}
	t.Cleanup(func() {
		_ = os.Remove(sibling)
		_ = os.Remove(parentLeak)
	})

	// wantErr：期望的 Python 异常类型；空表示只要 ERR: 即可
	cases := []struct {
		name     string
		action   string
		target   string
		wantOK   bool
		okToken  string
		wantErr  string // 区外异常类型，如 FileNotFoundError
		denySub  string
	}{
		// —— 区内成功：读 ——
		{name: "inside_read_a", action: "read_box", target: "/box/runs/inside_a.txt", wantOK: true, okToken: "INSIDE_A"},
		{name: "inside_read_b", action: "read_box", target: "/box/runs/inside_b.txt", wantOK: true, okToken: "INSIDE_B"},
		{name: "inside_read_nested_c", action: "read_box", target: "/box/runs/nested/c.txt", wantOK: true, okToken: "INSIDE_C"},
		{name: "inside_read_nested_d", action: "read_box", target: "/box/runs/nested/d.dat", wantOK: true, okToken: "INSIDE_D"},
		{name: "inside_read_source", action: "read_box", target: "/box/source/main.py", wantOK: true, okToken: "INSIDE_SRC"},
		{name: "inside_read_build", action: "read_box", target: "/box/build/out.bin", wantOK: true, okToken: "INSIDE_BUILD"},
		{name: "inside_read_rel_cwd", action: "read_rel", target: "inside_a.txt", wantOK: true, okToken: "INSIDE_A"},
		{name: "inside_read_rel_nested", action: "read_rel", target: "nested/c.txt", wantOK: true, okToken: "INSIDE_C"},

		// —— 区内成功：写 / 列目录 ——
		{name: "inside_write_runs", action: "write_box", target: "/box/runs/write_ok.txt", wantOK: true, okToken: "WROTE"},
		{name: "inside_write_rel", action: "write_rel", target: "write_rel_ok.txt", wantOK: true, okToken: "WROTE"},
		{name: "inside_listdir_runs", action: "listdir_box", target: "/box/runs", wantOK: true, okToken: "inside_a.txt"},
		{name: "inside_listdir_nested", action: "listdir_box", target: "/box/runs/nested", wantOK: true, okToken: "c.txt"},

		// —— 区外异常：绝对 canary / 兄弟 / 系统路径 ——
		{name: "outside_abs_a", action: "read_abs", target: canaries[0].path, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_A"},
		{name: "outside_abs_b", action: "read_abs", target: canaries[1].path, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_B"},
		{name: "outside_abs_c", action: "read_abs", target: canaries[2].path, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_C"},
		{name: "outside_deep", action: "read_abs", target: deepOutside, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_DEEP"},
		{name: "outside_sibling", action: "read_abs", target: sibling, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_SIBLING"},
		{name: "outside_parent", action: "read_abs", target: parentLeak, wantOK: false, wantErr: "FileNotFoundError", denySub: "CANARY_PARENT"},
		{name: "outside_etc_passwd", action: "read_abs", target: "/etc/passwd", wantOK: false, wantErr: "FileNotFoundError", denySub: "root:"},
		{name: "outside_etc_hosts", action: "read_abs", target: "/etc/hosts", wantOK: false, wantErr: "FileNotFoundError", denySub: "localhost"},
		{name: "outside_etc_shadow", action: "read_abs", target: "/etc/shadow", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_var_log", action: "read_abs", target: "/var/log/syslog", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_var_tmp", action: "read_abs", target: "/var/tmp", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_root_bashrc", action: "read_abs", target: "/root/.bashrc", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_home", action: "read_abs", target: "/home", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_opt", action: "read_abs", target: "/opt", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_mnt", action: "read_abs", target: "/mnt", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},

		// —— 区外写拒绝（父路径不在挂载树内 → FileNotFoundError）——
		{name: "outside_write_abs", action: "write_abs", target: filepath.Join(outsideRoot, "should_not_create.txt"), wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_write_nosuch", action: "write_abs", target: "/no_such_mount_acoj/should_not", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_write_var", action: "write_abs", target: "/var/tmp/acoj_should_not", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_write_sibling", action: "write_abs", target: filepath.Join(filepath.Dir(root), "sibling_write.txt"), wantOK: false, wantErr: "FileNotFoundError", denySub: ""},
		{name: "outside_write_home", action: "write_abs", target: "/home/acoj_should_not", wantOK: false, wantErr: "FileNotFoundError", denySub: ""},

		// —— 路径穿越：相对 .. 跳出 /box ——
		{name: "outside_traverse_dotdot", action: "read_rel", target: "../../etc/passwd", wantOK: false, wantErr: "FileNotFoundError", denySub: "root:"},
		{name: "outside_traverse_abs_via_box", action: "read_box", target: "/box/../etc/passwd", wantOK: false, wantErr: "FileNotFoundError", denySub: "root:"},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			out, runErr := runFsProbe(t, root, runs, source, tc.action, tc.target)
			t.Logf("action=%s target=%s stdout=%q err=%v", tc.action, tc.target, out, runErr)
			if runErr != nil {
				t.Fatalf("probe failed: %v", runErr)
			}

			if tc.wantOK {
				// 区内成功：必须有 OK:，不得出现 ERR:
				if !strings.Contains(out, "OK:"+tc.okToken) {
					t.Fatalf("区内应成功，want OK:%s，got %q", tc.okToken, out)
				}
				if strings.Contains(out, "ERR:") {
					t.Fatalf("区内不应有异常类型，got %q", out)
				}
				return
			}

			// 区外异常：必须 ERR:类型，不得 OK:，不得泄露 canary
			if !strings.Contains(out, "ERR:") {
				t.Fatalf("区外应异常，want ERR:类型，got %q", out)
			}
			if strings.Contains(out, "OK:") {
				t.Fatalf("区外不得成功，got %q", out)
			}
			if tc.wantErr != "" {
				want := "ERR:" + tc.wantErr
				if !strings.Contains(out, want) {
					t.Fatalf("区外异常类型不符，want %s，got %q", want, out)
				}
			}
			if tc.denySub != "" && strings.Contains(out, tc.denySub) {
				t.Fatalf("泄露 %q: %q", tc.denySub, out)
			}
			if strings.HasPrefix(tc.action, "write_") {
				if _, err := os.Stat(tc.target); err == nil {
					t.Fatalf("区外文件被创建: %s", tc.target)
				}
			}
		})
	}

	// 区内写入应真实落盘（宿主侧可复核）
	t.Run("inside_write_persisted", func(t *testing.T) {
		data, err := os.ReadFile(filepath.Join(runs, "write_ok.txt"))
		if err != nil {
			t.Fatalf("区内写入未落盘: %v", err)
		}
		if strings.TrimSpace(string(data)) != "WROTE" {
			t.Fatalf("内容不符: %q", data)
		}
	})

	// 区外 canary 内容未被读出/改写（宿主 SHA 前后一致）
	t.Run("outside_canary_unchanged", func(t *testing.T) {
		for _, c := range canaries {
			data, err := os.ReadFile(c.path)
			if err != nil {
				t.Fatalf("canary 丢失 %s: %v", c.path, err)
			}
			if string(data) != c.body {
				t.Fatalf("canary 被改写 %s: got %q want %q", c.path, data, c.body)
			}
		}
	})

	// /etc 在空 mount ns 里可能被进程「看似可写」，但不得落到宿主机
	t.Run("outside_write_etc_host_untouched", func(t *testing.T) {
		target := "/etc/acoj_fs_probe_should_not"
		if _, err := os.Stat(target); err == nil {
			t.Skipf("宿主已存在 %s，跳过以免误伤", target)
		}
		out, runErr := runFsProbe(t, root, runs, source, "write_abs", target)
		t.Logf("stdout=%q err=%v（ns 内可能 OK 或 ERR，关键是宿主无文件）", out, runErr)
		if runErr != nil {
			t.Fatalf("probe failed: %v", runErr)
		}
		if _, err := os.Stat(target); err == nil {
			t.Fatalf("宿主机出现越权文件: %s", target)
		}
	})
}

// runFsProbe 在 bwrap 工作区内执行单次读/写/列目录探测（不断网）。
func runFsProbe(t *testing.T, root, runs, source, action, target string) (string, error) {
	t.Helper()
	probe := filepath.Join(source, "fs_probe.py")
	script := `
import os, sys
action, target = sys.argv[1], sys.argv[2]
try:
    if action == "read_box":
        with open(target) as f:
            print("OK:" + f.read().strip())
    elif action == "read_rel":
        with open(target) as f:
            print("OK:" + f.read().strip())
    elif action == "read_abs":
        with open(target) as f:
            print("OK:" + f.read()[:64])
    elif action == "write_box" or action == "write_rel":
        with open(target, "w") as f:
            f.write("WROTE")
        print("OK:WROTE")
    elif action == "write_abs":
        with open(target, "w") as f:
            f.write("SHOULD_NOT")
        print("OK:wrote")
    elif action == "listdir_box":
        names = sorted(os.listdir(target))
        print("OK:" + ",".join(names))
    else:
        print("ERR:bad_action")
except Exception as e:
    print("ERR:" + type(e).__name__)
`
	mustWrite(t, probe, script)

	cfg := config.SandboxConfig{
		DisableNetwork: false,
		DropPrivileges: false,
		UseBwrap:       "on",
	}
	plan, err := BuildIsolatePlan([]string{"python3", probe, action, target}, root, runs, cfg, 0)
	if err != nil {
		t.Fatalf("BuildIsolatePlan: %v", err)
	}
	if plan.Mode != "bwrap" {
		t.Fatalf("want bwrap, got %s", plan.Mode)
	}
	return runIsolatePlan(t, plan)
}

func requireBwrapPython(t *testing.T) {
	t.Helper()
	if _, err := exec.LookPath("bwrap"); err != nil {
		t.Skip("bwrap not installed")
	}
	if _, err := exec.LookPath("python3"); err != nil {
		t.Skip("python3 not installed")
	}
}

func mustMkdir(t *testing.T, dir string) {
	t.Helper()
	if err := os.MkdirAll(dir, 0755); err != nil {
		t.Fatal(err)
	}
}

func mustWrite(t *testing.T, path, body string) {
	t.Helper()
	if err := os.MkdirAll(filepath.Dir(path), 0755); err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(path, []byte(body), 0644); err != nil {
		t.Fatal(err)
	}
}

func runIsolatePlan(t *testing.T, plan *IsolatePlan) (string, error) {
	t.Helper()
	cmd := exec.Command(plan.Argv[0], plan.Argv[1:]...)
	cmd.SysProcAttr = plan.Attr
	if plan.Dir != "" {
		cmd.Dir = plan.Dir
	}
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	err := cmd.Run()
	if stderr.Len() > 0 {
		t.Logf("stderr=%q", stderr.String())
	}
	return stdout.String(), err
}
