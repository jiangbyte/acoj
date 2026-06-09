package testcase

import "hei-gin/sdk/pojo"

type TestcaseVO struct {
	ID            string `json:"id"`
	ProblemID     string `json:"problem_id"`
	Input         string `json:"input"`
	Output        string `json:"output"`
	Order         int    `json:"order"`
	IsSample      bool   `json:"is_sample"`
	Score         int    `json:"score"`
	GroupID       string `json:"group_id"`
	StrictCompare bool   `json:"strict_compare"`
	CreatedAt     string `json:"created_at"`
}

type TestcaseListParam struct {
	ProblemID string `json:"problem_id" form:"problem_id" binding:"required"`
}

type TestcaseCreateParam struct {
	ProblemID     string `json:"problem_id" binding:"required"`
	Input         string `json:"input"`
	Output        string `json:"output"`
	Order         int    `json:"order"`
	IsSample      bool   `json:"is_sample"`
	Score         int    `json:"score"`
	GroupID       string `json:"group_id"`
	StrictCompare bool   `json:"strict_compare"`
}

type TestcaseModifyParam struct {
	ID            string `json:"id" binding:"required"`
	Input         string `json:"input"`
	Output        string `json:"output"`
	Order         *int   `json:"order"`
	IsSample      *bool  `json:"is_sample"`
	Score         *int   `json:"score"`
	GroupID       string `json:"group_id"`
	StrictCompare *bool  `json:"strict_compare"`
}

type TestcaseRemoveParam pojo.IdsParam

type TestcaseBatchCreateParam struct {
	ProblemID string                `json:"problem_id" binding:"required"`
	Cases     []TestcaseCreateParam `json:"cases" binding:"required"`
}

// TestCaseResult 单个测试用例的判题结果（引擎输出）
type TestCaseResult struct {
	TestCaseID string `json:"testcase_id"`
	GroupID    string `json:"group_id"`
	Order      int    `json:"order"`
	Status     string `json:"status"`
	Score      int    `json:"score"`
	TimeUsed   int64  `json:"time_used"`
	MemoryUsed int64  `json:"memory_used"`
	Stderr     string `json:"stderr,omitempty"`
	Error      string `json:"error,omitempty"`
}

// TestCaseGroupResult 子任务分组判题结果
type TestCaseGroupResult struct {
	GroupID   string           `json:"group_id"`
	Score     int              `json:"score"`
	Testcases []TestCaseResult `json:"testcases"`
}

// GroupBySubtaskForResult 将判题结果按 GroupID 分组
func GroupBySubtaskForResult(details []TestCaseResult) []TestCaseGroupResult {
	groupMap := make(map[string]*TestCaseGroupResult)
	var groups []TestCaseGroupResult

	for _, d := range details {
		gid := d.GroupID
		if gid == "" {
			gid = d.TestCaseID
		}
		if _, ok := groupMap[gid]; !ok {
			groupMap[gid] = &TestCaseGroupResult{GroupID: gid}
		}
		groupMap[gid].Testcases = append(groupMap[gid].Testcases, d)
	}

	seen := make(map[string]bool)
	for _, d := range details {
		gid := d.GroupID
		if gid == "" {
			gid = d.TestCaseID
		}
		if seen[gid] {
			continue
		}
		seen[gid] = true
		grp := groupMap[gid]
		// 计算该组总分
		for _, tc := range grp.Testcases {
			grp.Score += tc.Score
		}
		groups = append(groups, *grp)
	}

	return groups
}
