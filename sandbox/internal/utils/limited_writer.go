package utils

import (
	"fmt"
	"io"
)

/* 带限制的Writer实现 */
type LimitedWriter struct {
	Writer  io.Writer // 底层Writer
	Max     int64     // 最大允许字节数
	Written int64     // 已写入字节数
}

// Write方法实现，带大小限制检查
func (l *LimitedWriter) Write(p []byte) (n int, err error) {
	if l.Max > 0 && l.Written+int64(len(p)) > l.Max {
		// 超过限制时只写入剩余允许的部分
		n, _ = l.Writer.Write(p[:l.Max-l.Written])
		l.Written = l.Max
		return n, fmt.Errorf("output size limit exceeded")
	}
	n, err = l.Writer.Write(p)
	l.Written += int64(n)
	return
}