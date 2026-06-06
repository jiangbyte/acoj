package contest

import (
	"context"
	"sort"

	"hei-gin/sdk/db"
)

// JudgeSubmissionBrief 提交记录简表（避免循环依赖）
type JudgeSubmissionBrief struct {
	ID        string `gorm:"column:id"`
	ProblemID string `gorm:"column:problem_id"`
	UserID    string `gorm:"column:user_id"`
	ContestID string `gorm:"column:contest_id"`
	Status    string `gorm:"column:status"`
	Score     int    `gorm:"column:score"`
	TimeUsed  int64  `gorm:"column:time_used"`
	CreatedAt string `gorm:"column:created_at"`
}

func (JudgeSubmissionBrief) TableName() string { return "judge_submission" }

// CalculateRank 计算竞赛排行榜
func CalculateRank(contestID string) ([]ContestRankItem, error) {
	ctx := context.Background()

	var contest JudgeContest
	if err := db.DB.WithContext(ctx).Where("id = ?", contestID).First(&contest).Error; err != nil {
		return nil, err
	}

	var rels []RelContestProblem
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Order("sort ASC").Find(&rels)

	if len(rels) == 0 {
		return nil, nil
	}

	var userRels []RelContestUser
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Find(&userRels)

	problemIDs := make([]string, len(rels))
	for i, r := range rels {
		problemIDs[i] = r.ProblemID
	}

	type UserBrief struct {
		ID       string `gorm:"column:id"`
		Username string `gorm:"column:username"`
	}
	userIDs := make([]string, len(userRels))
	for i, r := range userRels {
		userIDs[i] = r.UserID
	}

	usernameMap := make(map[string]string)
	if len(userIDs) > 0 {
		var users []UserBrief
		db.DB.WithContext(ctx).Table("sys_user").Where("id IN ?", userIDs).Find(&users)
		for _, u := range users {
			usernameMap[u.ID] = u.Username
		}
	}

	startTime := ""
	if contest.StartTime != nil {
		startTime = contest.StartTime.Format("2006-01-02 15:04:05")
	}
	endTime := ""
	if contest.EndTime != nil {
		endTime = contest.EndTime.Format("2006-01-02 15:04:05")
	}

	var items []ContestRankItem

	for _, ur := range userRels {
		item := ContestRankItem{
			UserID:   ur.UserID,
			Username: usernameMap[ur.UserID],
		}

		for _, rel := range rels {
			var submissions []JudgeSubmissionBrief
			tx := db.DB.WithContext(ctx).
				Where("contest_id = ? AND problem_id = ? AND user_id = ?", contestID, rel.ProblemID, ur.UserID)
			if startTime != "" {
				tx = tx.Where("created_at >= ?", startTime)
			}
			if endTime != "" {
				tx = tx.Where("created_at <= ?", endTime)
			}
			tx.Order("created_at ASC").Find(&submissions)

			detail := ProblemRankItem{
				ProblemID: rel.ProblemID,
			}

			for _, s := range submissions {
				detail.Attempts++
				if s.Status == "Accepted" {
					detail.Accepted = true
					detail.Score = s.Score
					detail.TimeUsed = s.TimeUsed
					detail.SubmitTime = s.CreatedAt
					item.Solved++
					penalty := int64(detail.Attempts-1) * 20 * 60 * 1000
					item.TotalTime += detail.TimeUsed + penalty
					item.Score += s.Score
					break
				}
			}
			item.Details = append(item.Details, detail)
		}

		items = append(items, item)
	}

	sort.Slice(items, func(i, j int) bool {
		if items[i].Solved != items[j].Solved {
			return items[i].Solved > items[j].Solved
		}
		if items[i].TotalTime != items[j].TotalTime {
			return items[i].TotalTime < items[j].TotalTime
		}
		return items[i].UserID < items[j].UserID
	})

	for i := range items {
		items[i].Rank = i + 1
	}

	return items, nil
}
