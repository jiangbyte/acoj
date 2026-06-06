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

// Health 检查 go-judge 是否正常工作
// 先检查 gRPC 连接状态，再发送一个简单的 true 命令验证
func (c *GoJudgeClient) Health() (bool, string, error) {
	state := c.conn.GetState()
	if state != connectivity.Ready && state != connectivity.Idle {
		return false, state.String(), fmt.Errorf("connection state: %s", state.String())
	}

	// 发送一个简单命令验证 go-judge 实际可用
	checkCmd := &pb.Request_CmdType{}
	checkCmd.SetArgs([]string{"/usr/bin/true"})
	checkCmd.SetCpuTimeLimit(1000000000)     // 1s
	checkCmd.SetEnv([]string{"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"})
	checkCmd.SetMemoryLimit(16777216)         // 16MB
	checkCmd.SetProcLimit(10)

	req := &pb.Request{}
	req.SetCmd([]*pb.Request_CmdType{checkCmd})

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	resp, err := c.client.Exec(ctx, req)
	if err != nil {
		return false, state.String(), fmt.Errorf("health check exec failed: %w", err)
	}
	results := resp.GetResults()
	if len(results) == 0 || results[0].GetStatus() != pb.Response_Result_Accepted {
		errMsg := "health check: unexpected result"
		if len(results) > 0 {
			errMsg = fmt.Sprintf("health check: status=%v, error=%s", results[0].GetStatus(), results[0].GetError())
		}
		return false, state.String(), fmt.Errorf("%s", errMsg)
	}

	return true, state.String(), nil
}
