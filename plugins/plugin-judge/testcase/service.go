package testcase

import (
	"context"
	"fmt"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

// ListByProblemService 查询题目的测试用例列表（不含文件内容）
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

// GetContentService 获取单个测试用例的文件内容
func GetContentService(c *gin.Context, id, field string) (*TestcaseContentVO, error) {
	ctx := context.Background()
	var tc JudgeTestcase
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&tc).Error; err != nil {
		return nil, fmt.Errorf("测试用例不存在: %w", err)
	}
	vo := &TestcaseContentVO{ID: tc.ID}

	switch field {
	case "input":
		data, err := GetInput(tc.ProblemID, tc.ID)
		if err != nil {
			return nil, err
		}
		vo.Input = string(data)
	case "output":
		data, err := GetOutput(tc.ProblemID, tc.ID)
		if err != nil {
			return nil, err
		}
		vo.Output = string(data)
	default:
		data, _ := GetInput(tc.ProblemID, tc.ID)
		vo.Input = string(data)
		data, _ = GetOutput(tc.ProblemID, tc.ID)
		vo.Output = string(data)
	}
	return vo, nil
}

// saveTestcaseFiles 将数据保存到文件存储并返回路径
func saveTestcaseFiles(problemID, testcaseID, input, output string) (inputPath, outputPath string, fileSize int64, err error) {
	var total int64

	if input != "" {
		path, e := SaveInput(problemID, testcaseID, []byte(input))
		if e != nil {
			return "", "", 0, fmt.Errorf("save input: %w", e)
		}
		inputPath = path
		total += int64(len(input))
	}
	if output != "" {
		path, e := SaveOutput(problemID, testcaseID, []byte(output))
		if e != nil {
			return "", "", 0, fmt.Errorf("save output: %w", e)
		}
		outputPath = path
		total += int64(len(output))
	}
	return inputPath, outputPath, total, nil
}

// CreateService 创建测试用例
func CreateService(c *gin.Context, param *TestcaseCreateParam) error {
	ctx := context.Background()
	now := time.Now()

	tcID := utils.GenerateID()
	inputPath, outputPath, fileSize, err := saveTestcaseFiles(param.ProblemID, tcID, param.Input, param.Output)
	if err != nil {
		return err
	}

	tc := JudgeTestcase{
		ID:            tcID,
		ProblemID:     param.ProblemID,
		InputPath:     inputPath,
		OutputPath:    outputPath,
		FileSize:      fileSize,
		Order:         param.Order,
		IsSample:      param.IsSample,
		Score:         param.Score,
		GroupID:       param.GroupID,
		StrictCompare: param.StrictCompare,
		CreatedAt:     &now,
		UpdatedAt:     &now,
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
		tcID := utils.GenerateID()
		inputPath, outputPath, fileSize, err := saveTestcaseFiles(param.ProblemID, tcID, cp.Input, cp.Output)
		if err != nil {
			return fmt.Errorf("case %d: %w", i, err)
		}
		cases[i] = JudgeTestcase{
			ID:            tcID,
			ProblemID:     param.ProblemID,
			InputPath:     inputPath,
			OutputPath:    outputPath,
			FileSize:      fileSize,
			Order:         cp.Order,
			IsSample:      cp.IsSample,
			Score:         score,
			GroupID:       cp.GroupID,
			StrictCompare: cp.StrictCompare,
			CreatedAt:     &now,
			UpdatedAt:     &now,
		}
	}

	return db.DB.WithContext(ctx).Create(&cases).Error
}

// ModifyService 编辑测试用例
func ModifyService(c *gin.Context, param *TestcaseModifyParam) error {
	ctx := context.Background()
	now := time.Now()

	// 先查询现有记录
	var existing JudgeTestcase
	if err := db.DB.WithContext(ctx).Where("id = ?", param.ID).First(&existing).Error; err != nil {
		return fmt.Errorf("测试用例不存在")
	}

	updates := map[string]any{}
	needFileUpdate := false

	// 处理文件更新
	if param.Input != "" || param.Output != "" {
		needFileUpdate = true
		inputData := ""
		outputData := ""
		if param.Input != "" {
			inputData = param.Input
		}
		if param.Output != "" {
			outputData = param.Output
		}
		inputPath, outputPath, fileSize, err := saveTestcaseFiles(existing.ProblemID, param.ID, inputData, outputData)
		if err != nil {
			return err
		}
		if inputPath != "" {
			updates["input_path"] = inputPath
		}
		if outputPath != "" {
			updates["output_path"] = outputPath
		}
		updates["file_size"] = fileSize
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
	if param.GroupID != "" {
		updates["group_id"] = param.GroupID
	}
	if param.StrictCompare != nil {
		updates["strict_compare"] = *param.StrictCompare
	}
	updates["updated_at"] = now

	// DB 更新
	if err := db.DB.WithContext(ctx).Model(&JudgeTestcase{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
		return err
	}

	// 文件更新成功后，删除旧文件（非关键，best-effort）
	if needFileUpdate {
		if param.Input != "" && existing.InputPath != "" {
			// 旧文件会在下次覆盖或清理任务中处理
			// 这里只失效缓存
			testcaseCache.Del(cacheKey(existing.ProblemID, param.ID, "in"))
		}
		if param.Output != "" && existing.OutputPath != "" {
			testcaseCache.Del(cacheKey(existing.ProblemID, param.ID, "out"))
		}
	}

	return nil
}

// RemoveService 删除测试用例
func RemoveService(c *gin.Context, param TestcaseRemoveParam) error {
	ctx := context.Background()

	// 先查询要删除的记录，获取文件路径
	var cases []JudgeTestcase
	if err := db.DB.WithContext(ctx).Where("id IN ?", param.IDs).Find(&cases).Error; err != nil {
		return err
	}

	// DB 删除
	if err := db.DB.WithContext(ctx).Where("id IN ?", param.IDs).Delete(&JudgeTestcase{}).Error; err != nil {
		return err
	}

	// 删除文件（best-effort）
	for _, tc := range cases {
		if tc.InputPath != "" || tc.OutputPath != "" {
			if err := DeleteTestcaseFiles(tc.ProblemID, tc.ID); err != nil {
				// 仅日志，不影响 DB 操作结果
			}
		}
	}

	return nil
}

func modelToVO(tc *JudgeTestcase) TestcaseVO {
	createdAt := ""
	if tc.CreatedAt != nil {
		createdAt = tc.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return TestcaseVO{
		ID:            tc.ID,
		ProblemID:     tc.ProblemID,
		InputPath:     tc.InputPath,
		OutputPath:    tc.OutputPath,
		FileSize:      tc.FileSize,
		Order:         tc.Order,
		IsSample:      tc.IsSample,
		Score:         tc.Score,
		GroupID:       tc.GroupID,
		StrictCompare: tc.StrictCompare,
		CreatedAt:     createdAt,
	}
}
