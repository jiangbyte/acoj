package rank

import (
	"context"

	"hei-gin/core/db"
	contestModel "hei-gin/modules/judge/contest"
	submissionModel "hei-gin/modules/judge/submission"
)

type OIRankEntry struct {
	UserID     string
	TotalScore int
}

type OIRankCalculator struct{}

func (c *OIRankCalculator) Calculate(ctx context.Context, contestID string) ([]OIRankEntry, error) {
	var participants []contestModel.JudgeContestParticipant
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Find(&participants)

	userIDs := make([]string, 0, len(participants))
	for _, p := range participants {
		userIDs = append(userIDs, p.UserID)
	}

	// Query submissions directly, then aggregate by user
	var submissions []submissionModel.JudgeSubmission
	db.DB.WithContext(ctx).
		Where("contest_id = ? AND user_id IN ? AND status = ?", contestID, userIDs, "AC").
		Find(&submissions)

	type subKey struct {
		UserID    string
		ProblemID string
	}
	bestScore := make(map[subKey]int)
	for _, sub := range submissions {
		key := subKey{UserID: sub.UserID, ProblemID: sub.ProblemID}
		if sub.Score > bestScore[key] {
			bestScore[key] = sub.Score
		}
	}

	userScore := make(map[string]int)
	for key, score := range bestScore {
		userScore[key.UserID] += score
	}

	result := make([]OIRankEntry, 0, len(userScore))
	for _, uid := range userIDs {
		score := userScore[uid]
		result = append(result, OIRankEntry{UserID: uid, TotalScore: score})
	}
	return result, nil
}
