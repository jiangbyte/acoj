package file

import (
	"context"
	"fmt"
	"io"
	"mime/multipart"
	"os"
	"path/filepath"
	"time"

	"github.com/google/uuid"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysfile"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
}

type SysFileVO struct {
	ID           string `json:"id"`
	Name         string `json:"name"`
	OriginalName string `json:"original_name"`
	Path         string `json:"path"`
	URL          string `json:"url"`
	MimeType     string `json:"mime_type"`
	Size         int64  `json:"size"`
	Category     string `json:"category"`
	Storage      string `json:"storage"`
	CreatedAt    string `json:"created_at"`
	CreatedBy    string `json:"created_by"`
	UpdatedAt    string `json:"updated_at"`
	UpdatedBy    string `json:"updated_by"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

func toVO(f *ent.SysFile) SysFileVO {
	return SysFileVO{
		ID:           f.ID,
		Name:         f.Name,
		OriginalName: f.OriginalName,
		Path:         f.Path,
		URL:          f.URL,
		MimeType:     f.MimeType,
		Size:         f.Size,
		Category:     f.Category,
		Storage:      f.Storage,
		CreatedAt:    f.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:    f.CreatedBy,
		UpdatedAt:    f.UpdatedAt.Format("2006-01-02 15:04:05"),
		UpdatedBy:    f.UpdatedBy,
	}
}

func Page(page, size int, keyword string) (int, []*ent.SysFile, error) {
	ctx := context.Background()
	q := db.Client.SysFile.Query()

	if keyword != "" {
		q = q.Where(sysfile.OriginalNameContains(keyword))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(sysfile.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Upload(file io.Reader, header *multipart.FileHeader, loginID string) (*ent.SysFile, error) {
	ctx := context.Background()
	now := time.Now()

	// Generate unique filename
	ext := filepath.Ext(header.Filename)
	uuidName := uuid.New().String() + ext

	// Create date-based directory
	dateStr := now.Format("20060102")
	uploadDir := filepath.Join("uploads", dateStr)
	if err := os.MkdirAll(uploadDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create upload dir: %w", err)
	}

	// Save file to disk
	filePath := filepath.Join(uploadDir, uuidName)
	dst, err := os.Create(filePath)
	if err != nil {
		return nil, fmt.Errorf("failed to create file: %w", err)
	}
	defer dst.Close()

	written, err := io.Copy(dst, file)
	if err != nil {
		return nil, fmt.Errorf("failed to write file: %w", err)
	}

	// Build URL
	url := fmt.Sprintf("/uploads/%s/%s", dateStr, uuidName)

	// Detect mime type
	mimeType := header.Header.Get("Content-Type")

	// Save to database
	item, err := db.Client.SysFile.Create().
		SetID(utils.NextID()).
		SetName(uuidName).
		SetOriginalName(header.Filename).
		SetPath(filePath).
		SetURL(url).
		SetMimeType(mimeType).
		SetSize(written).
		SetStorage("local").
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID).
		Save(ctx)
	if err != nil {
		// Clean up file on DB error
		os.Remove(filePath)
		return nil, fmt.Errorf("failed to save file record: %w", err)
	}

	return item, nil
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Get files to delete from disk
	files, err := db.Client.SysFile.Query().
		Where(sysfile.IDIn(ids...)).
		All(ctx)
	if err != nil {
		return err
	}

	// Delete from database
	_, err = db.Client.SysFile.Delete().Where(sysfile.IDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	// Delete physical files
	for _, f := range files {
		os.Remove(f.Path)
	}

	return nil
}

func Download(id string) (*ent.SysFile, error) {
	ctx := context.Background()
	return db.Client.SysFile.Get(ctx, id)
}

func Detail(id string) (*ent.SysFile, error) {
	return Download(id)
}

func RemoveAbsolute(ids []string) error {
	return Remove(ids)
}
