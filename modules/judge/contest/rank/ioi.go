package rank

import (
	"context"

	"hei-gin/core/db"
	contestModel "hei-gin/modules/judge/contest"
	submissionModel "hei-gin/modules/judge/submission"
)

type IOIRankEntry struct {
	UserID     string
	TotalScore int
}

type IOIRankCalculator struct{}

func (c *IOIRankCalculator) Calculate(ctx context.Context, contestID string) ([]IOIRankEntry, error) {
	var participants []contestModel.JudgeContestParticipant
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Find(&participants)

	userIDs := make([]string, 0, len(participants))
	for _, p := range participants {
		userIDs = append(userIDs, p.UserID)
	}

	// IOI: highest score per problem, sum of best scores
	var submissions []submissionModel.JudgeSubmission
	db.DB.WithContext(ctx).
		Where("contest_id = ? AND user_id IN ?", contestID, userIDs).
		Find(&submissions)

	type subKey struct {
		UserID    string
		ProblemID string
	}
	maxScore := make(map[subKey]int)
	for _, sub := range submissions {
		key := subKey{UserID: sub.UserID, ProblemID: sub.ProblemID}
		if sub.Score > maxScore[key] {
			maxScore[key] = sub.Score
		}
	}

	userScore := make(map[string]int)
	for key, score := range maxScore {
		userScore[key.UserID] += score
	}

	result := make([]IOIRankEntry, 0, len(userIDs))
	for _, uid := range userIDs {
		score := userScore[uid]
		result = append(result, IOIRankEntry{UserID: uid, TotalScore: score})
	}
	return result, nil
}
