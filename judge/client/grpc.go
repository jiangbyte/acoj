package client

import (
	"context"
	"fmt"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	pb "hei-gin/judge/grpc"
)

// grpcExecutor implements ExecutorClient over native gRPC transport (port 5051).
type grpcExecutor struct {
	conn *grpc.ClientConn
}

// newGrpcExecutor creates a gRPC connection to the go-judge instance at addr (host:port, gRPC port 5051).
func newGrpcExecutor(addr string) (*grpcExecutor, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	conn, err := grpc.DialContext(ctx, addr,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithBlock(),
	)
	if err != nil {
		return nil, fmt.Errorf("grpc dial %s failed: %w", addr, err)
	}
	return &grpcExecutor{conn: conn}, nil
}

// Close closes the gRPC connection.
func (e *grpcExecutor) Close() {
	if e.conn != nil {
		e.conn.Close()
	}
}

// Exec sends an Exec request via gRPC unary call.
func (e *grpcExecutor) Exec(req *pb.Request) (*pb.Response, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	var resp pb.Response
	if err := e.conn.Invoke(ctx, "/pb.Executor/Exec", req, &resp); err != nil {
		return nil, fmt.Errorf("grpc Exec failed: %w", err)
	}
	return &resp, nil
}

// FileAdd sends a FileAdd request via gRPC unary call.
func (e *grpcExecutor) FileAdd(content *pb.FileContent) (*pb.FileID, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	var resp pb.FileID
	if err := e.conn.Invoke(ctx, "/pb.Executor/FileAdd", content, &resp); err != nil {
		return nil, fmt.Errorf("grpc FileAdd failed: %w", err)
	}
	return &resp, nil
}
