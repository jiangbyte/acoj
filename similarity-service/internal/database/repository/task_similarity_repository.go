package repository

import (
	"math"
	"similarity-service/internal/database/model"

	"gorm.io/gorm"
)

type TaskSimilarityRepository interface {
	SelectSimilarityStats(taskID, problemID, setID string, isSet int) (*model.TaskReportStats, error)
	SelectSimilarityDistribution(taskID string) ([]int, error)
	SelectDegreeStatistics(taskID string, threshold float64) ([]model.CloneLevel, error)
	GetDegreeBySimilarity(similarity, threshold float64) (string, error)

	// 批量插入
	BatchCreate(tasks []*model.TaskSimilarity) error
}

type taskSimilarityRepository struct {
	db *gorm.DB
}

func NewTaskSimilarityRepository(db *gorm.DB) TaskSimilarityRepository {
	return &taskSimilarityRepository{
		db: db,
	}
}

// BatchCreate 批量插入
func (r *taskSimilarityRepository) BatchCreate(tasks []*model.TaskSimilarity) error {
	if len(tasks) == 0 {
		return nil
	}
	return r.db.Create(&tasks).Error
}

// SelectSimilarityStats 查询相似度统计
func (r *taskSimilarityRepository) SelectSimilarityStats(taskID, problemID, setID string, isSet int) (*model.TaskReportStats, error) {
	var stats model.TaskReportStats

	query := r.db.Model(&model.TaskSimilarity{})

	if taskID != "" {
		query = query.Where("task_id = ?", taskID)
	}
	if problemID != "" {
		query = query.Where("problem_id = ?", problemID)
	}
	if setID != "" {
		query = query.Where("set_id = ?", setID)
	}

	// 注意：这里根据您的业务逻辑决定是否添加 isSet 条件
	// 如果 isSet 为0表示不筛选，可以省略这个条件
	if isSet != 0 {
		query = query.Where("is_set = ?", isSet)
	}

	err := query.Select("COUNT(*) as sample_count, " +
		"COUNT(DISTINCT similarity) as group_count, " +
		"MAX(similarity) as max_similarity, " +
		"AVG(similarity) as avg_similarity").
		Scan(&stats).Error

	if err != nil {
		return nil, err
	}

	return &stats, nil
}

// SelectSimilarityDistribution 查询相似度分布
func (r *taskSimilarityRepository) SelectSimilarityDistribution(taskID string) ([]int, error) {
	type RangeResult struct {
		RangeIndex int
		Count      int
	}

	var results []RangeResult

	// 使用原生SQL查询
	query := `
		WITH ranges AS (
			SELECT 0 as range_start, 10 as range_end, 1 as range_index
			UNION ALL SELECT 10, 20, 2
			UNION ALL SELECT 20, 30, 3
			UNION ALL SELECT 30, 40, 4
			UNION ALL SELECT 40, 50, 5
			UNION ALL SELECT 50, 60, 6
			UNION ALL SELECT 60, 70, 7
			UNION ALL SELECT 70, 80, 8
			UNION ALL SELECT 80, 90, 9
			UNION ALL SELECT 90, 101, 10
		)
		SELECT r.range_index, COALESCE(COUNT(ts.similarity), 0) as count
		FROM ranges r
		LEFT JOIN task_similarity ts ON ts.task_id = ? 
			AND ts.similarity >= r.range_start / 100.0 
			AND ts.similarity < r.range_end / 100.0
		GROUP BY r.range_index
		ORDER BY r.range_index
	`

	err := r.db.Raw(query, taskID).Scan(&results).Error
	if err != nil {
		return nil, err
	}

	// 转换为数量数组
	counts := make([]int, 10)
	for _, result := range results {
		if result.RangeIndex >= 1 && result.RangeIndex <= 10 {
			counts[result.RangeIndex-1] = result.Count
		}
	}

	return counts, nil
}

//// SelectDegreeStatistics 查询程度统计
//func (r *taskSimilarityRepository) SelectDegreeStatistics(taskID string, threshold float64) ([]model.CloneLevel, error) {
//	var cloneLevels []model.CloneLevel
//
//	query := `
//		WITH degree_categories AS (
//			SELECT 'HIGHLY_SUSPECTED' as degree, 1 as sort_order
//			UNION ALL SELECT 'MEDIUM_SUSPECTED', 2
//			UNION ALL SELECT 'LOW_SUSPECTED', 3
//			UNION ALL SELECT 'NOT_REACHED', 4
//		),
//		degree_stats AS (
//			SELECT
//				CASE
//					WHEN similarity >= ? * 0.9 THEN 'HIGHLY_SUSPECTED'
//					WHEN similarity >= ? * 0.7 THEN 'MEDIUM_SUSPECTED'
//					WHEN similarity >= ? * 0.5 THEN 'LOW_SUSPECTED'
//					WHEN similarity >= ? THEN 'NOT_REACHED'
//					ELSE 'NOT_DETECTED'
//				END as degree,
//				COUNT(*) as count
//			FROM task_similarity
//			WHERE task_id = ?
//			GROUP BY
//				CASE
//					WHEN similarity >= ? * 0.9 THEN 'HIGHLY_SUSPECTED'
//					WHEN similarity >= ? * 0.7 THEN 'MEDIUM_SUSPECTED'
//					WHEN similarity >= ? * 0.5 THEN 'LOW_SUSPECTED'
//					WHEN similarity >= ? THEN 'NOT_REACHED'
//					ELSE 'NOT_DETECTED'
//				END
//		),
//		total_stats AS (
//			SELECT COUNT(*) as total_count
//			FROM task_similarity
//			WHERE task_id = ?
//		)
//		SELECT
//			dc.degree as clone_level,
//			CASE dc.degree
//				WHEN 'HIGHLY_SUSPECTED' THEN '高度可疑'
//				WHEN 'MEDIUM_SUSPECTED' THEN '中度可疑'
//				WHEN 'LOW_SUSPECTED' THEN '轻度可疑'
//				WHEN 'NOT_REACHED' THEN '未达阈值'
//			END as clone_level_name,
//			NULL as similarity,
//			COALESCE(ds.count, 0) as count,
//			CASE
//				WHEN ts.total_count > 0 THEN
//					ROUND((COALESCE(ds.count, 0) * 100.0 / ts.total_count), 2)
//				ELSE 0
//			END as percentage
//		FROM degree_categories dc
//		LEFT JOIN degree_stats ds ON dc.degree = ds.degree
//		CROSS JOIN total_stats ts
//		ORDER BY dc.sort_order
//	`
//
//	err := r.db.Raw(query,
//		threshold, threshold, threshold, threshold, taskID,
//		threshold, threshold, threshold, threshold, taskID).Scan(&cloneLevels).Error
//
//	if err != nil {
//		return nil, err
//	}
//
//	return cloneLevels, nil
//}

// SelectDegreeStatistics 查询程度统计
func (r *taskSimilarityRepository) SelectDegreeStatistics(taskID string, threshold float64) ([]model.CloneLevel, error) {
	var cloneLevels []model.CloneLevel

	// 首先获取总记录数
	var totalCount int64
	err := r.db.Model(&model.TaskSimilarity{}).
		Where("task_id = ?", taskID).
		Count(&totalCount).Error
	if err != nil {
		return nil, err
	}

	// 定义程度分类
	degreeCategories := []struct {
		degree     string
		name       string
		sortOrder  int
		minPercent float64
		maxPercent float64
	}{
		{"HIGHLY_SUSPECTED", "高度可疑", 1, 0.9, 1.0},
		{"MEDIUM_SUSPECTED", "中度可疑", 2, 0.7, 0.9},
		{"LOW_SUSPECTED", "轻度可疑", 3, 0.5, 0.7},
		{"NOT_REACHED", "未达阈值", 4, 1.0, 1.0}, // 特殊处理
	}

	// 查询每个程度的数量
	for _, category := range degreeCategories {
		var count int64
		var query *gorm.DB

		if category.degree == "NOT_REACHED" {
			// NOT_REACHED 特殊处理：>= threshold 但 < threshold * 0.5
			query = r.db.Model(&model.TaskSimilarity{}).
				Where("task_id = ? AND similarity >= ? AND similarity < ?",
					taskID, threshold, threshold*0.5)
		} else {
			// 其他程度
			query = r.db.Model(&model.TaskSimilarity{}).
				Where("task_id = ? AND similarity >= ? AND similarity < ?",
					taskID, threshold*category.minPercent, threshold*category.maxPercent)
		}

		err := query.Count(&count).Error
		if err != nil {
			return nil, err
		}

		// 计算百分比
		percentage := 0.0
		if totalCount > 0 {
			percentage = float64(count) * 100.0 / float64(totalCount)
			percentage = math.Round(percentage*100) / 100 // 保留2位小数
		}

		cloneLevels = append(cloneLevels, model.CloneLevel{
			CloneLevel:     category.degree,
			CloneLevelName: category.name,
			Count:          int(count),
			Percentage:     percentage,
		})
	}

	return cloneLevels, nil
}

// GetDegreeBySimilarity 根据相似度获取程度
func (r *taskSimilarityRepository) GetDegreeBySimilarity(similarity, threshold float64) (string, error) {
	var degree string

	query := `
		SELECT CASE
			WHEN ? >= ? * 0.9 THEN 'HIGHLY_SUSPECTED'
			WHEN ? >= ? * 0.7 THEN 'MEDIUM_SUSPECTED'
			WHEN ? >= ? * 0.5 THEN 'LOW_SUSPECTED'
			WHEN ? >= ? THEN 'NOT_REACHED'
			ELSE 'NOT_DETECTED'
		END as degree
	`

	err := r.db.Raw(query,
		similarity, threshold,
		similarity, threshold,
		similarity, threshold,
		similarity, threshold).Scan(&degree).Error

	if err != nil {
		return "", err
	}

	return degree, nil
}
