package problem

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

func Page(c *gin.Context, param *ProblemPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	query := db.DB.WithContext(ctx).Model(&JudgeProblem{})
	if param.Title != "" {
		query = query.Where("title LIKE ?", "%"+param.Title+"%")
	}
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []JudgeProblem
	offset := (param.Current - 1) * param.Size
	query.Order("created_at DESC").Limit(param.Size).Offset(offset).Find(&records)

	vos := make([]*ProblemVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(&r))
	}
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *ProblemVO {
	ctx := context.Background()
	var entity JudgeProblem
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询题目详情失败: "+err.Error(), 500))
	}
	vo := entToVO(&entity)

	// Load languages
	var languages []JudgeProblemLanguage
	db.DB.WithContext(ctx).Where("problem_id = ?", id).Find(&languages)
	for _, l := range languages {
		vo.Languages = append(vo.Languages, LanguageVO{
			ID:            l.ID,
			Language:      l.Language,
			Template:      l.Template,
			TimeLimitMs:   l.TimeLimitMs,
			MemoryLimitKb: l.MemoryLimitKb,
		})
	}

	// Load samples
	var samples []JudgeProblemSample
	db.DB.WithContext(ctx).Where("problem_id = ?", id).Order("sort_order ASC").Find(&samples)
	for _, s := range samples {
		vo.Samples = append(vo.Samples, SampleVO{
			ID:        s.ID,
			SortOrder: s.SortOrder,
			Input:     s.Input,
			Output:    s.Output,
		})
	}

	// Load test cases
	var testcases []JudgeProblemTestCase
	db.DB.WithContext(ctx).Where("problem_id = ?", id).Order("sort_order ASC").Find(&testcases)
	for _, tc := range testcases {
		vo.TestCases = append(vo.TestCases, TestCaseVO{
			ID:          tc.ID,
			SubtaskID:   tc.SubtaskID,
			SortOrder:   tc.SortOrder,
			Input:       tc.Input,
			Output:      tc.Output,
			TimeLimitMs: tc.TimeLimitMs,
			MemLimitKb:  tc.MemLimitKb,
			Score:       tc.Score,
		})
	}

	// Load subtasks
	var subtasks []JudgeProblemSubtask
	db.DB.WithContext(ctx).Where("problem_id = ?", id).Order("sort_order ASC").Find(&subtasks)
	for _, st := range subtasks {
		vo.Subtasks = append(vo.Subtasks, SubtaskVO{
			ID:          st.ID,
			SortOrder:   st.SortOrder,
			Score:       st.Score,
			JudgeMethod: st.JudgeMethod,
		})
	}

	return vo
}

func Create(c *gin.Context, param *ProblemCreateParam, userID string) {
	ctx := context.Background()
	now := time.Now()

	entity := JudgeProblem{
		ID:            utils.GenerateID(),
		Title:         param.Title,
		Description:   param.Description,
		InputDesc:     param.InputDesc,
		OutputDesc:    param.OutputDesc,
		Hint:          param.Hint,
		JudgeMethod:   param.JudgeMethod,
		TimeLimitMs:   param.TimeLimitMs,
		MemoryLimitKb: param.MemoryLimitKb,
		SpjSource:     param.SpjSource,
		SpjLanguage:   param.SpjLanguage,
		InteractorSrc: param.InteractorSrc,
		Status:        "ENABLED",
		CreatedAt:     &now,
		UpdatedAt:     &now,
	}
	if userID != "" {
		entity.CreatedBy = &userID
		entity.UpdatedBy = &userID
	}

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("创建题目失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, param *ProblemModifyParam, userID string) {
	ctx := context.Background()
	if param.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	var entity JudgeProblem
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", param.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("题目不存在", 404))
		}
		panic(exception.NewBusinessError("查询题目失败: "+err.Error(), 500))
	}

	now := time.Now()
	updates := map[string]interface{}{
		"title":          param.Title,
		"description":    param.Description,
		"input_desc":     param.InputDesc,
		"output_desc":    param.OutputDesc,
		"hint":           param.Hint,
		"judge_method":   param.JudgeMethod,
		"time_limit_ms":  param.TimeLimitMs,
		"memory_limit_kb": param.MemoryLimitKb,
		"spj_source":     param.SpjSource,
		"spj_language":   param.SpjLanguage,
		"interactor_src": param.InteractorSrc,
		"status":         param.Status,
		"updated_at":     now,
	}
	if userID != "" {
		updates["updated_by"] = userID
	}

	if err := db.DB.WithContext(ctx).Model(&JudgeProblem{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑题目失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&JudgeProblem{})
}

// ===== Language Management =====

func ListLanguages(c *gin.Context, problemID string) gin.H {
	ctx := context.Background()
	var languages []JudgeProblemLanguage
	db.DB.WithContext(ctx).Where("problem_id = ?", problemID).Find(&languages)
	return result.Success(c, languages)
}

// ===== Testcase Management =====

func ListTestcases(c *gin.Context, problemID string) gin.H {
	ctx := context.Background()
	var testcases []JudgeProblemTestCase
	db.DB.WithContext(ctx).Where("problem_id = ?", problemID).Order("sort_order ASC").Find(&testcases)
	return result.Success(c, testcases)
}

// ===== Subtask Management =====

func ListSubtasks(c *gin.Context, problemID string) gin.H {
	ctx := context.Background()
	var subtasks []JudgeProblemSubtask
	db.DB.WithContext(ctx).Where("problem_id = ?", problemID).Order("sort_order ASC").Find(&subtasks)
	return result.Success(c, subtasks)
}

func entToVO(entity *JudgeProblem) *ProblemVO {
	vo := &ProblemVO{
		ID:            entity.ID,
		Title:         entity.Title,
		Description:   entity.Description,
		InputDesc:     entity.InputDesc,
		OutputDesc:    entity.OutputDesc,
		Hint:          entity.Hint,
		JudgeMethod:   entity.JudgeMethod,
		TimeLimitMs:   entity.TimeLimitMs,
		MemoryLimitKb: entity.MemoryLimitKb,
		SpjSource:     entity.SpjSource,
		SpjLanguage:   entity.SpjLanguage,
		InteractorSrc: entity.InteractorSrc,
		Status:        entity.Status,
	}
	if entity.CreatedAt != nil {
		s := entity.CreatedAt.Format("2006-01-02 15:04:05")
		vo.CreatedAt = &s
	}
	if entity.CreatedBy != nil {
		vo.CreatedBy = entity.CreatedBy
	}
	if entity.UpdatedAt != nil {
		s := entity.UpdatedAt.Format("2006-01-02 15:04:05")
		vo.UpdatedAt = &s
	}
	if entity.UpdatedBy != nil {
		vo.UpdatedBy = entity.UpdatedBy
	}
	return vo
}

// ===== Testcase Management =====

func TestcaseAdd(c *gin.Context, param *TestcaseAddParam) {
	ctx := context.Background()
	entity := JudgeProblemTestCase{
		ID:          utils.GenerateID(),
		ProblemID:   param.ProblemID,
		SubtaskID:   param.SubtaskID,
		SortOrder:   param.SortOrder,
		Input:       param.Input,
		Output:      param.Output,
		TimeLimitMs: param.TimeLimitMs,
		MemLimitKb:  param.MemLimitKb,
		Score:       param.Score,
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加测试点失败: "+err.Error(), 500))
	}
}

func TestcaseModify(c *gin.Context, param *TestcaseModifyParam) {
	ctx := context.Background()
	updates := map[string]interface{}{
		"subtask_id":   param.SubtaskID,
		"sort_order":   param.SortOrder,
		"input":        param.Input,
		"output":       param.Output,
		"time_limit_ms": param.TimeLimitMs,
		"mem_limit_kb":  param.MemLimitKb,
		"score":        param.Score,
	}
	result := db.DB.WithContext(ctx).Model(&JudgeProblemTestCase{}).
		Where("id = ?", param.ID).Updates(updates)
	if result.RowsAffected == 0 {
		panic(exception.NewBusinessError("测试点不存在", 404))
	}
	if result.Error != nil {
		panic(exception.NewBusinessError("修改测试点失败: "+result.Error.Error(), 500))
	}
}

func TestcaseRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&JudgeProblemTestCase{})
}

// ===== Language Management =====

func LanguageSync(c *gin.Context, param *LanguageSyncParam) {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Begin()

	// Remove existing language configs for this problem
	if err := tx.Where("problem_id = ?", param.ProblemID).Delete(&JudgeProblemLanguage{}).Error; err != nil {
		tx.Rollback()
		panic(exception.NewBusinessError("同步语言配置失败: "+err.Error(), 500))
	}

	// Insert new language configs
	for _, l := range param.Languages {
		entity := JudgeProblemLanguage{
			ID:            utils.GenerateID(),
			ProblemID:     param.ProblemID,
			Language:      l.Language,
			Template:      l.Template,
			TimeLimitMs:   l.TimeLimitMs,
			MemoryLimitKb: l.MemoryLimitKb,
		}
		if err := tx.Create(&entity).Error; err != nil {
			tx.Rollback()
			panic(exception.NewBusinessError("同步语言配置失败: "+err.Error(), 500))
		}
	}

	if err := tx.Commit().Error; err != nil {
		panic(exception.NewBusinessError("同步语言配置失败: "+err.Error(), 500))
	}
}

// ===== Sample Management =====

func SampleAdd(c *gin.Context, param *SampleAddParam) {
	ctx := context.Background()
	entity := JudgeProblemSample{
		ID:        utils.GenerateID(),
		ProblemID: param.ProblemID,
		SortOrder: param.SortOrder,
		Input:     param.Input,
		Output:    param.Output,
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加样例失败: "+err.Error(), 500))
	}
}

func SampleRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&JudgeProblemSample{})
}

func SubtaskAdd(c *gin.Context, param *SubtaskAddParam) {
	ctx := context.Background()
	entity := JudgeProblemSubtask{
		ID:          utils.GenerateID(),
		ProblemID:   param.ProblemID,
		SortOrder:   param.SortOrder,
		Score:       param.Score,
		JudgeMethod: param.JudgeMethod,
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加子任务失败: "+err.Error(), 500))
	}
}

func SubtaskModify(c *gin.Context, param *SubtaskModifyParam) {
	ctx := context.Background()
	updates := map[string]interface{}{
		"sort_order":   param.SortOrder,
		"score":        param.Score,
		"judge_method": param.JudgeMethod,
	}
	if err := db.DB.WithContext(ctx).Model(&JudgeProblemSubtask{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("修改子任务失败: "+err.Error(), 500))
	}
}

func SubtaskRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&JudgeProblemSubtask{})
}

func DepAdd(c *gin.Context, param *DepAddParam) {
	ctx := context.Background()
	entity := JudgeProblemSubtaskDep{
		ID:                 utils.GenerateID(),
		SubtaskID:          param.SubtaskID,
		DependsOnSubtaskID: param.DependsOnSubtaskID,
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加依赖失败: "+err.Error(), 500))
	}
}

func DepRemove(c *gin.Context, id string) {
	ctx := context.Background()
	db.DB.WithContext(ctx).Delete(&JudgeProblemSubtaskDep{}, "id = ?", id)
}
