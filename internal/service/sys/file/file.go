package file

import (
	"context"
	"os"
	"path/filepath"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/gogf/gf/v2/os/gfile"
	"github.com/gogf/gf/v2/text/gstr"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Upload(ctx context.Context, file ghttp.UploadFile, engine string) (g.Map, error) {
	loginId := getLoginId(ctx)

	// Get upload directory from config
	uploadDir := g.Cfg().MustGet(ctx, "hei.upload.dir", "resource/upload").String()

	// Ensure directory exists
	absDir := gfile.Join(gfile.Pwd(), uploadDir)
	if !gfile.Exists(absDir) {
		_ = gfile.Mkdir(absDir)
	}

	// Save file
	savedName, err := file.Save(absDir, true)
	if err != nil {
		return nil, err
	}

	storagePath := gfile.Join(uploadDir, savedName)
	fileSize := file.Size
	fileExt := filepath.Ext(file.Filename)

	// Insert DB record
	id := utility.GenerateID()
	_, err = dao.SysFile.Ctx().Ctx(ctx).Insert(g.Map{
		"id":           id,
		"engine":       engine,
		"name":         file.Filename,
		"suffix":       fileExt,
		"size_kb":      fileSize / 1024,
		"size_info":    gstr.JoinAny([]interface{}{fileSize / 1024, "KB"}, ""),
		"obj_name":     savedName,
		"storage_path": storagePath,
		"created_by":   loginId,
	})
	if err != nil {
		return nil, err
	}

	return g.Map{
		"id":        id,
		"file_name": file.Filename,
		"file_path": storagePath,
		"file_size": fileSize,
		"engine":    engine,
	}, nil
}

func Download(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}

	absPath := gfile.Join(gfile.Pwd(), row["storage_path"].String())
	if !gfile.Exists(absPath) {
		return nil, os.ErrNotExist
	}

	return g.Map{
		"id":            row["id"].String(),
		"name":          row["name"].String(),
		"storage_path":  row["storage_path"].String(),
		"download_path": row["download_path"].String(),
		"abs_path":      absPath,
	}, nil
}

func Page(ctx context.Context, keyword, engine, dateRangeStart, dateRangeEnd string, current, size int) (*utility.PageRes, error) {
	m := dao.SysFile.Ctx().Ctx(ctx).OrderDesc("created_at")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("name LIKE ?", kw)
	}
	if engine != "" {
		m = m.Where("engine", engine)
	}
	if dateRangeStart != "" {
		m = m.Where("created_at >= ?", dateRangeStart)
	}
	if dateRangeEnd != "" {
		m = m.Where("created_at <= ?", dateRangeEnd)
	}

	count, err := m.Count()
	if err != nil {
		return nil, err
	}
	var list []g.Map
	if err := m.Page(current, size).Scan(&list); err != nil {
		return nil, err
	}
	return utility.NewPageRes(list, count, current, size), nil
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":            row["id"].String(),
		"engine":        row["engine"].String(),
		"name":          row["name"].String(),
		"suffix":        row["suffix"].String(),
		"size_kb":       row["size_kb"].Int(),
		"size_info":     row["size_info"].String(),
		"obj_name":      row["obj_name"].String(),
		"storage_path":  row["storage_path"].String(),
		"download_path": row["download_path"].String(),
		"created_at":    row["created_at"].String(),
		"created_by":    row["created_by"].String(),
		"updated_at":    row["updated_at"].String(),
		"updated_by":    row["updated_by"].String(),
	}, nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func RemoveAbsolute(ctx context.Context, ids []string) error {
	rows, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).All()
	if err != nil {
		return err
	}
	for _, row := range rows {
		storagePath := row["storage_path"].String()
		if storagePath != "" {
			absPath := gfile.Join(gfile.Pwd(), storagePath)
			if gfile.Exists(absPath) {
				_ = gfile.Remove(absPath)
			}
		}
	}
	_, err = dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}
