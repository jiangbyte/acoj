package result

import (
	"encoding/json"
	"fmt"
)

/* 执行结果结构体 */
type Result struct {
	ExitCode int    `json:"exitCode"` // 退出状态码
	Signal   int    `json:"signal"`   // 导致终止的信号
	Message  string `json:"message"`  // 结果消息

	CpuTime  int64 `json:"cpuTime"`  // 实际CPU时间(毫秒)
	RealTime int64 `json:"realTime"` // 实际运行时间(毫秒)
	Memory   int64 `json:"memory"`   // 实际内存使用(KB)
}

/* 打印执行结果 */
func (r *Result) Print() {
	jsonData, _ := json.MarshalIndent(r, "", "    ")
	fmt.Println(string(jsonData))
}
