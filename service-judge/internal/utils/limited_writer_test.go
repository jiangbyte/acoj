package utils

import (
	"sync/atomic"
	"testing"
)

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
}
