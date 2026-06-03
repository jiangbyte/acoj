package worker

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/redis/go-redis/v9"

	"hei-gin/config"
	"hei-gin/core/db"
)

// QueueConsumer reads messages from the Redis Stream and processes them.
type QueueConsumer struct {
	worker  *Worker
	stopCh  chan struct{}
	group   string
	stream  string
	started bool
}

// NewQueueConsumer creates a new QueueConsumer.
func NewQueueConsumer(worker *Worker) *QueueConsumer {
	return &QueueConsumer{
		worker: worker,
		stopCh: make(chan struct{}),
		group:  config.C.Judge.Redis.ConsumerGroup,
		stream: config.C.Judge.Redis.StreamKey,
	}
}

// Start begins consuming messages from the Redis Stream.
func (qc *QueueConsumer) Start() error {
	ctx := context.Background()
	err := db.Redis.XGroupCreateMkStream(ctx, qc.stream, qc.group, "0").Err()
	if err != nil && err.Error() != "BUSYGROUP Consumer Group name already exists" {
		return fmt.Errorf("failed to create consumer group: %w", err)
	}
	qc.started = true
	log.Printf("[QueueConsumer] Started consuming from stream=%s group=%s", qc.stream, qc.group)
	return nil
}

// Consume reads one message from the stream (blocking).
func (qc *QueueConsumer) Consume(ctx context.Context) (*redis.XMessage, error) {
	if !qc.started {
		return nil, fmt.Errorf("consumer not started")
	}

	results, err := db.Redis.XReadGroup(ctx, &redis.XReadGroupArgs{
		Group:    qc.group,
		Consumer: qc.worker.consumerName(),
		Streams:  []string{qc.stream, ">"},
		Count:    1,
		Block:    5 * time.Second,
	}).Result()

	if err == redis.Nil {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("XReadGroup error: %w", err)
	}
	if len(results) == 0 || len(results[0].Messages) == 0 {
		return nil, nil
	}
	return &results[0].Messages[0], nil
}

// ClaimPending claims pending messages from PEL.
func (qc *QueueConsumer) ClaimPending(ctx context.Context) ([]redis.XMessage, error) {
	claimIdle := time.Duration(config.C.Judge.Redis.ClaimIdleMs) * time.Millisecond
	messages, _, err := db.Redis.XAutoClaim(ctx, &redis.XAutoClaimArgs{
		Stream:   qc.stream,
		Group:    qc.group,
		Consumer: qc.worker.consumerName(),
		MinIdle:  claimIdle,
		Start:    "0",
		Count:    10,
	}).Result()
	if err != nil {
		return nil, err
	}
	return messages, nil
}

// Ack acknowledges a message as processed.
func (qc *QueueConsumer) Ack(ctx context.Context, msgID string) error {
	return db.Redis.XAck(ctx, qc.stream, qc.group, msgID).Err()
}
