package contest

import (
	"context"
	"strings"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// Contest status constants
const (
	ContestStatusPending  = "PENDING"
	ContestStatusRunning  = "RUNNING"
	ContestStatusFrozen   = "FROZEN"
	ContestStatusEnded    = "ENDED"
	ContestStatusUnfrozen = "UNFROZEN"
)

// StatusTransition checks all contests and auto-transitions their status.
// Should be called periodically (every minute) from a cron job.
func StatusTransition(ctx context.Context) {
	now := time.Now()
	var contests []JudgeContest
	db.DB.WithContext(ctx).Where("status IN ?", []string{ContestStatusPending, ContestStatusRunning, ContestStatusFrozen}).Find(&contests)
	for _, c := range contests {
		newStatus := computeNextStatus(&c, now)
		if newStatus != "" && newStatus != c.Status {
			db.DB.WithContext(ctx).Model(&JudgeContest{}).Where("id = ?", c.ID).Update("status", newStatus)
		}
	}
}

func computeNextStatus(c *JudgeContest, now time.Time) string {
	switch c.Status {
	case ContestStatusPending:
		if now.After(c.StartTime) || now.Equal(c.StartTime) {
			return ContestStatusRunning
		}
	case ContestStatusRunning:
		if c.FreezeTime != nil && (now.After(*c.FreezeTime) || now.Equal(*c.FreezeTime)) {
			return ContestStatusFrozen
		}
		if now.After(c.EndTime) || now.Equal(c.EndTime) {
			return ContestStatusEnded
		}
	case ContestStatusFrozen:
		if now.After(c.EndTime) || now.Equal(c.EndTime) {
			return ContestStatusEnded
		}
	}
	return ""
}

// ValidateContestAccess checks if a user can access a contest for submission.
func ValidateContestAccess(ctx context.Context, contestID, userID string) error {
	var contest JudgeContest
	if err := db.DB.WithContext(ctx).First(&contest, "id = ?", contestID).Error; err != nil {
		return exception.NewBusinessError("比赛不存在", 404)
	}
	if contest.Status != ContestStatusRunning && contest.Status != ContestStatusFrozen {
		return exception.NewBusinessError("比赛未在进行中", 400)
	}
	var count int64
	db.DB.WithContext(ctx).Model(&JudgeContestParticipant{}).Where("contest_id = ? AND user_id = ?", contestID, userID).Count(&count)
	if count == 0 {
		return exception.NewBusinessError("未报名该比赛", 400)
	}
	if contest.MaxAttempts > 0 {
		var subCount int64
		db.DB.WithContext(ctx).Model(&JudgeSubmissionInContest{}).Where("contest_id = ? AND user_id = ?", contestID, userID).Count(&subCount)
		if subCount >= int64(contest.MaxAttempts) {
			return exception.NewBusinessError("已达到最大提交次数限制", 400)
		}
	}
	return nil
}

func Page(c *gin.Context, param *ContestPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	query := db.DB.WithContext(ctx).Model(&JudgeContest{})
	if param.Title != "" {
		query = query.Where("title LIKE ?", "%"+param.Title+"%")
	}
	if param.Mode != "" {
		query = query.Where("mode = ?", param.Mode)
	}
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []JudgeContest
	offset := (param.Current - 1) * param.Size
	query.Order("created_at DESC").Limit(param.Size).Offset(offset).Find(&records)

	vos := make([]*ContestVO, 0, len(records))
	for _, r := range records {
		vo := entToVO(&r)
		vo.ParticipantCount = CountContestParticipants(ctx, r.ID)
		vos = append(vos, vo)
	}
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *ContestVO {
	ctx := context.Background()
	var entity JudgeContest
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询比赛详情失败: "+err.Error(), 500))
	}
	vo := entToVO(&entity)

	var problems []JudgeContestProblem
	db.DB.WithContext(ctx).Where("contest_id = ?", id).Order("sort_order ASC").Find(&problems)
	for _, p := range problems {
		vo.Problems = append(vo.Problems, ContestProblemVO{
			ID: p.ID, ProblemID: p.ProblemID, SortOrder: p.SortOrder,
			Label: p.Label, TimeLimitMs: p.TimeLimitMs, MemoryLimitKb: p.MemoryLimitKb,
			Score: p.Score, IsPretestOnly: p.IsPretestOnly,
		})
	}

	// Load participant count
	vo.ParticipantCount = CountContestParticipants(ctx, id)

	// Load announcements (pinned first, then by time)
	var rawAnnouncements []struct {
		ID        string
		Title     string
		Content   string
		Pinned    bool
		CreatedAt *time.Time
	}
	db.DB.WithContext(ctx).Table("judge_contest_announcement").
		Select("id, title, content, pinned, created_at").
		Where("contest_id = ?", id).
		Order("pinned DESC, created_at DESC").
		Limit(20).
		Find(&rawAnnouncements)
	for _, a := range rawAnnouncements {
		voPtr := a
		var createdAt *string
		if voPtr.CreatedAt != nil {
			s := voPtr.CreatedAt.Format("2006-01-02 15:04:05")
			createdAt = &s
		}
		vo.Announcements = append(vo.Announcements, ContestAnnouncementVO{
			ID:        voPtr.ID,
			Title:     voPtr.Title,
			Content:   voPtr.Content,
			Pinned:    voPtr.Pinned,
			CreatedAt: createdAt,
		})
	}

	return vo
}

func Create(c *gin.Context, param *ContestCreateParam, userID string) {
	ctx := context.Background()
	if param.StartTime.After(param.EndTime) {
		panic(exception.NewBusinessError("开始时间不能晚于结束时间", 400))
	}
	now := time.Now()
	entity := JudgeContest{
		ID: utils.GenerateID(), Title: param.Title, Description: param.Description,
		Mode: param.Mode, StartTime: param.StartTime, EndTime: param.EndTime,
		FreezeTime: param.FreezeTime, UnfreezeTime: param.UnfreezeTime,
		Duration: param.Duration, ShowRank: param.ShowRank, ShowAnswer: param.ShowAnswer,
		MaxAttempts: param.MaxAttempts, PenaltyDecay: param.PenaltyDecay,
		LateSubmitPenalty: param.LateSubmitPenalty,
		Status: ContestStatusPending, CreatedAt: &now, UpdatedAt: &now,
	}
	if userID != "" {
		entity.CreatedBy = &userID
		entity.UpdatedBy = &userID
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("创建比赛失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, param *ContestModifyParam, userID string) {
	ctx := context.Background()
	if param.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}
	if param.StartTime.After(param.EndTime) {
		panic(exception.NewBusinessError("开始时间不能晚于结束时间", 400))
	}
	var entity JudgeContest
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", param.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("比赛不存在", 404))
		}
		panic(exception.NewBusinessError("查询比赛失败: "+err.Error(), 500))
	}
	now := time.Now()
	updates := map[string]interface{}{
		"title": param.Title, "description": param.Description, "mode": param.Mode,
		"start_time": param.StartTime, "end_time": param.EndTime,
		"freeze_time": param.FreezeTime, "unfreeze_time": param.UnfreezeTime,
		"duration": param.Duration, "show_rank": param.ShowRank,
		"show_answer": param.ShowAnswer, "max_attempts": param.MaxAttempts,
		"penalty_decay": param.PenaltyDecay, "late_submit_penalty": param.LateSubmitPenalty,
		"updated_at": now,
	}
	if userID != "" {
		updates["updated_by"] = userID
	}
	if err := db.DB.WithContext(ctx).Model(&JudgeContest{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑比赛失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	db.DB.WithContext(context.Background()).Where("id IN ?", ids).Delete(&JudgeContest{})
}

func Register(c *gin.Context, contestID, userID string) {
	ctx := context.Background()
	var contest JudgeContest
	if err := db.DB.WithContext(ctx).First(&contest, "id = ?", contestID).Error; err != nil {
		panic(exception.NewBusinessError("比赛不存在", 404))
	}
	if contest.Status != ContestStatusPending && contest.Status != ContestStatusRunning {
		panic(exception.NewBusinessError("比赛已结束，无法报名", 400))
	}
	var count int64
	db.DB.WithContext(ctx).Model(&JudgeContestParticipant{}).Where("contest_id = ? AND user_id = ?", contestID, userID).Count(&count)
	if count > 0 {
		panic(exception.NewBusinessError("已报名该比赛", 400))
	}
	now := time.Now()
	participant := JudgeContestParticipant{
		ID: utils.GenerateID(), ContestID: contestID, UserID: userID,
		StartTime: &now, Status: "NORMAL",
	}
	if err := db.DB.WithContext(ctx).Create(&participant).Error; err != nil {
		if strings.Contains(err.Error(), "Duplicate entry") || strings.Contains(err.Error(), "1062") {
			panic(exception.NewBusinessError("已报名该比赛", 400))
		}
		panic(exception.NewBusinessError("注册比赛失败: "+err.Error(), 500))
	}
}

func Unregister(c *gin.Context, contestID, userID string) {
	ctx := context.Background()
	var contest JudgeContest
	if err := db.DB.WithContext(ctx).First(&contest, "id = ?", contestID).Error; err != nil {
		panic(exception.NewBusinessError("比赛不存在", 404))
	}
	if contest.Status != ContestStatusPending {
		panic(exception.NewBusinessError("比赛已开始或结束，无法取消报名", 400))
	}
	db.DB.WithContext(ctx).Where("contest_id = ? AND user_id = ?", contestID, userID).Delete(&JudgeContestParticipant{})
}

func SyncProblems(c *gin.Context, contestID string, problems []ContestProblemVO) {
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Delete(&JudgeContestProblem{})
	for i, p := range problems {
		entity := JudgeContestProblem{
			ID: utils.GenerateID(), ContestID: contestID, ProblemID: p.ProblemID,
			SortOrder: i, Label: p.Label, TimeLimitMs: p.TimeLimitMs,
			MemoryLimitKb: p.MemoryLimitKb, Score: p.Score, IsPretestOnly: p.IsPretestOnly,
		}
		db.DB.WithContext(ctx).Create(&entity)
	}
}

// GetContestProblems returns problems for a contest with their labels.
func GetContestProblems(ctx context.Context, contestID string) []ContestProblemVO {
	var problems []JudgeContestProblem
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Order("sort_order ASC").Find(&problems)
	vos := make([]ContestProblemVO, 0, len(problems))
	for _, p := range problems {
		vos = append(vos, ContestProblemVO{
			ID: p.ID, ProblemID: p.ProblemID, SortOrder: p.SortOrder,
			Label: p.Label, TimeLimitMs: p.TimeLimitMs, MemoryLimitKb: p.MemoryLimitKb,
			Score: p.Score, IsPretestOnly: p.IsPretestOnly,
		})
	}
	return vos
}

// IsContestProblem checks if a problem belongs to a contest.
func IsContestProblem(ctx context.Context, contestID, problemID string) bool {
	var count int64
	db.DB.WithContext(ctx).Model(&JudgeContestProblem{}).Where("contest_id = ? AND problem_id = ?", contestID, problemID).Count(&count)
	return count > 0
}

// CountContestParticipants returns the number of registered participants.
func CountContestParticipants(ctx context.Context, contestID string) int64 {
	var count int64
	db.DB.WithContext(ctx).Model(&JudgeContestParticipant{}).Where("contest_id = ?", contestID).Count(&count)
	return count
}

// ===== Contest-level submission tracking =====

// JudgeSubmissionInContest records a submission made in a contest context.
type JudgeSubmissionInContest struct {
	ID           string    `gorm:"primaryKey;size:32" json:"id"`
	SubmissionID string    `gorm:"size:32;uniqueIndex:idx_submission" json:"submission_id"`
	ContestID    string    `gorm:"size:32;index:idx_contest_sub;uniqueIndex:idx_submission" json:"contest_id"`
	UserID       string    `gorm:"size:32;index:idx_contest_sub" json:"user_id"`
	ProblemID    string    `gorm:"size:32" json:"problem_id"`
	ProblemLabel string    `gorm:"size:8" json:"problem_label"`
	Language     string    `gorm:"size:32" json:"language"`
	Status       string    `gorm:"size:32" json:"status"`
	Score        int       `json:"score"`
	TimeUsed     int64     `json:"time_used"`
	MemoryUsed   int64     `json:"memory_used"`
	CreatedAt    time.Time `json:"created_at"`
}

func (JudgeSubmissionInContest) TableName() string { return "judge_contest_submission" }

// RecordContestSubmission syncs a submission to the contest_submission table.
func RecordContestSubmission(ctx context.Context, contestID, userID, problemID, label string, subID, language string) {
	entity := JudgeSubmissionInContest{
		ID:           utils.GenerateID(),
		SubmissionID: subID,
		ContestID:    contestID,
		UserID:       userID,
		ProblemID:    problemID,
		ProblemLabel: label,
		Language:     language,
		Status:       "PENDING",
		Score:        0,
		TimeUsed:     0,
		MemoryUsed:   0,
		CreatedAt:    time.Now(),
	}
	db.DB.WithContext(ctx).Create(&entity)
}

// UpdateContestSubmissionStatus updates the status of a contest submission.
func UpdateContestSubmissionStatus(ctx context.Context, submissionID, status string, score int, timeUsed, memoryUsed int64) {
	db.DB.WithContext(ctx).Model(&JudgeSubmissionInContest{}).
		Where("submission_id = ?", submissionID).
		Updates(map[string]interface{}{
			"status": status, "score": score, "time_used": timeUsed, "memory_used": memoryUsed,
		})
}

// GetMyContestSubmissions returns the current user's submissions in a contest.
func GetMyContestSubmissions(ctx context.Context, contestID, userID string, limit int) []JudgeSubmissionInContest {
	if limit <= 0 {
		limit = 10
	}
	var subs []JudgeSubmissionInContest
	db.DB.WithContext(ctx).Where("contest_id = ? AND user_id = ?", contestID, userID).
		Order("created_at DESC").Limit(limit).Find(&subs)
	return subs
}

// ===== VO builders =====

func entToVO(entity *JudgeContest) *ContestVO {
	vo := &ContestVO{
		ID: entity.ID, Title: entity.Title, Description: entity.Description,
		Mode: entity.Mode, Duration: entity.Duration, Status: entity.Status,
		ShowRank: entity.ShowRank, ShowAnswer: entity.ShowAnswer,
		MaxAttempts: entity.MaxAttempts, PenaltyDecay: entity.PenaltyDecay,
		LateSubmitPenalty: entity.LateSubmitPenalty,
		StartTime: entity.StartTime.Format("2006-01-02 15:04:05"),
		EndTime:   entity.EndTime.Format("2006-01-02 15:04:05"),
	}
	if entity.FreezeTime != nil {
		s := entity.FreezeTime.Format("2006-01-02 15:04:05")
		vo.FreezeTime = &s
	}
	if entity.UnfreezeTime != nil {
		s := entity.UnfreezeTime.Format("2006-01-02 15:04:05")
		vo.UnfreezeTime = &s
	}
	if entity.CreatedAt != nil {
		s := entity.CreatedAt.Format("2006-01-02 15:04:05")
		vo.CreatedAt = &s
	}
	if entity.UpdatedAt != nil {
		s := entity.UpdatedAt.Format("2006-01-02 15:04:05")
		vo.UpdatedAt = &s
	}
	return vo
}
