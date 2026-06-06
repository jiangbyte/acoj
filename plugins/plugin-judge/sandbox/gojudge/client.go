package gojudge

import (
	"context"
	"fmt"
	"strings"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/connectivity"
	"google.golang.org/grpc/credentials/insecure"

	"github.com/criyle/go-judge/pb"
)

// GoJudgeClient go-judge gRPC 客户端
type GoJudgeClient struct {
	conn   *grpc.ClientConn
	client pb.ExecutorClient
}

// NewGoJudgeClient 创建 go-judge gRPC 客户端
func NewGoJudgeClient(endpoint string, timeout int) (*GoJudgeClient, error) {
	endpoint = strings.TrimPrefix(endpoint, "http://")
	endpoint = strings.TrimPrefix(endpoint, "https://")
	endpoint = strings.TrimRight(endpoint, "/")

	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second)
	defer cancel()

	conn, err := grpc.DialContext(ctx, endpoint,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithBlock(),
	)
	if err != nil {
		return nil, fmt.Errorf("grpc dial: %w", err)
	}

	return &GoJudgeClient{
		conn:   conn,
		client: pb.NewExecutorClient(conn),
	}, nil
}

// Close 关闭连接
func (c *GoJudgeClient) Close() error {
	return c.conn.Close()
}

// Exec 执行代码
func (c *GoJudgeClient) Exec(req *pb.Request) (*pb.Response, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()
	return c.client.Exec(ctx, req)
}

// Health 检查连接状态（不发起 RPC，仅检查 gRPC 连接状态）
func (c *GoJudgeClient) Health() (bool, string, error) {
	state := c.conn.GetState()
	if state == connectivity.Ready || state == connectivity.Idle {
		return true, state.String(), nil
	}
	return false, state.String(), fmt.Errorf("connection state: %s", state.String())
}
