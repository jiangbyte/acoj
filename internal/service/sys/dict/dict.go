package dict

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:dict:page", "sys/dict", "BACKEND", "字典查询")
	auth.RegisterPermission("sys:dict:create", "sys/dict", "BACKEND", "字典新增")
	auth.RegisterPermission("sys:dict:modify", "sys/dict", "BACKEND", "字典修改")
	auth.RegisterPermission("sys:dict:remove", "sys/dict", "BACKEND", "字典删除")
	auth.RegisterPermission("sys:dict:detail", "sys/dict", "BACKEND", "字典详情")
	auth.RegisterPermission("sys:dict:export", "sys/dict", "BACKEND", "字典导出")
	auth.RegisterPermission("sys:dict:template", "sys/dict", "BACKEND", "字典导入模板")
	auth.RegisterPermission("sys:dict:import", "sys/dict", "BACKEND", "字典导入")
	auth.RegisterPermission("sys:dict:list", "sys/dict", "BACKEND", "字典列表")
	auth.RegisterPermission("sys:dict:tree", "sys/dict", "BACKEND", "字典树")
	auth.RegisterPermission("sys:dict:get-label", "sys/dict", "BACKEND", "字典标签查询")
	auth.RegisterPermission("sys:dict:get-children", "sys/dict", "BACKEND", "字典子级列表")
}

func Page(ctx context.Context, keyword, parentId, category string, current, size int) (*utility.PageRes, error) {
	m := dao.SysDict.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("label LIKE ?", kw)
	}
	if parentId != "" {
		// Return both the parent and its children (matches Python behavior)
		m = m.Where("parent_id = ? OR id = ?", parentId, parentId)
	}
	if category != "" {
		m = m.Where("category", category)
	}
	count, err := m.Count()
	if err != nil {
		return nil, err
	}

	all, err := m.Page(current, size).All()
	if err != nil {
		return nil, err
	}
	list := all.List()
	batchEnrichNames(ctx, list)
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, code, label, value, color, category, parentId, status string, sortCode int) error {
	if parentId == "" {
		parentId = "0"
	}
	if code == "" {
		code = utility.GenerateID()
	}
	if err := checkDuplicate(ctx, parentId, label, value, ""); err != nil {
		return err
	}

	loginId := getLoginId(ctx)
	_, err := dao.SysDict.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         utility.GenerateID(),
		"code":       code,
		"label":      label,
		"value":      value,
		"color":      color,
		"category":   category,
		"parent_id":  parentId,
		"status":     ifEmpty(status, consts.StatusEnabled),
		"sort_code":  sortCode,
		"created_by": loginId,
	})
	if err != nil {
		return err
	}
	return syncCache(ctx)
}

func Modify(ctx context.Context, id, code, label, value, color, category, parentId, status string, sortCode int) error {
	existing, err := dao.SysDict.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil {
		return err
	}
	if existing == nil {
		return errors.New("数据不存在")
	}

	updateData := g.Map{}
	if code != "" {
		updateData["code"] = code
	}
	if label != "" {
		updateData["label"] = label
	}
	if value != "" {
		updateData["value"] = value
	}
	if color != "" {
		updateData["color"] = color
	}
	if category != "" {
		updateData["category"] = category
	}
	if parentId != "" {
		updateData["parent_id"] = parentId
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}

	if len(updateData) == 0 {
		return nil
	}

	// Determine final parent_id for validation
	oldParentId := existing["parent_id"].String()
	newParentId := parentId
	if newParentId == "" {
		newParentId = oldParentId
	}
	if newParentId == "" {
		newParentId = "0"
	}

	// Determine final label/value for duplicate check
	finalLabel := label
	if finalLabel == "" {
		finalLabel = existing["label"].String()
	}
	finalValue := value
	if finalValue == "" {
		finalValue = existing["value"].String()
	}

	// Duplicate check (ignores current entity via excludeId)
	if err := checkDuplicate(ctx, newParentId, finalLabel, finalValue, id); err != nil {
		return err
	}

	// Circular parent check (only if parent_id changed)
	if newParentId != oldParentId {
		if err := checkCircularParent(ctx, id, newParentId); err != nil {
			return err
		}
	}

	updateData["updated_by"] = getLoginId(ctx)
	_, err = dao.SysDict.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
	if err != nil {
		return err
	}
	return syncCache(ctx)
}

func Remove(ctx context.Context, ids []string) error {
	// Load all records for building descendant tree
	allRecords, err := dao.SysDict.Ctx().Ctx(ctx).All()
	if err != nil {
		return err
	}

	// Build children map: parent_id -> [child_ids]
	childrenMap := make(map[string][]string)
	for _, r := range allRecords {
		pid := r["parent_id"].String()
		if pid == "" {
			pid = "0"
		}
		childrenMap[pid] = append(childrenMap[pid], r["id"].String())
	}

	// Collect all descendant ids using DFS (cascade delete)
	allIds := make(map[string]bool)
	for _, id := range ids {
		allIds[id] = true
	}
	stack := make([]string, len(ids))
	copy(stack, ids)
	for len(stack) > 0 {
		parentId := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		for _, childId := range childrenMap[parentId] {
			if !allIds[childId] {
				allIds[childId] = true
				stack = append(stack, childId)
			}
		}
	}

	finalIds := make([]string, 0, len(allIds))
	for id := range allIds {
		finalIds = append(finalIds, id)
	}

	_, err = dao.SysDict.Ctx().Ctx(ctx).WherePri(finalIds).Delete()
	if err != nil {
		return err
	}
	return syncCache(ctx)
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysDict.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":         row["id"].String(),
		"code":       row["code"].String(),
		"label":      row["label"].String(),
		"value":      row["value"].String(),
		"color":      row["color"].String(),
		"category":   row["category"].String(),
		"parent_id":  row["parent_id"].String(),
		"status":     row["status"].String(),
		"sort_code":  row["sort_code"].Int(),
		"created_at": row["created_at"].String(),
		"created_by": row["created_by"].String(),
		"updated_at": row["updated_at"].String(),
		"updated_by": row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
}

func List(ctx context.Context, parentId, category string) ([]g.Map, error) {
	m := dao.SysDict.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if parentId != "" {
		m = m.Where("parent_id", parentId)
	}
	if category != "" {
		m = m.Where("category", category)
	}
	var list []g.Map
	if err := m.Scan(&list); err != nil {
		return nil, err
	}
	return list, nil
}

func Tree(ctx context.Context, category, status string) ([]g.Map, error) {
	// Try Redis cache first for the full tree
	cached, err := GetCachedTree(ctx)
	if err == nil && cached != nil {
		return cached, nil
	}

	// Cache miss: build from DB
	m := dao.SysDict.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if category != "" {
		m = m.Where("category", category)
	}
	if status != "" {
		m = m.Where("status", status)
	}
	rows, err := m.All()
	if err != nil {
		return nil, err
	}

	dictMap := make(map[string]g.Map)
	for _, r := range rows {
		dictMap[r["id"].String()] = g.Map{
			"id":        r["id"].String(),
			"code":      r["code"].String(),
			"label":     r["label"].String(),
			"value":     r["value"].String(),
			"color":     r["color"].String(),
			"category":  r["category"].String(),
			"parent_id": r["parent_id"].String(),
			"status":    r["status"].String(),
			"sort_code": r["sort_code"].Int(),
			"children":  []g.Map{},
		}
	}

	var tree []g.Map
	for _, r := range rows {
		node := dictMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && dictMap[pid] != nil {
			children, _ := dictMap[pid]["children"].([]g.Map)
			children = append(children, node)
			dictMap[pid]["children"] = children
		} else {
			tree = append(tree, node)
		}
	}
	return tree, nil
}

func GetLabel(ctx context.Context, code, value string) (string, error) {
	// 1. Find root dict by code
	root, err := dao.SysDict.Ctx().Ctx(ctx).Where("code", code).One()
	if err != nil {
		return "", err
	}
	if root == nil {
		return "", nil
	}
	// 2. Find children by parent_id = root.id, matching the given value
	row, err := dao.SysDict.Ctx().Ctx(ctx).
		Where("parent_id", root["id"].String()).
		Where("value", value).
		Fields("label").One()
	if err != nil {
		return "", err
	}
	if row == nil {
		return "", nil
	}
	return row["label"].String(), nil
}

func GetChildren(ctx context.Context, typeCode string) ([]g.Map, error) {
	// 1. Find root dict by code
	root, err := dao.SysDict.Ctx().Ctx(ctx).Where("code", typeCode).One()
	if err != nil {
		return nil, err
	}
	if root == nil {
		return nil, nil
	}
	// 2. Find children by parent_id = root.id
	var list []g.Map
	err = dao.SysDict.Ctx().Ctx(ctx).
		Where("parent_id", root["id"].String()).
		OrderAsc("sort_code").Scan(&list)
	if err != nil {
		return nil, err
	}
	return list, nil
}

func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map
	switch exportType {
	case "current":
		pageSize := size
		if pageSize <= 0 {
			pageSize = 10
		}
		pageCurrent := current
		if pageCurrent <= 0 {
			pageCurrent = 1
		}
		m := dao.SysDict.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysDict.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysDict.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}
	return utility.CreateExcelFromData(data, "字典数据")
}

func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"code", "label", "value", "color", "category", "parent_id", "status", "sort_code"}
	return utility.CreateExcelTemplate(headers, "字典数据")
}

func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	if file.Size > 5*1024*1024 {
		return nil, fmt.Errorf("文件大小不能超过5MB")
	}
	if !strings.HasSuffix(strings.ToLower(file.Filename), ".xlsx") {
		return nil, fmt.Errorf("仅支持.xlsx格式文件")
	}

	content, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	rows, err := utility.ParseExcelFromBytes(content, true)
	if err != nil {
		return nil, err
	}

	if len(rows) == 0 {
		return nil, fmt.Errorf("导入数据不能为空")
	}

	imported := 0
	for _, row := range rows {
		id := utility.GenerateID()
		_, err := dao.SysDict.Ctx().Ctx(ctx).Insert(g.Map{
			"id":         id,
			"code":       row["code"],
			"label":      row["label"],
			"value":      row["value"],
			"color":      row["color"],
			"category":   row["category"],
			"parent_id":  row["parent_id"],
			"status":     row["status"],
			"sort_code":  row["sort_code"],
			"created_by": getLoginId(ctx),
		})
		if err == nil {
			imported++
		}
	}

	_ = syncCache(ctx)

	return g.Map{
		"total":   imported,
		"message": fmt.Sprintf("成功导入%d条数据", imported),
	}, nil
}

// ---- Duplicate check ----

func checkDuplicate(ctx context.Context, parentId, label, value, excludeId string) error {
	if label != "" {
		m := dao.SysDict.Ctx().Ctx(ctx).Where("parent_id", parentId).Where("label", label)
		if excludeId != "" {
			m = m.WhereNot("id", excludeId)
		}
		count, err := m.Count()
		if err != nil {
			return err
		}
		if count > 0 {
			return errors.New("同一父字典下已存在相同标签: " + label)
		}
	}
	if value != "" {
		m := dao.SysDict.Ctx().Ctx(ctx).Where("parent_id", parentId).Where("value", value)
		if excludeId != "" {
			m = m.WhereNot("id", excludeId)
		}
		count, err := m.Count()
		if err != nil {
			return err
		}
		if count > 0 {
			return errors.New("同一父字典下已存在相同值: " + value)
		}
	}
	return nil
}

// ---- Circular parent check ----

func checkCircularParent(ctx context.Context, entityId, newParentId string) error {
	if newParentId == "" || newParentId == "0" {
		return nil
	}
	allRecords, err := dao.SysDict.Ctx().Ctx(ctx).All()
	if err != nil {
		return err
	}
	parentMap := make(map[string]string)
	for _, r := range allRecords {
		parentMap[r["id"].String()] = r["parent_id"].String()
	}
	current := newParentId
	for current != "" {
		if current == entityId {
			return errors.New("父级不能选择自身或子节点")
		}
		current = parentMap[current]
		if current == "" || current == "0" {
			break
		}
	}
	return nil
}

// ---- Redis cache ----

func syncCache(ctx context.Context) error {
	records, err := dao.SysDict.Ctx().Ctx(ctx).OrderAsc("sort_code").All()
	if err != nil {
		return err
	}

	// Build children_by_parent map
	childrenByParent := make(map[string][]gdb.Record)
	for _, r := range records {
		pid := r["parent_id"].String()
		if pid == "" {
			pid = "0"
		}
		childrenByParent[pid] = append(childrenByParent[pid], r)
	}

	// Flat cache: typeCode -> [{label, value, color}, ...]
	flatCache := make(map[string][]map[string]string)
	for _, r := range records {
		code := r["code"].String()
		pid := r["parent_id"].String()
		if code != "" && (pid == "" || pid == "0") {
			id := r["id"].String()
			children := childrenByParent[id]
			if len(children) > 0 {
				childList := make([]map[string]string, 0, len(children))
				for _, c := range children {
					childList = append(childList, map[string]string{
						"label": c["label"].String(),
						"value": c["value"].String(),
						"color": c["color"].String(),
					})
				}
				flatCache[code] = childList
			}
		}
	}

	// Full tree for tree cache
	tree := buildFullTree(records)

	// Marshal
	flatJson, _ := json.Marshal(flatCache)
	treeJson, _ := json.Marshal(tree)

	// Set to Redis with 24h expiry
	_ = g.Redis().SetEX(ctx, consts.DictCacheKey, flatJson, 86400)
	_ = g.Redis().SetEX(ctx, consts.DictTreeCacheKey, treeJson, 86400)

	return nil
}

// buildFullTree builds a complete tree structure from all dict records.
func buildFullTree(records gdb.Result) []g.Map {
	nodeMap := make(map[string]g.Map)
	var roots []g.Map

	for _, r := range records {
		node := g.Map{
			"id":        r["id"].String(),
			"code":      r["code"].String(),
			"label":     r["label"].String(),
			"value":     r["value"].String(),
			"color":     r["color"].String(),
			"category":  r["category"].String(),
			"parent_id": r["parent_id"].String(),
			"status":    r["status"].String(),
			"sort_code": r["sort_code"].Int(),
			"children":  []g.Map{},
		}
		nodeMap[r["id"].String()] = node
	}

	for _, r := range records {
		node := nodeMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && pid != "0" && nodeMap[pid] != nil {
			children, _ := nodeMap[pid]["children"].([]g.Map)
			children = append(children, node)
			nodeMap[pid]["children"] = children
		} else {
			roots = append(roots, node)
		}
	}
	return roots
}

// GetCachedDicts returns the flat dict cache from Redis.
// Returns a map of typeCode -> [{label, value, color}, ...].
func GetCachedDicts(ctx context.Context) (map[string][]map[string]string, error) {
	data, err := g.Redis().Get(ctx, consts.DictCacheKey)
	if err != nil || data.IsNil() {
		return nil, err
	}
	var result map[string][]map[string]string
	if err := json.Unmarshal(data.Bytes(), &result); err != nil {
		return nil, err
	}
	return result, nil
}

// GetCachedTree returns the full dict tree from Redis cache.
func GetCachedTree(ctx context.Context) ([]g.Map, error) {
	data, err := g.Redis().Get(ctx, consts.DictTreeCacheKey)
	if err != nil || data.IsNil() {
		return nil, err
	}
	var result []g.Map
	if err := json.Unmarshal(data.Bytes(), &result); err != nil {
		return nil, err
	}
	return result, nil
}

// ---- Helper functions ----

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}

func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	delete(result, "id")
	return result
}

func batchEnrichNames(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichCreatorUpdater(ctx, item)
	}
}

func enrichCreatorUpdater(ctx context.Context, item g.Map) {
	if id, ok := item["created_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["created_name"] = row["nickname"].String()
		}
	}
	if id, ok := item["updated_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["updated_name"] = row["nickname"].String()
		}
	}
}
