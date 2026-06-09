package contest

import "time"

// JudgeContest 竞赛实体
type JudgeContest struct {
	ID              string     `gorm:"primaryKey;size:32" json:"id"`
	Title           string     `gorm:"size:255;index" json:"title"`
	Description     string     `gorm:"type:text" json:"description"`
	Type            string     `gorm:"size:16;default:ACM" json:"type"`          // ACM / OI / IOI / CF (Codeforces) / AT (AtCoder)
	Rule            string     `gorm:"size:16;default:PRIVATE" json:"rule"`      // PUBLIC / PRIVATE / PASSWORD
	Password        string     `gorm:"size:255" json:"password"`
	StartTime       *time.Time `json:"start_time"`
	EndTime         *time.Time `json:"end_time"`
	Status          string     `gorm:"size:16;default:PENDING" json:"status"`    // PENDING / RUNNING / ENDED / CANCELED

	// 罚时配置
	PenaltyPerFail  int        `gorm:"default:20" json:"penalty_per_fail"`       // 每次错误提交罚时（分钟），仅 ACM/CF/AT 模式生效
	PenaltyMinute   int        `gorm:"default:5" json:"penalty_minute"`          // AtCoder 模式每分钟罚时

	// 功能开关
	AllowHack       bool       `gorm:"default:false" json:"allow_hack"`          // 是否启用 Hack 环节（CF 模式）
	FreezeAt        *time.Time `json:"freeze_at"`                                // 排行榜冻结时间（CF 模式）
	ScoreDecay      bool       `gorm:"default:false" json:"score_decay"`          // 是否启用分值衰减（CF 模式）
	DecayStartMinute int       `gorm:"default:0" json:"decay_start_minute"`      // 分值衰减开始时间（分钟）

	// 团队赛
	IsTeam          bool       `gorm:"default:false" json:"is_team"`              // 是否团队赛
	TeamMaxSize     int        `gorm:"default:3" json:"team_max_size"`            // 团队人数上限

	// 可见性
	StandingsVisible bool     `gorm:"default:true" json:"standings_visible"`     // 排行榜是否对选手可见
	AllowPractice    bool     `gorm:"default:false" json:"allow_practice"`       // 比赛结束后是否允许补题（自动将提交标记为非比赛）

	CreatedBy       *string    `gorm:"size:32" json:"created_by"`
	CreatedAt       *time.Time `json:"created_at"`
	UpdatedBy       *string    `gorm:"size:32" json:"updated_by"`
	UpdatedAt       *time.Time `json:"updated_at"`
}

func (JudgeContest) TableName() string { return "judge_contest" }

// RelContestProblem 竞赛-题目关联
type RelContestProblem struct {
	ID           string `gorm:"primaryKey;size:32" json:"id"`
	ContestID    string `gorm:"size:32;uniqueIndex:idx_contest_problem" json:"contest_id"`
	ProblemID    string `gorm:"size:32;uniqueIndex:idx_contest_problem;index" json:"problem_id"`
	Sort         int    `gorm:"default:0" json:"sort"`
	Score        int    `gorm:"default:100" json:"score"`
	// CF 模式动态分值
	BaseScore    int    `gorm:"default:500" json:"base_score"`     // 基础分值
	DecayRate    int    `gorm:"default:5" json:"decay_rate"`      // 每分钟衰减分值
}

func (RelContestProblem) TableName() string { return "rel_contest_problem" }

// RelContestUser 竞赛-用户关联（报名）
type RelContestUser struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID string     `gorm:"size:32;uniqueIndex:idx_contest_user" json:"contest_id"`
	UserID    string     `gorm:"size:32;uniqueIndex:idx_contest_user;index" json:"user_id"`
	TeamID    string     `gorm:"size:32;default:'';index" json:"team_id"` // 团队ID（团队赛时使用）
	CreatedAt *time.Time `json:"created_at"`
}

func (RelContestUser) TableName() string { return "rel_contest_user" }

// JudgeTeam 团队实体（团队赛）
type JudgeTeam struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID string     `gorm:"size:32;index" json:"contest_id"`
	Name      string     `gorm:"size:128" json:"name"`
	Avatar    string     `gorm:"size:512" json:"avatar"`
	Description string   `gorm:"type:text" json:"description"`
	LeaderID  string     `gorm:"size:32" json:"leader_id"`
	CreatedAt *time.Time `json:"created_at"`
	CreatedBy *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt *time.Time `json:"updated_at"`
	UpdatedBy *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeTeam) TableName() string { return "judge_team" }
