package dto

// 定义监控结果结构体
type MonitorResult struct {
	MaxMemoryUsage uint64
	PeakCPULoad    float64
	TotalSamples   int
	IsKilled       bool
	Reason         string
}