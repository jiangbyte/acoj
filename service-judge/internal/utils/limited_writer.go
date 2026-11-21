package utils

import (
	"bytes"
	"sync/atomic"
)

// LimitedWriter 限制写入字节数，超限后丢弃并标记 truncated。
type LimitedWriter struct {
	buf       bytes.Buffer
	limit     int
	truncated int32
	written   int64
}

// NewLimitedWriter 创建有字节上限的写入器。
func NewLimitedWriter(limit int) *LimitedWriter {
	if limit < 1 {
		limit = 1 << 20 // 默认 1 MiB
	}
	return &LimitedWriter{limit: limit}
}

func (w *LimitedWriter) Write(p []byte) (int, error) {
	remain := w.limit - w.buf.Len()
	if remain <= 0 {
		atomic.StoreInt32(&w.truncated, 1)
		atomic.AddInt64(&w.written, int64(len(p)))
		return len(p), nil
	}
	if len(p) > remain {
		_, _ = w.buf.Write(p[:remain])
		atomic.StoreInt32(&w.truncated, 1)
		atomic.AddInt64(&w.written, int64(len(p)))
		return len(p), nil
	}
	n, err := w.buf.Write(p)
	atomic.AddInt64(&w.written, int64(n))
	return n, err
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
