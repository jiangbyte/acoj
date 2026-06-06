package contest

import (
	"context"
	"sort"
	"time"

	"hei-gin/sdk/db"
)

// JudgeSubmissionBrief 提交记录简表（避免循环依赖）
type JudgeSubmissionBrief struct {
	ID        string     `gorm:"column:id"`
	ProblemID string     `gorm:"column:problem_id"`
	UserID    string     `gorm:"column:user_id"`
	ContestID string     `gorm:"column:contest_id"`
	Status    string     `gorm:"column:status"`
	Score     int        `gorm:"column:score"`
	TimeUsed  int64      `gorm:"column:time_used"` // ns
	CreatedAt *time.Time `gorm:"column:created_at"`
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

	userIDs := make([]string, len(userRels))
	for i, r := range userRels {
		userIDs[i] = r.UserID
	}

	usernameMap := make(map[string]string)
	if len(userIDs) > 0 {
		type ClientUserBrief struct {
			ID       string `gorm:"column:id"`
			Nickname string `gorm:"column:nickname"`
		}
		var clientUsers []ClientUserBrief
		db.DB.WithContext(ctx).Table("client_user").Where("id IN ?", userIDs).Find(&clientUsers)
		for _, u := range clientUsers {
			usernameMap[u.ID] = u.Nickname
		}
		// 未在 client_user 中查到的用户直接用 ID 显示
		for _, uid := range userIDs {
			if _, ok := usernameMap[uid]; !ok {
				usernameMap[uid] = uid
			}
		}
	}

	startTime := contest.StartTime
	endTime := contest.EndTime

	isACM := contest.Type == "ACM"
	isOI := contest.Type == "OI" || contest.Type == "IOI"

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
			if startTime != nil {
				tx = tx.Where("created_at >= ?", startTime)
			}
			if endTime != nil {
				tx = tx.Where("created_at <= ?", endTime)
			}
			tx.Order("created_at ASC").Find(&submissions)

			detail := ProblemRankItem{
				ProblemID: rel.ProblemID,
			}

			if isACM {
				for _, s := range submissions {
					detail.Attempts++
					if s.Status == "Accepted" {
						detail.Accepted = true
						detail.Score = rel.Score
						detail.TimeUsed = s.TimeUsed
						if s.CreatedAt != nil {
							detail.SubmitTime = s.CreatedAt.Format("2006-01-02 15:04:05")
						}
						item.Solved++
						penaltyNs := int64(detail.Attempts-1) * 20 * 60 * 1000 * 1000000
						item.TotalTime += s.TimeUsed + penaltyNs
						item.Score += rel.Score
						break
					}
				}
			} else if isOI {
				bestScore := 0
				bestTime := int64(0)
				for _, s := range submissions {
					if s.Score > bestScore {
						bestScore = s.Score
						bestTime = s.TimeUsed
						detail.Accepted = s.Status == "Accepted"
					}
				}
				detail.Score = bestScore
				detail.TimeUsed = bestTime
				detail.Attempts = len(submissions)
				if bestScore > 0 {
					item.Solved++
					item.Score += bestScore
				}
			} else {
				bestScore := 0
				for _, s := range submissions {
					if s.Score > bestScore {
						bestScore = s.Score
						detail.Accepted = s.Status == "Accepted"
					}
				}
				detail.Score = bestScore
				detail.Attempts = len(submissions)
				if bestScore > 0 {
					item.Solved++
					item.Score += bestScore
				}
			}

			item.Details = append(item.Details, detail)
		}

		items = append(items, item)
	}

	if isACM {
		sort.Slice(items, func(i, j int) bool {
			if items[i].Solved != items[j].Solved {
				return items[i].Solved > items[j].Solved
			}
			if items[i].TotalTime != items[j].TotalTime {
				return items[i].TotalTime < items[j].TotalTime
			}
			return items[i].UserID < items[j].UserID
		})
	} else {
		sort.Slice(items, func(i, j int) bool {
			if items[i].Score != items[j].Score {
				return items[i].Score > items[j].Score
			}
			return items[i].UserID < items[j].UserID
		})
	}

	for i := range items {
		items[i].Rank = i + 1
	}

	return items, nil
}
