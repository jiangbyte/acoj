package grutil

import (
	"regexp"
	"strings"
)

func FormatBytesKB(bytes uint64) float64 {
	return float64(bytes) / 1024
}

// normalizeLineEndings 将不同风格的换行符统一为\n
func NormalizeLineEndings(str string) string {
	// 将\r\n替换为\n
	str = strings.ReplaceAll(str, "\r\n", "\n")
	// 将单独的\r也替换为\n（处理Mac OS旧格式）
	str = strings.ReplaceAll(str, "\r", "\n")
	return str
}

// 评估两个标准答案与用户提交是否相同
func CompareOutput(ans string, output string) bool {
	if NormalizeLineEndings(ans) == NormalizeLineEndings(output) {
		return true
	} else {
		return false
	}
}

// filterFilePath 过滤掉消息中的文件路径
func FilterFilePath(message string) string {
	// 使用正则表达式匹配并移除文件路径
	// 匹配模式：以/tmp/或类似开头的文件路径
	pattern := `/\S+?\.(cpp|c|java|py|js|go):\d+:\d+:`
	re := regexp.MustCompile(pattern)

	// 移除文件路径部分，只保留错误信息
	filtered := re.ReplaceAllString(message, "")

	// 如果过滤后为空，返回原始消息
	if filtered == "" {
		return message
	}

	return filtered
}
