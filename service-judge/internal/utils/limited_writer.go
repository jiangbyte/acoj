package utils

import (
	"bytes"
	"sync"
	"sync/atomic"
)

// LimitedWriter 限制写入字节数，超限后丢弃并标记 truncated；首次截断可回调（用于杀进程组）。
type LimitedWriter struct {
	buf        bytes.Buffer
	limit      int
	truncated  int32
	written    int64
	onTruncate func()
	once       sync.Once
}

// NewLimitedWriter 创建有字节上限的写入器。
func NewLimitedWriter(limit int) *LimitedWriter {
	if limit < 1 {
		limit = 1 << 20 // 默认 1 MiB
	}
	return &LimitedWriter{limit: limit}
}

// SetOnTruncate 设置首次截断时的回调（例如 SIGKILL 进程组）。
func (w *LimitedWriter) SetOnTruncate(fn func()) {
	w.onTruncate = fn
}

func (w *LimitedWriter) Write(p []byte) (int, error) {
	remain := w.limit - w.buf.Len()
	if remain <= 0 {
		w.markTruncated()
		atomic.AddInt64(&w.written, int64(len(p)))
		return len(p), nil
	}
	if len(p) > remain {
		_, _ = w.buf.Write(p[:remain])
		w.markTruncated()
		atomic.AddInt64(&w.written, int64(len(p)))
		return len(p), nil
	}
	n, err := w.buf.Write(p)
	atomic.AddInt64(&w.written, int64(n))
	return n, err
}

func (w *LimitedWriter) markTruncated() {
	w.once.Do(func() {
		atomic.StoreInt32(&w.truncated, 1)
		if w.onTruncate != nil {
			w.onTruncate()
		}
	})
}

func (w *LimitedWriter) String() string {
	return w.buf.String()
}

func (w *LimitedWriter) Truncated() bool {
	return atomic.LoadInt32(&w.truncated) == 1
}

func (w *LimitedWriter) Written() int64 {
	return atomic.LoadInt64(&w.written)
}

func (w *LimitedWriter) Len() int {
	return w.buf.Len()
}
