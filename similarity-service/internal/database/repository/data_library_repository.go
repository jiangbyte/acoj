package repository

import (
	"gorm.io/gorm"
	"similarity-service/internal/database/model"
)

type DataLibraryRepository interface {
	// 操作接口
	GetSimilarityLibraries(isSet bool, problemID, language, userID, setId string, limit int) ([]model.DataLibrary, error)
}

type dataLibraryRepository struct {
	db *gorm.DB
}

func NewDataLibraryRepository(db *gorm.DB) DataLibraryRepository {
	return &dataLibraryRepository{
		db: db,
	}
}

// GetSimilarityLibraries 根据条件查询相似代码库
func (r *dataLibraryRepository) GetSimilarityLibraries(isSet bool, problemID, language, userID, setId string, limit int) ([]model.DataLibrary, error) {
	var dataLibraries []model.DataLibrary

	query := r.db.Model(&model.DataLibrary{}).
		Where("problem_id = ?", problemID).
		Where("language = ?", language).
		Where("user_id != ?", userID).
		Order("update_time DESC").
		Limit(limit)

	if isSet {
		query = query.
			Where("is_set = ?", true).
			Where("set_id = ?", setId)
	} else {
		query = query.
			Where("is_set = ?", false)
	}

	err := query.Find(&dataLibraries).Error
	if err != nil {
		return nil, err
	}

	return dataLibraries, nil
}
