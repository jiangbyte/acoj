package queue

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/redis/go-redis/v9"

	"hei-gin/config"
	"hei-gin/core/db"
)

// Message is the message structure pushed to Redis Stream for judging.
type Message struct {
	SubmissionID string `json:"submission_id"`
	ProblemID    string `json:"problem_id"`
	UserID       string `json:"user_id"`
	Language     string `json:"language"`
	JudgeMethod  string `json:"judge_method"`
	ContestID    string `json:"contest_id,omitempty"`
	ContestMode  string `json:"contest_mode,omitempty"`
	IsPretest    bool   `json:"is_pretest"`
	ProblemLabel string `json:"problem_label,omitempty"`
	SetID        string `json:"set_id,omitempty"`
	Code         string `json:"code,omitempty"`
}

// EnqueueSubmission pushes a judging message to the Redis Stream.
func EnqueueSubmission(msg *Message) error {
	ctx := context.Background()
	data, err := json.Marshal(msg)
	if err != nil {
		return fmt.Errorf("failed to marshal queue message: %w", err)
	}

	streamKey := config.C.Judge.Redis.StreamKey
	_, err = db.Redis.XAdd(ctx, &redis.XAddArgs{
		Stream: streamKey,
		Values: map[string]interface{}{
			"data": string(data),
		},
	}).Result()
	if err != nil {
		return fmt.Errorf("failed to enqueue submission %s: %w", msg.SubmissionID, err)
	}
	return nil
}
