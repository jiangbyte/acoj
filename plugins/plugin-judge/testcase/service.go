package testcase

import (
	"context"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

// ListByProblemService 查询题目的测试用例列表
func ListByProblemService(c *gin.Context, problemID string) gin.H {
	ctx := context.Background()
	var cases []JudgeTestcase
	db.DB.WithContext(ctx).Where("problem_id = ?", problemID).Order("`order` ASC, created_at ASC").Find(&cases)

	voList := make([]TestcaseVO, len(cases))
	for i, tc := range cases {
		voList[i] = modelToVO(&tc)
	}
	return result.Success(c, voList)
}

// CreateService 创建测试用例
func CreateService(c *gin.Context, param *TestcaseCreateParam) error {
	ctx := context.Background()
	now := time.Now()

	tc := JudgeTestcase{
		ID:        utils.GenerateID(),
		ProblemID: param.ProblemID,
		Input:     param.Input,
		Output:    param.Output,
		Order:     param.Order,
		IsSample:  param.IsSample,
		Score:     param.Score,
		CreatedAt: &now,
		UpdatedAt: &now,
	}
	if tc.Score <= 0 {
		tc.Score = 100
	}

	return db.DB.WithContext(ctx).Create(&tc).Error
}

// BatchCreateService 批量创建测试用例
func BatchCreateService(c *gin.Context, param *TestcaseBatchCreateParam) error {
	ctx := context.Background()
	now := time.Now()

	cases := make([]JudgeTestcase, len(param.Cases))
	for i, cp := range param.Cases {
		score := cp.Score
		if score <= 0 {
			score = 100
		}
		cases[i] = JudgeTestcase{
			ID:        utils.GenerateID(),
			ProblemID: param.ProblemID,
			Input:     cp.Input,
			Output:    cp.Output,
			Order:     cp.Order,
			IsSample:  cp.IsSample,
			Score:     score,
			CreatedAt: &now,
			UpdatedAt: &now,
		}
	}

	return db.DB.WithContext(ctx).Create(&cases).Error
}

// ModifyService 编辑测试用例
func ModifyService(c *gin.Context, param *TestcaseModifyParam) error {
	ctx := context.Background()
	updates := map[string]any{}
	if param.Input != "" {
		updates["input"] = param.Input
	}
	if param.Output != "" {
		updates["output"] = param.Output
	}
	if param.Order != nil {
		updates["order"] = *param.Order
	}
	if param.IsSample != nil {
		updates["is_sample"] = *param.IsSample
	}
	if param.Score != nil {
		updates["score"] = *param.Score
	}
	updates["updated_at"] = time.Now()

	return db.DB.WithContext(ctx).Model(&JudgeTestcase{}).Where("id = ?", param.ID).Updates(updates).Error
}

// RemoveService 删除测试用例
func RemoveService(c *gin.Context, param TestcaseRemoveParam) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).Where("id IN ?", param.IDs).Delete(&JudgeTestcase{}).Error
}

func modelToVO(tc *JudgeTestcase) TestcaseVO {
	createdAt := ""
	if tc.CreatedAt != nil {
		createdAt = tc.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return TestcaseVO{
		ID:        tc.ID,
		ProblemID: tc.ProblemID,
		Input:     tc.Input,
		Output:    tc.Output,
		Order:     tc.Order,
		IsSample:  tc.IsSample,
		Score:     tc.Score,
		CreatedAt: createdAt,
	}
}
