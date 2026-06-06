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
// 修复: 根据竞赛类型 (ACM / OI / IOI) 采用不同排名逻辑:
//
//	ACM:  取首次 AC 的提交, 计算罚时 (每次错误提交 +20min)
//	OI:   取每次提交中每个题目的最高分, 按总分排名
//	IOI:  类似 OI, 取最高分, 但评分方式可能不同
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
		type UserBrief struct {
			ID       string `gorm:"column:id"`
			Username string `gorm:"column:username"`
		}
		var users []UserBrief
		db.DB.WithContext(ctx).Table("sys_user").Where("id IN ?", userIDs).Find(&users)
		for _, u := range users {
			usernameMap[u.ID] = u.Username
		}
		// 补充查询 client_user
		var unresolvedIDs []string
		for _, uid := range userIDs {
			if _, ok := usernameMap[uid]; !ok {
				unresolvedIDs = append(unresolvedIDs, uid)
			}
		}
		if len(unresolvedIDs) > 0 {
			type ClientUserBrief struct {
				ID       string `gorm:"column:id"`
				Nickname string `gorm:"column:nickname"`
			}
			var clientUsers []ClientUserBrief
			db.DB.WithContext(ctx).Table("client_user").Where("id IN ?", unresolvedIDs).Find(&clientUsers)
			for _, u := range clientUsers {
				usernameMap[u.ID] = u.Nickname
			}
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

			if isACM {
				// ACM 模式: 首次 AC 有效, 计算罚时
				for _, s := range submissions {
					detail.Attempts++
					if s.Status == "Accepted" {
						detail.Accepted = true
						detail.Score = rel.Score
						detail.TimeUsed = s.TimeUsed
						detail.SubmitTime = s.CreatedAt
						item.Solved++
						// 罚时: (提交次数-1) * 20 分钟 (毫秒)
						penalty := int64(detail.Attempts-1) * 20 * 60 * 1000
						item.TotalTime += detail.TimeUsed + penalty
						item.Score += rel.Score
						break
					}
				}
			} else if isOI {
				// OI/IOI 模式: 取该题目所有提交中的最高分
				bestScore := 0
				bestTime := int64(0)
				for _, s := range submissions {
					if s.Status == "Accepted" {
						// 优先取 AC 的最高分
						if s.Score > bestScore {
							bestScore = s.Score
							bestTime = s.TimeUsed
							detail.Accepted = true
						}
					}
					// 也记录非 AC 但可能有分数的提交
					if s.Score > bestScore {
						bestScore = s.Score
						bestTime = s.TimeUsed
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
				// 默认 (类似 OI): 取最高分
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

	// 排序
	if isACM {
		// ACM: 解题数降序 → 总用时升序 → 用户ID升序
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
		// OI/IOI: 总分降序 → 总时间降序 → 用户ID升序
		sort.Slice(items, func(i, j int) bool {
			if items[i].Score != items[j].Score {
				return items[i].Score > items[j].Score
			}
			if items[i].TotalTime != items[j].TotalTime {
				return items[i].TotalTime > items[j].TotalTime
			}
			return items[i].UserID < items[j].UserID
		})
	}

	for i := range items {
		items[i].Rank = i + 1
	}

	return items, nil
}
