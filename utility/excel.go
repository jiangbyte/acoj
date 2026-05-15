package utility

import (
	"bytes"
	"fmt"

	"github.com/xuri/excelize/v2"
)

// CreateExcelFromData creates an Excel file from a slice of maps.
// The first row is the header (keys of the first map).
func CreateExcelFromData(data []map[string]interface{}, sheetName string) (*bytes.Buffer, error) {
	f := excelize.NewFile()
	defer f.Close()

	idx, err := f.GetSheetIndex("Sheet1")
	if err == nil {
		f.SetSheetName(f.GetSheetName(idx), sheetName)
	}

	if len(data) == 0 {
		buf := new(bytes.Buffer)
		if err := f.Write(buf); err != nil {
			return nil, err
		}
		return buf, nil
	}

	// Write header
	headers := make([]string, 0, len(data[0]))
	for k := range data[0] {
		headers = append(headers, k)
	}
	headerRow := make([]interface{}, len(headers))
	for i, h := range headers {
		headerRow[i] = h
	}
	cell, _ := excelize.CoordinatesToCellName(1, 1)
	f.SetSheetRow(sheetName, cell, &headerRow)

	// Write data rows
	for rowIdx, row := range data {
		values := make([]interface{}, len(headers))
		for i, h := range headers {
			values[i] = row[h]
		}
		cell, _ := excelize.CoordinatesToCellName(1, rowIdx+2)
		f.SetSheetRow(sheetName, cell, &values)
	}

	buf := new(bytes.Buffer)
	if err := f.Write(buf); err != nil {
		return nil, err
	}
	return buf, nil
}

// CreateExcelTemplate creates an Excel template file with headers only.
func CreateExcelTemplate(headers []string, sheetName string) (*bytes.Buffer, error) {
	f := excelize.NewFile()
	defer f.Close()

	idx, err := f.GetSheetIndex("Sheet1")
	if err == nil {
		f.SetSheetName(f.GetSheetName(idx), sheetName)
	}

	headerRow := make([]interface{}, len(headers))
	for i, h := range headers {
		headerRow[i] = h
	}
	cell, _ := excelize.CoordinatesToCellName(1, 1)
	f.SetSheetRow(sheetName, cell, &headerRow)

	buf := new(bytes.Buffer)
	if err := f.Write(buf); err != nil {
		return nil, err
	}
	return buf, nil
}

// ParseExcelFromBytes parses an Excel file from bytes, returning rows as maps.
// If hasHeader is true, the first row is used as keys; otherwise "col1","col2"... are used.
func ParseExcelFromBytes(content []byte, hasHeader bool) ([]map[string]string, error) {
	f, err := excelize.OpenReader(bytes.NewReader(content))
	if err != nil {
		return nil, fmt.Errorf("cannot open excel: %w", err)
	}
	defer f.Close()

	sheetName := f.GetSheetName(0)
	rows, err := f.GetRows(sheetName)
	if err != nil {
		return nil, fmt.Errorf("cannot read sheet: %w", err)
	}

	if len(rows) == 0 {
		return nil, nil
	}

	var headers []string
	var startRow int
	if hasHeader {
		headers = rows[0]
		startRow = 1
	} else {
		for i := 0; i < len(rows[0]); i++ {
			headers = append(headers, fmt.Sprintf("col%d", i+1))
		}
		startRow = 0
	}

	var result []map[string]string
	for _, row := range rows[startRow:] {
		rowMap := make(map[string]string)
		hasValue := false
		for i, h := range headers {
			if i < len(row) {
				rowMap[h] = row[i]
				if row[i] != "" {
					hasValue = true
				}
			}
		}
		if hasValue {
			result = append(result, rowMap)
		}
	}

	return result, nil
}
