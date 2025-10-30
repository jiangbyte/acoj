package main

import (
	"flag"
	"fmt"
	"os"
	"similarity-service/internal/app"

	"github.com/zeromicro/go-zero/core/logx"
)

var (
	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")
)

func main() {
	flag.Parse()

	logx.SetLevel(logx.InfoLevel)

	// 创建应用管理器
	appManager := app.NewAppManager(*configFile, *useNacos)

	// 启动应用
	if err := appManager.Run(); err != nil {
		fmt.Printf("应用启动失败: %v\n", err)
		os.Exit(1)
	}
}
