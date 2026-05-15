package utils

import (
	"bytes"
	"fmt"
	"strconv"

	"github.com/xuri/excelize/v2"
)

// ExportExcel generates an Excel file from a slice of maps.
// headers example: [{"field":"title","name":"标题"}, ...]
func ExportExcel(data []map[string]interface{}, headers []map[string]string, sheetName string) ([]byte, error) {
	f := excelize.NewFile()
	defer f.Close()

	index, _ := f.NewSheet(sheetName)
	f.SetActiveSheet(index)

	// Write header row
	for i, h := range headers {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		f.SetCellValue(sheetName, cell, h["name"])
	}

	// Write data rows
	for rowIdx, item := range data {
		for colIdx, h := range headers {
			cell, _ := excelize.CoordinatesToCellName(colIdx+1, rowIdx+2)
			val := item[h["field"]]
			if val != nil {
				f.SetCellValue(sheetName, cell, val)
			}
		}
	}

	// Remove default sheet
	if sheetName != "Sheet1" {
		_ = f.DeleteSheet("Sheet1")
	}

	buf, err := f.WriteToBuffer()
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// ParseExcel reads an Excel file and returns rows as []map[string]string (header → value).
func ParseExcel(fileBytes []byte, sheetName string) ([]map[string]string, error) {
	f, err := excelize.OpenReader(bytes.NewReader(fileBytes))
	if err != nil {
		return nil, fmt.Errorf("failed to open excel: %w", err)
	}
	defer f.Close()

	rows, err := f.GetRows(sheetName)
	if err != nil {
		// Try first sheet
		sheets := f.GetSheetList()
		if len(sheets) == 0 {
			return nil, fmt.Errorf("no sheets found")
		}
		rows, err = f.GetRows(sheets[0])
		if err != nil {
			return nil, fmt.Errorf("failed to read sheet: %w", err)
		}
	}

	if len(rows) < 2 {
		return nil, nil
	}

	headers := rows[0]
	var result []map[string]string

	for _, row := range rows[1:] {
		item := map[string]string{}
		for i, h := range headers {
			if i < len(row) {
				item[h] = row[i]
			} else {
				item[h] = ""
			}
		}
		result = append(result, item)
	}

	return result, nil
}

// ExportToResponse sets the appropriate headers for Excel file download.
func ExportToResponse(filename string) (string, string) {
	return "Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, filename)
}

// BuildHeaders builds header definitions from a field list and name map.
func BuildHeaders(fields []string, names map[string]string) []map[string]string {
	var headers []map[string]string
	for _, f := range fields {
		name := f
		if n, ok := names[f]; ok {
			name = n
		}
		headers = append(headers, map[string]string{
			"field": f,
			"name":  name,
		})
	}
	return headers
}

// GetFieldString safely extracts a string value from a map.
func GetFieldString(data map[string]interface{}, key string) string {
	if v, ok := data[key]; ok && v != nil {
		return fmt.Sprintf("%v", v)
	}
	return ""
}

// GetFieldInt64 safely extracts an int64 value from a map.
func GetFieldInt64(data map[string]interface{}, key string) int64 {
	if v, ok := data[key]; ok && v != nil {
		switch val := v.(type) {
		case float64:
			return int64(val)
		case string:
			n, _ := strconv.ParseInt(val, 10, 64)
			return n
		case int64:
			return val
		case int:
			return int64(val)
		}
	}
	return 0
}
