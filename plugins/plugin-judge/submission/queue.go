package submission

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"hei-gin/sdk/db"
)

const queueKey = "judge:submission:queue"

// Enqueue 将提交 ID 入队
func Enqueue(submissionID string) error {
	ctx := context.Background()
	return db.Redis.LPush(ctx, queueKey, submissionID).Err()
}

// Dequeue 从队列取出一个提交 ID（阻塞）
func Dequeue(timeout int) (string, error) {
	ctx := context.Background()
	result, err := db.Redis.BRPop(ctx, time.Duration(timeout)*time.Second, queueKey).Result()
	if err != nil {
		return "", err
	}
	if len(result) < 2 {
		return "", nil
	}
	return result[1], nil
}

// QueueLength 返回队列长度
func QueueLength() (int64, error) {
	ctx := context.Background()
	return db.Redis.LLen(ctx, queueKey).Result()
}

// QueueStatus 队列状态
type QueueStatus struct {
	Length int64 `json:"length"`
}

// GetQueueStatus 获取队列状态
func GetQueueStatus() (*QueueStatus, error) {
	length, err := QueueLength()
	if err != nil {
		return nil, err
	}
	return &QueueStatus{Length: length}, nil
}

// PublishJudgeEvent 发布判题事件
func PublishJudgeEvent(submissionID, status string) {
	ctx := context.Background()
	event := map[string]any{
		"type":          "judge_update",
		"submission_id": submissionID,
		"status":        status,
	}
	data, err := json.Marshal(event)
	if err != nil {
		return
	}
	if err := db.Redis.Publish(ctx, "judge:events", string(data)).Err(); err != nil {
		log.Printf("[queue] publish judge event error: %v", err)
	}
}
