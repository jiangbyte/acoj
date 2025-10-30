package utils

import (
	"regexp"
	"strings"
)

func FormatBytesKB(bytes uint64) float64 {
	return float64(bytes) / 1024
}

// SplitAndNormalize 将文本按行分割并标准化每行
func SplitAndNormalize(str string) []string {
	// 标准化换行符
	str = strings.ReplaceAll(str, "\r\n", "\n")
	str = strings.ReplaceAll(str, "\r", "\n")

	lines := strings.Split(str, "\n")
	normalized := make([]string, 0, len(lines))

	for _, line := range lines {
		// 移除每行的首尾空白
		line = strings.TrimSpace(line)
		if line != "" { // 可选：是否忽略空行
			normalized = append(normalized, line)
		}
	}

	return normalized
}

// 评估两个标准答案与用户提交是否相同
func CompareOutput(ans string, output string) bool {
	// if NormalizeLineEndings(ans) == NormalizeLineEndings(output) {
	// 	return true
	// } else {
	// 	return false
	// }
	// CompareOutput 按行比较，忽略行尾差异
	ansLines := SplitAndNormalize(ans)
	outputLines := SplitAndNormalize(output)

	if len(ansLines) != len(outputLines) {
		return false
	}

	for i := range ansLines {
		if ansLines[i] != outputLines[i] {
			return false
		}
	}

	return true
}

// 综合自动屏蔽文件路径
func AutoMaskAllFilePaths(text string) string {
	// 首先处理编译错误格式的路径
	result := maskCompilerErrorPaths(text)
	// 然后处理普通文件路径
	result = maskGeneralFilePaths(result)
	return result
}

// 屏蔽编译错误中的文件路径
func maskCompilerErrorPaths(text string) string {
	// 匹配: 路径:行号:列号: 消息
	re := regexp.MustCompile(`([/\w\.-]+(/[/\w\.-]+)*):(\d+):(\d+):`)
	return re.ReplaceAllStringFunc(text, func(match string) string {
		parts := strings.Split(match, ":")
		if len(parts) >= 3 {
			filePath := parts[0]
			pathParts := strings.Split(filePath, "/")
			filename := pathParts[len(pathParts)-1]
			return "/" + filename + ":" + strings.Join(parts[1:], ":")
		}
		return ""
	})
}

// 屏蔽普通文件路径
func maskGeneralFilePaths(text string) string {
	// 匹配绝对路径
	re := regexp.MustCompile(`(/[^\s:]+)+`)
	return re.ReplaceAllStringFunc(text, func(match string) string {
		pathParts := strings.Split(match, "/")
		if len(pathParts) > 1 {
			return "/" + pathParts[len(pathParts)-1]
		}
		return ""
	})
}
