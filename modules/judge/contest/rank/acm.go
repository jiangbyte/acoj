package rank

import (
	"context"

	"hei-gin/config"
	"hei-gin/core/db"
	contestModel "hei-gin/modules/judge/contest"
	submissionModel "hei-gin/modules/judge/submission"
)

type ACMRankEntry struct {
	UserID       string
	SolvedCount  int
	TotalPenalty int64
}

type ACMProblemEntry struct {
	Attempts int
	Accepted bool
	Penalty  int64
}

type ACMRankCalculator struct{}

func (c *ACMRankCalculator) Calculate(ctx context.Context, contestID string) ([]ACMRankEntry, error) {
	penaltyMinutes := config.C.Contest.ACMPenaltyMinutes
	if penaltyMinutes <= 0 {
		penaltyMinutes = 20
	}

	var participants []contestModel.JudgeContestParticipant
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Find(&participants)

	userIDs := make([]string, 0, len(participants))
	for _, p := range participants {
		userIDs = append(userIDs, p.UserID)
	}

	var submissions []submissionModel.JudgeSubmission
	db.DB.WithContext(ctx).
		Where("contest_id = ? AND user_id IN ?", contestID, userIDs).
		Find(&submissions)

	type subKey struct {
		UserID    string
		ProblemID string
	}
	bestSub := make(map[subKey]submissionModel.JudgeSubmission)
	failCount := make(map[subKey]int)

	for _, sub := range submissions {
		key := subKey{UserID: sub.UserID, ProblemID: sub.ProblemID}
		if sub.Status == "AC" {
			existing, exists := bestSub[key]
			if !exists || (sub.CreatedAt != nil && existing.CreatedAt != nil && sub.CreatedAt.Before(*existing.CreatedAt)) {
				bestSub[key] = sub
			}
		} else if sub.Status != "PENDING" && sub.Status != "JUDGING" {
			failCount[key]++
		}
	}

	userMap := make(map[string]*ACMRankEntry)
	for _, uid := range userIDs {
		userMap[uid] = &ACMRankEntry{UserID: uid}
	}

	for key, sub := range bestSub {
		entry := userMap[key.UserID]
		if entry == nil {
			continue
		}
		entry.SolvedCount++
		penalty := failCount[key] * penaltyMinutes
		var submitTime int64
		if sub.CreatedAt != nil {
			// Use contest start time as reference
		}
		entry.TotalPenalty += int64(penalty) + submitTime

		var existing contestModel.JudgeContestRankItem
		err := db.DB.WithContext(ctx).
			Where("contest_id = ? AND user_id = ? AND problem_id = ?", contestID, key.UserID, key.ProblemID).
			First(&existing).Error
		if err != nil {
			item := contestModel.JudgeContestRankItem{
				ContestID:  contestID,
				UserID:     key.UserID,
				ProblemID:  key.ProblemID,
				Attempts:   failCount[key] + 1,
				Score:      1,
				IsAccepted: true,
				TimePenalty: entry.TotalPenalty,
			}
			db.DB.WithContext(ctx).Create(&item)
		} else {
			db.DB.WithContext(ctx).Model(&existing).
				Where("contest_id = ? AND user_id = ? AND problem_id = ?", contestID, key.UserID, key.ProblemID).
				Updates(map[string]interface{}{
					"attempts":    failCount[key] + 1,
					"is_accepted": true,
					"score":       1,
					"time_penalty": entry.TotalPenalty,
				})
		}
	}

	result := make([]ACMRankEntry, 0, len(userMap))
	for _, entry := range userMap {
		result = append(result, *entry)
	}
	return result, nil
}
