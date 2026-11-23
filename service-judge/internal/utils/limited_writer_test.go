package utils

import (
	"bytes"
	"os/exec"
	"sync/atomic"
	"syscall"
	"testing"
	"time"
)

// 与 core/executor.go 中 defaultOutputLimitBytes 保持一致（stdout/stderr 各上限）。
const judgeOutputLimitBytes = 2 << 20 // 2 MiB

// 多档字节上限：未超限不截断；恰到上限不截断；超过则截断且缓冲不超过 limit。
func TestLimitedWriterTruncateByLimit(t *testing.T) {
	cases := []struct {
		name      string
		limit     int
		chunk     int
		rounds    int
		wantTrunc bool
	}{
		{name: "under_64", limit: 64, chunk: 16, rounds: 3, wantTrunc: false},          // 48 < 64
		{name: "exact_64", limit: 64, chunk: 32, rounds: 2, wantTrunc: false},          // 64 == 64
		{name: "over_64_one_write", limit: 64, chunk: 65, rounds: 1, wantTrunc: true},  // 单次越界
		{name: "over_64_multi", limit: 64, chunk: 20, rounds: 4, wantTrunc: true},      // 80 > 64
		{name: "over_1KiB", limit: 1 << 10, chunk: 300, rounds: 4, wantTrunc: true},    // 1200 > 1024
		{name: "under_4KiB", limit: 4 << 10, chunk: 512, rounds: 7, wantTrunc: false},  // 3584 < 4096
		{name: "over_4KiB", limit: 4 << 10, chunk: 1024, rounds: 5, wantTrunc: true},   // 5120 > 4096
		{name: "under_2MiB", limit: judgeOutputLimitBytes, chunk: 1 << 20, rounds: 1, wantTrunc: false},
		{name: "exact_2MiB", limit: judgeOutputLimitBytes, chunk: 1 << 20, rounds: 2, wantTrunc: false},
		{name: "over_2MiB", limit: judgeOutputLimitBytes, chunk: 1 << 20, rounds: 3, wantTrunc: true}, // 3MiB
		{name: "over_2MiB_small_chunks", limit: judgeOutputLimitBytes, chunk: 64 << 10, rounds: 40, wantTrunc: true},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			var calls atomic.Int32
			w := NewLimitedWriter(tc.limit)
			w.SetOnTruncate(func() { calls.Add(1) })

			payload := bytes.Repeat([]byte("A"), tc.chunk)
			var total int64
			for i := 0; i < tc.rounds; i++ {
				n, err := w.Write(payload)
				if err != nil {
					t.Fatalf("write: %v", err)
				}
				if n != len(payload) {
					t.Fatalf("Write should accept full chunk, n=%d want %d", n, len(payload))
				}
				total += int64(n)
			}

			if w.Written() != total {
				t.Fatalf("Written=%d want %d", w.Written(), total)
			}
			if w.Len() > tc.limit {
				t.Fatalf("buffered Len=%d exceeds limit=%d", w.Len(), tc.limit)
			}
			if tc.wantTrunc {
				if !w.Truncated() {
					t.Fatalf("expected truncated at limit=%d written=%d", tc.limit, total)
				}
				if w.Len() != tc.limit {
					t.Fatalf("after truncate Len=%d want limit=%d", w.Len(), tc.limit)
				}
				if calls.Load() != 1 {
					t.Fatalf("onTruncate calls=%d want 1", calls.Load())
				}
			} else {
				if w.Truncated() {
					t.Fatalf("unexpected truncate: limit=%d written=%d", tc.limit, total)
				}
				if calls.Load() != 0 {
					t.Fatalf("onTruncate should not fire, got %d", calls.Load())
				}
				if w.Len() != int(total) {
					t.Fatalf("Len=%d want written=%d", w.Len(), total)
				}
			}
		})
	}
}

func TestLimitedWriterOnTruncateOnce(t *testing.T) {
	var calls atomic.Int32
	w := NewLimitedWriter(8)
	w.SetOnTruncate(func() { calls.Add(1) })

	n, err := w.Write([]byte("0123456789ABCDEF"))
	if err != nil || n != 16 {
		t.Fatalf("write n=%d err=%v", n, err)
	}
	if !w.Truncated() {
		t.Fatal("expected truncated")
	}
	if calls.Load() != 1 {
		t.Fatalf("onTruncate calls=%d want 1", calls.Load())
	}
	if w.Len() != 8 {
		t.Fatalf("buffered len=%d want 8", w.Len())
	}
	if w.Written() != 16 {
		t.Fatalf("written=%d want 16", w.Written())
	}

	_, _ = w.Write([]byte("more"))
	if calls.Load() != 1 {
		t.Fatalf("onTruncate should fire once, got %d", calls.Load())
	}
	if w.Written() != 20 {
		t.Fatalf("written after second write=%d want 20", w.Written())
	}
}

// 默认 limit<=0 时回落到 1MiB，且仍可截断。
func TestLimitedWriterDefaultLimit(t *testing.T) {
	w := NewLimitedWriter(0)
	chunk := bytes.Repeat([]byte("B"), 512<<10) // 512 KiB
	for i := 0; i < 3; i++ {
		_, _ = w.Write(chunk)
	}
	if !w.Truncated() {
		t.Fatal("3x512KiB should exceed default 1MiB")
	}
	if w.Len() != 1<<20 {
		t.Fatalf("default buffered len=%d want 1MiB", w.Len())
	}
}

// 多档短时超量输出：截断后杀进程组（对齐 executor 的 OLE 行为），避免真实磁盘耗尽。
func TestOutputLimitKillsProcessGroup(t *testing.T) {
	if _, err := exec.LookPath("python3"); err != nil {
		t.Skip("python3 not installed")
	}

	cases := []struct {
		name  string
		limit int
	}{
		{name: "limit_1KiB", limit: 1 << 10},
		{name: "limit_4KiB", limit: 4 << 10},
		{name: "limit_64KiB", limit: 64 << 10},
		{name: "limit_256KiB", limit: 256 << 10},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			runOutputFloodProbe(t, tc.limit)
		})
	}
}

// runOutputFloodProbe 启动狂打 stdout 的短时进程，限流截断后 SIGKILL 进程组。
func runOutputFloodProbe(t *testing.T, limit int) {
	t.Helper()

	// 1. 构造 LimitedWriter，首次截断时杀进程组（与 executor.executeCommand 一致）
	stdoutBuf := NewLimitedWriter(limit)
	var killPgid atomic.Int32
	stdoutBuf.SetOnTruncate(func() {
		pgid := int(killPgid.Load())
		if pgid > 0 {
			_ = syscall.Kill(-pgid, syscall.SIGKILL)
			_ = syscall.Kill(pgid, syscall.SIGKILL)
		}
	})

	// 2. 受控洪水：先短睡再狂写，保证 Start 后已写入 pgid，再截断杀组（对齐 executor）
	script := `
import sys, time
time.sleep(0.05)
block = b"X" * 4096
for _ in range(100000):
    sys.stdout.buffer.write(block)
    sys.stdout.buffer.flush()
`
	cmd := exec.Command("python3", "-c", script)
	cmd.Stdout = stdoutBuf
	cmd.Stderr = &bytes.Buffer{}
	cmd.SysProcAttr = &syscall.SysProcAttr{Setpgid: true}

	if err := cmd.Start(); err != nil {
		t.Fatal(err)
	}
	killPgid.Store(int32(cmd.Process.Pid))

	// 3. 短超时等待；应因截断被杀或自然结束，不得挂死
	done := make(chan error, 1)
	go func() { done <- cmd.Wait() }()
	select {
	case <-time.After(8 * time.Second):
		_ = syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
		t.Fatalf("output flood timed out, limit=%d written=%d truncated=%v",
			limit, stdoutBuf.Written(), stdoutBuf.Truncated())
	case waitErr := <-done:
		t.Logf("limit=%d written=%d buffered=%d truncated=%v wait=%v",
			limit, stdoutBuf.Written(), stdoutBuf.Len(), stdoutBuf.Truncated(), waitErr)
	}

	// 4. 断言：发生截断、缓冲不超过上限、实际写出量超过上限 → 对应 OUTPUT_LIMIT_EXCEEDED 证据
	if !stdoutBuf.Truncated() {
		t.Fatalf("expected truncate, limit=%d written=%d", limit, stdoutBuf.Written())
	}
	if stdoutBuf.Len() != limit {
		t.Fatalf("buffered=%d want limit=%d", stdoutBuf.Len(), limit)
	}
	if stdoutBuf.Written() <= int64(limit) {
		t.Fatalf("Written=%d should exceed limit=%d", stdoutBuf.Written(), limit)
	}
}
