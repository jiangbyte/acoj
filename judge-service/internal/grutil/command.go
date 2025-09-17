package grutil

import (
	"judge-service/internal/config"
	"strings"

	"github.com/zeromicro/go-zero/core/logx"
)

// getRunCommand 获取执行命令
func GetRunCommand(config config.LanguageConfig, buildFile string) []string {
	runCmd := make([]string, len(config.RunCmd))
	for i, part := range config.RunCmd {
		runCmd[i] = strings.ReplaceAll(part, "{exec}", buildFile)
	}

	logx.Infof("得到运行命令: %s", runCmd)
	return runCmd
}

func GetCompileCommand(config config.LanguageConfig, sourceFile string, buildFile string) []string {
	compileCmd := make([]string, len(config.CompileCmd))
	for i, part := range config.CompileCmd {
		compileCmd[i] = strings.ReplaceAll(part, "{source}", sourceFile)
		compileCmd[i] = strings.ReplaceAll(compileCmd[i], "{exec}", buildFile)
	}

	logx.Infof("得到编译命令: %s", compileCmd)
	return compileCmd
}
