package testcase

import "time"

// JudgeTestcase 测试用例实体
type JudgeTestcase struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	ProblemID     string     `gorm:"size:32;index" json:"problem_id"`
	InputPath     string     `gorm:"size:512;default:''" json:"input_path"`    // 输入文件存储路径
	OutputPath    string     `gorm:"size:512;default:''" json:"output_path"`   // 输出文件存储路径
	FileSize      int64      `gorm:"default:0" json:"file_size"`               // 文件总大小（字节）
	Input         string     `gorm:"type:longtext" json:"input"`               // 临时兼容：迁移完成后移除
	Output        string     `gorm:"type:longtext" json:"output"`              // 临时兼容：迁移完成后移除
	Order         int        `gorm:"default:0" json:"order"`
	IsSample      bool       `gorm:"default:false" json:"is_sample"`
	Score         int        `gorm:"default:100" json:"score"`
	GroupID       string     `gorm:"size:32;default:'';index" json:"group_id"` // 子任务分组ID: 同组全部通过才给分（子任务捆绑）
	StrictCompare bool       `gorm:"default:false" json:"strict_compare"`      // 是否严格逐字节比对（覆盖题目级设置）
	CreatedAt     *time.Time `json:"created_at"`
	CreatedBy     *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     *time.Time `json:"updated_at"`
	UpdatedBy     *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeTestcase) TableName() string { return "judge_testcase" }

// JudgeTestcaseGroup 测试用例分组（子任务）视图
type JudgeTestcaseGroup struct {
	GroupID   string           `json:"group_id"`
	Score     int              `json:"score"`     // 该子任务总分
	Testcases []JudgeTestcase  `json:"testcases"`
}

// GroupBySubtask 将测试用例按 GroupID 分组
func GroupBySubtask(testcases []JudgeTestcase) []JudgeTestcaseGroup {
	groupMap := make(map[string]*JudgeTestcaseGroup)
	var groups []JudgeTestcaseGroup

	for _, tc := range testcases {
		gid := tc.GroupID
		if gid == "" {
			gid = tc.ID
		}
		if _, ok := groupMap[gid]; !ok {
			groupMap[gid] = &JudgeTestcaseGroup{
				GroupID: gid,
			}
		}
		groupMap[gid].Testcases = append(groupMap[gid].Testcases, tc)
		groupMap[gid].Score += tc.Score
	}

	seen := make(map[string]bool)
	for _, tc := range testcases {
		gid := tc.GroupID
		if gid == "" {
			gid = tc.ID
		}
		if seen[gid] {
			continue
		}
		seen[gid] = true
		groups = append(groups, *groupMap[gid])
	}

	return groups
}
