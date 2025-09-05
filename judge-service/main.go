package main

import (
	"context"
	"flag"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/logic"
	"judge-service/internal/svc"
	"os"
	"os/signal"
	"syscall"

	"github.com/zeromicro/go-zero/core/conf"
	"github.com/zeromicro/go-zero/core/logx"
)

var configFile = flag.String("f", "etc/judge.yaml", "the config file")

func main() {
	flag.Parse()

	var localConfig config.Config
	conf.MustLoad(*configFile, &localConfig)

	ctx := svc.NewServiceContext(localConfig)
	consumeLogic := logic.NewConsumeLogic(context.Background(), ctx)

	// 启动消费者
	go consumeLogic.StartConsuming()

	// 优雅退出
	ch := make(chan os.Signal, 1)
	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
	<-ch

	fmt.Println("Shutting down server...")
	// 关闭通道和连接
	if err := ctx.ProblemChannel.Close(); err != nil {
		logx.Errorf("Error closing ProblemChannel RabbitMQ connection: %v", err)
	}
	if err := ctx.ProblemSetChannel.Close(); err != nil {
		logx.Errorf("Error closing ProblemSetChannel RabbitMQ connection: %v", err)
	}
	if err := ctx.RabbitMQ.Close(); err != nil {
		logx.Errorf("Error closing RabbitMQ connection: %v", err)
	}

	fmt.Println("Server exited")
}
