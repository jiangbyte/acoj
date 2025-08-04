package utils

import (
	"encoding/json"
	"os"
	"time"
	"fmt"
	
	"sandbox/internal/config"
	"sandbox/internal/result"
)

/* 记录执行日志 */
func LogCommand(isCompile bool, argument config.Argument, resource config.Resource, additional config.Additional, result result.Result) {
	if additional.LogFilePath == "" {
		return
	}

	logFile, err := os.OpenFile(additional.LogFilePath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return
	}
	defer logFile.Close()

	logData := map[string]interface{}{
		"isCompile":  isCompile,
		"argument":   argument,
		"resource":   resource,
		"additional": additional,
		"result":     result,
		"timestamp":  time.Now().Format(time.RFC3339),
	}

	jsonData, _ := json.Marshal(logData)
	logFile.Write(jsonData)
	logFile.WriteString("\n")
}

/* 打印所有参数（调试用） */
func PrintAll(argument config.Argument, resource config.Resource, additional config.Additional) {
	data := map[string]interface{}{
		"Argument":   argument,
		"Resource":   resource,
		"Additional": additional,
	}
	jsonData, _ := json.MarshalIndent(data, "", "    ")
	fmt.Println(string(jsonData))
}