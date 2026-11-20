package app

import (
	"os"
	"os/signal"
	"syscall"
)

// SignalHandler 信号处理器
type SignalHandler struct {
	shutdownCallback func()
}

// NewSignalHandler 创建信号处理器
func NewSignalHandler() *SignalHandler {
	return &SignalHandler{}
}

// SetShutdownCallback 设置关闭回调函数
func (sh *SignalHandler) SetShutdownCallback(callback func()) {
	sh.shutdownCallback = callback
}

// WaitForSignal 等待信号
func (sh *SignalHandler) WaitForSignal() {
	ch := make(chan os.Signal, 1)
	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
	
	// 等待信号
	<-ch
	
	// 执行关闭回调
	if sh.shutdownCallback != nil {
		sh.shutdownCallback()
	}
}