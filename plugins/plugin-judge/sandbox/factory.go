package sandbox

import (
	gojudgeAdapter "hei-gin/plugins/plugin-judge/sandbox/gojudge"
	"hei-gin/plugins/plugin-judge/judgetypes"
)

// NewBackend 根据类型创建沙箱后端
func NewBackend(endpoint string, timeout int) (judgetypes.SandboxBackend, error) {
	return gojudgeAdapter.NewBackend(endpoint, timeout)
}
