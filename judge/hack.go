package judge

import (
	"context"
	"fmt"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/judge/queue"
	submissionModel "hei-gin/modules/judge/submission"
)

// HandleHack handles a CF-style hack attempt.
func HandleHack(ctx context.Context, targetSubmissionID string, hackInput string, problemID string) (string, error) {
	var target submissionModel.JudgeSubmission
	if err := db.DB.WithContext(ctx).First(&target, "id = ?", targetSubmissionID).Error; err != nil {
		return "", fmt.Errorf("target submission not found: %w", err)
	}
	if target.Status != "AC" {
		return "", fmt.Errorf("can only hack AC submissions")
	}
	_ = hackInput
	_ = problemID
	return fmt.Sprintf("hack attempt for submission %s logged", targetSubmissionID), nil
}

// SystemTest triggers a full re-judge for all pretest-only submissions in a contest.
func SystemTest(ctx context.Context, contestID string) error {
	var pretestSubs []submissionModel.JudgeSubmission
	db.DB.WithContext(ctx).
		Where("contest_id = ? AND is_pretest = ? AND status = ?", contestID, true, "AC").
		Find(&pretestSubs)

	for _, sub := range pretestSubs {
		newSub := submissionModel.JudgeSubmission{
			ID:          utils.GenerateID(),
			UserID:      sub.UserID,
			ProblemID:   sub.ProblemID,
			ContestID:   &contestID,
			ContestMode: sub.ContestMode,
			IsPretest:   false,
			Language:    sub.Language,
			Code:        sub.Code,
			Status:      submissionModel.StatusPending,
		}
		db.DB.WithContext(ctx).Create(&newSub)

		qmsg := &queue.Message{
			SubmissionID: newSub.ID,
			ProblemID:    newSub.ProblemID,
			UserID:       newSub.UserID,
			Language:     newSub.Language,
			ContestID:    contestID,
			ContestMode:  sub.ContestMode,
			Code:         sub.Code,
		}
		if err := queue.EnqueueSubmission(qmsg); err != nil {
			return fmt.Errorf("failed to enqueue system test for submission %s: %w", newSub.ID, err)
		}
	}
	return nil
}
