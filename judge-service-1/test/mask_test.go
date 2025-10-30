package test

import (
	"judge-service/internal/utils"
	"testing"
)

func TestAutoMaskAllFilePaths(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name: "编译错误路径屏蔽",
			input: `/tmp/judgespace/dd8c941e8363437aa13b0db5932eea0e/source/Main.cpp: In function 'int main()':
/tmp/judgespace/dd8c941e8363437aa13b0db5932eea0e/source/Main.cpp:17:5: error: expected ';' before 'for'`,
			expected: `***/Main.cpp: In function 'int main()':
***/Main.cpp:17:5: error: expected ';' before 'for'`,
		},
		{
			name:     "普通文件路径屏蔽",
			input:    "编译文件 /tmp/build/main.go 完成",
			expected: "编译文件 ***/main.go 完成",
		},
		{
			name:     "Windows路径屏蔽",
			input:    "错误发生在 C:\\Users\\project\\main.go 第10行",
			expected: "错误发生在 C:\\Users\\project\\main.go 第10行", // 当前正则不处理Windows路径
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := utils.AutoMaskAllFilePaths(tt.input)
			if result != tt.expected {
				t.Errorf("测试失败 %s:\n输入: %s\n期望: %s\n实际: %s",
					tt.name, tt.input, tt.expected, result)
			}
		})
	}
}

// func TestMaskCompilerErrorPaths(t *testing.T) {
// 	input := `/tmp/judgespace/dd8c941e8363437aa13b0db5932eea0e/source/Main.cpp:17:5: error`
// 	expected := `***/Main.cpp:17:5: error`

// 	result := utils.maskCompilerErrorPaths(input)
// 	if result != expected {
// 		t.Errorf("编译错误路径屏蔽失败:\n输入: %s\n期望: %s\n实际: %s", input, expected, result)
// 	}
// }

// func TestMaskGeneralFilePaths(t *testing.T) {
// 	input := "文件路径为 /home/user/project/src/main.go"
// 	expected := "文件路径为 ***/main.go"

// 	result := utils.maskGeneralFilePaths(input)
// 	if result != expected {
// 		t.Errorf("普通路径屏蔽失败:\n输入: %s\n期望: %s\n实际: %s", input, expected, result)
// 	}
// }
